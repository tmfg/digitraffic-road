package fi.livi.digitraffic.tie.data.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class LockingDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Acquires lock for given threadId.
     * If lock doesn't exist then lock is acquired by inserting new lock-row.
     * If instance already have the lock then lock expiration is updated.
     * If instance doesn't have the lock but lock exists
     * then checks if previous lock has expired and updates the lock-row.
     */
    private static final String MERGE =
        "insert into locking_table(lock_name, thread_id, lock_locked, lock_expires)\n" +
        "VALUES (:lockName, :threadId, clock_timestamp(), clock_timestamp() + :expirationSeconds::integer * interval '1 second')\n" +
        "ON CONFLICT (lock_name)\n" +
        "DO UPDATE SET\n" +
        "   thread_id = :threadId,\n" +
        "   lock_locked = clock_timestamp(),\n" +
        "   lock_expires = clock_timestamp() + :expirationSeconds::integer * interval '1 second'\n" +
        "where locking_table.thread_id = :threadId OR locking_table.lock_expires < clock_timestamp()";

    private static final String RELEASE =
        "DELETE FROM LOCKING_TABLE LT\n" +
        "WHERE LT.LOCK_NAME = :lockName\n" +
        "  AND LT.thread_id = :threadId";

    private static final String SELECT =
        "SELECT LOCK_NAME\n" +
        "FROM LOCKING_TABLE LT\n" +
        "WHERE LT.LOCK_NAME = :lockName\n" +
        "  AND LT.thread_id = :threadId\n" +
        "  AND LT.LOCK_EXPIRES > clock_timestamp()";

    @Autowired
    public LockingDao(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean acquireLock(final String lockName, final long threadId, final int expirationSeconds) {
        final MapSqlParameterSource params = new MapSqlParameterSource("lockName", lockName)
            .addValue("threadId", threadId)
            .addValue("expirationSeconds", expirationSeconds);

        jdbcTemplate.update(MERGE, params);

        return hasLock(params);
    }

    private boolean hasLock(final MapSqlParameterSource params) {
        // If lock was acquired successfully then query should return one row
        return jdbcTemplate.queryForList(SELECT, params, String.class).size() == 1;
    }

    public void releaseLock(final String lockName, final long threadId) {
        final MapSqlParameterSource params = new MapSqlParameterSource("lockName", lockName)
            .addValue("threadId", threadId);

        jdbcTemplate.update(RELEASE, params);
    }
}
