package fi.livi.digitraffic.tie.dao;

import java.time.Instant;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LockingRepository extends SqlRepository {

    @Query(value =
           "SELECT LT.LOCK_NAME as lockName\n" +
           "     , LT.LOCK_LOCKED as lockLocked\n" +
           "     , LT.LOCK_EXPIRES as lockExpires\n" +
           "     , LT.INSTANCE_ID as instanceId\n\n" +
           "FROM LOCKING_TABLE LT\n" +
           "WHERE LT.LOCK_NAME = :lockName",
           nativeQuery = true)
    LockInfo getLockInfo(@Param("lockName")  final String lockName);

    interface LockInfo {
        String getLockName();
        String getInstanceId();
        Instant getLockLocked();
        Instant getLockExpires();
    }
}
