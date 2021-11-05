package fi.livi.digitraffic.tie.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.LockingDao;
import fi.livi.digitraffic.tie.dao.LockingRepository;

@Service
public class LockingServiceInternal {

    private final LockingDao lockingDao;
    private final LockingRepository lockingRepository;

    public LockingServiceInternal(final LockingDao lockingDao,
                                  final LockingRepository lockingRepository) {
        this.lockingDao = lockingDao;
        this.lockingRepository = lockingRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean tryLock(final String lockName, final int expirationSeconds, final long instanceId) {
        return lockingDao.acquireLock(lockName, instanceId, expirationSeconds);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void unlock(final String lockName, final long instanceId) {
        lockingDao.releaseLock(lockName, instanceId);
    }

    @Transactional(readOnly = true)
    public LockingRepository.LockInfo getLockInfo(final String lock) {
        return lockingRepository.getLockInfo(lock);
    }
}
