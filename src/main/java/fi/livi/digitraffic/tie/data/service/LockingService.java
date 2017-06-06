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
    public boolean acquireLock(final String lockName, final String callerInstanceId, int expirationSeconds) {
        return lockingDao.acquireLock(lockName, callerInstanceId, expirationSeconds);
    }
}
