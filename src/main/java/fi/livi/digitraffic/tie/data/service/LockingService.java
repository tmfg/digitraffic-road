package fi.livi.digitraffic.tie.data.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.LockingDao;

@Service
public class LockingService {

    private final LockingDao lockingDao;

    @Autowired
    public LockingService(LockingDao lockingDao) {
        this.lockingDao = lockingDao;
    }

    @Transactional
    public boolean aquireLock(final String lockName, final String callerInstanceId, int expirationSeconds) {
        return lockingDao.aquireLock(lockName, callerInstanceId, expirationSeconds);
    }

    @Transactional
    public void relaseLock(final String lockName, final String callerInstanceId) {
        lockingDao.relaseLock(lockName, callerInstanceId);
    }

}
