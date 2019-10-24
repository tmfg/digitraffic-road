package fi.livi.digitraffic.tie.data.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.LockingDao;

@Service
public class LockingServiceInternal {
    private final LockingDao lockingDao;

    private final String instanceId;

    public LockingServiceInternal(final LockingDao lockingDao) {
        this.lockingDao = lockingDao;
        this.instanceId = UUID.randomUUID().toString();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    boolean tryLock(final String lockName, final int expirationSeconds) {
        return lockingDao.acquireLock(lockName, getThreadId(), expirationSeconds);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void unlock(final String lockName) {
        lockingDao.releaseLock(lockName, getThreadId());
    }

    public long getThreadId() {
        System.out.println("Thread: " + Thread.currentThread().getId());
        return Thread.currentThread().getId();
    }
}
