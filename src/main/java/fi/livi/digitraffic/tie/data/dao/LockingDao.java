package fi.livi.digitraffic.tie.data.dao;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LockingDao {

    private static final Logger log = LoggerFactory.getLogger(LockingDao.class);
    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Acquires lock for given instanceId.
     * If lock doesn't exist then lock is acquired by inserting new lock-row.
     * If instance already have the lock then lock expiration is updated.
     * If instance doesn't have the lock but lock exists
     * then checks if previous lock has expired and updates the lock-row.
     */
    private static final String MERGE =
            "MERGE INTO LOCKING_TABLE dst\n" +
            "  USING (\n" +
            "    SELECT :lockName as LOCK_NAME\n" +
            "         , :instanceId INSTANCE_ID\n" +
            "         , sysdate LOCK_LOCKED\n" +
            "         , sysdate + NUMTODSINTERVAL(:expirationSeconds, 'SECOND') LOCK_EXPIRES\n" +
            "    FROM DUAL\n" +
            "  ) src ON (dst.LOCK_NAME = src.LOCK_NAME)\n" +
            "  WHEN MATCHED THEN\n" +
            "    UPDATE SET dst.INSTANCE_ID = src.INSTANCE_ID\n" +
            "             , dst.LOCK_LOCKED = src.LOCK_LOCKED \n" +
            "             , dst.LOCK_EXPIRES = src.LOCK_EXPIRES\n" +
            "    WHERE dst.INSTANCE_ID = src.INSTANCE_ID OR " +
            "          dst.LOCK_EXPIRES < sysdate\n" +
            "  WHEN NOT MATCHED THEN\n" +
            "    INSERT (dst.LOCK_NAME, dst.INSTANCE_ID, dst.LOCK_LOCKED, dst.LOCK_EXPIRES)\n" +
            "    VALUES (src.LOCK_NAME, src.INSTANCE_ID, src.LOCK_LOCKED, src.LOCK_EXPIRES)";

    private static final String RELEASE =
            "DELETE FROM LOCKING_TABLE LT\n" +
            "WHERE LT.LOCK_NAME = :lockName\n" +
            "  AND LT.INSTANCE_ID = :instanceId";

    private static final String SELECT =
            "SELECT LOCK_NAME\n" +
            "FROM LOCKING_TABLE LT\n" +
            "WHERE LT.LOCK_NAME = :lockName\n" +
            "  AND LT.INSTANCE_ID = :instanceId\n" +
            "  AND LT.LOCK_EXPIRES > sysdate";

    @Autowired
    public LockingDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean acquireLock(final String lockName, final String callerInstanceId, int expirationSeconds) {

        HashMap<String, Object> params = new HashMap<>();
        params.put("lockName", lockName);
        params.put("instanceId", callerInstanceId);
        params.put("expirationSeconds", expirationSeconds);

        try {
            // If lock was acquired successfull then query should return one row
            jdbcTemplate.update(MERGE, params);
            return jdbcTemplate.queryForList(SELECT, params, String.class).size() == 1;
        } catch (Exception e) {
            // May happen when lock-row doesn't exist in db and different instances try to insert it at the same time
            if (e instanceof org.springframework.dao.DuplicateKeyException) {
                log.info("Locking failed (lockName={}, callerInstanceId={}, expirationSeconds={})", lockName, callerInstanceId, expirationSeconds);
                return false;
            }
            throw e;
        }
    }

    public void releaseLock(final String lockName, final String callerInstanceId) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("lockName", lockName);
        params.put("instanceId", callerInstanceId);
        jdbcTemplate.update(RELEASE, params);
    }
}
