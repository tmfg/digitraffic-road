package fi.livi.digitraffic.tie.data.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.LockingDao;

@Service
public class LockingService {
    private final LockingDao lockingDao;

    private final String instanceId;

    private static final Logger log = LoggerFactory.getLogger(LockingService.class);

    public LockingService(final LockingDao lockingDao) {
        this.lockingDao = lockingDao;
        this.instanceId = UUID.randomUUID().toString();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean acquireLock(final String lockName, final int expirationSeconds) {
        return lockingDao.acquireLock(lockName, instanceId, expirationSeconds);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void releaseLock(final String lockName) {
        lockingDao.releaseLock(lockName, instanceId);
    }

    public String getInstanceId() {
        return instanceId;
    }
}
