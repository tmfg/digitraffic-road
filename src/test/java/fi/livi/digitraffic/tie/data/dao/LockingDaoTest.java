package fi.livi.digitraffic.tie.data.dao;

import javax.transaction.Transactional;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.MetadataApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MetadataApplication.class,
    properties = { "config.test=true" },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LockingDaoTest {
    private static final Logger log = LoggerFactory.getLogger(LockingDao.class);

    private static final String LOCK_NAME_1 = "Lock1";
    private static final String INSTANCE_ID_1 = "1";
    private static final int EXPIRATION_SECONDS = 5;
    private static final String LOCK_NAME_2 = "Lock2";
    private static final String INSTANCE_ID_2 = "2";

    @Autowired
    private LockingDao lockingDao;

    @Test
    public void testLockingAfterExpiration() {
        // Acquire 1. lock
        final boolean locked1 = acquireLock(LOCK_NAME_1, INSTANCE_ID_1);
        long locked1Time = System.currentTimeMillis();
        logLockingTime(LOCK_NAME_1, locked1, locked1Time, System.currentTimeMillis());
        Assert.assertTrue(locked1);

        // Another lock can be acquired
        final boolean locked2 = acquireLock(LOCK_NAME_2, INSTANCE_ID_2);
        logLockingTime(LOCK_NAME_2, locked2, locked1Time, System.currentTimeMillis());
        Assert.assertTrue(locked2);

        // Try to acquire 1. lock again
        boolean locked1Second = acquireLock(LOCK_NAME_1, INSTANCE_ID_2);
        logLockingTime(LOCK_NAME_1, locked1Second, locked1Time, System.currentTimeMillis());
        Assert.assertFalse(locked1Second);

        while (!locked1Second) {
            locked1Second = acquireLock(LOCK_NAME_1, INSTANCE_ID_2);
            long now = System.currentTimeMillis();
            logLockingTime(LOCK_NAME_1, locked1Second, locked1Time, now);

            if (locked1Time > (now - (EXPIRATION_SECONDS -1)*1000) ) {
                Assert.assertFalse("Lock acquired before expiration", locked1Second);
            } else if (locked1Time < (now - (EXPIRATION_SECONDS+1) * 1000) ) {
                Assert.assertTrue("Failed to lock after expiration", locked1Second);
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.debug("Interrupted", e);
            }
        }
    }

    @Transactional
    private boolean acquireLock(final String lockName, final String instanceId) {
        return lockingDao.acquireLock(lockName, instanceId, EXPIRATION_SECONDS);
    }

    private void logLockingTime(final String lockName, final boolean locked, final long lockedTime, final long now) {
        log.info("{} acquired: {}, time from locking {} seconds", lockName, locked, (double)(now-lockedTime)/1000.0);
    }

    @Test
    public void testLockingAndRelasing() {
        StopWatch start = StopWatch.createStarted();
        // Acquire 1. lock
        boolean locked1 = lockingDao.acquireLock(LOCK_NAME_1, INSTANCE_ID_1, EXPIRATION_SECONDS);
        Assert.assertTrue(locked1);

        // Try to acquire 1. lock again with other instance
        boolean locked1Second = lockingDao.acquireLock(LOCK_NAME_1, INSTANCE_ID_2, EXPIRATION_SECONDS);
        Assert.assertFalse(locked1Second);

        // release lock
        lockingDao.releaseLock(LOCK_NAME_1, INSTANCE_ID_1);

        // Try to acquire 1. lock again
        boolean locked1Third = lockingDao.acquireLock(LOCK_NAME_1, INSTANCE_ID_2, EXPIRATION_SECONDS);
        Assert.assertTrue(locked1Third);
        Assert.assertTrue("Test must be run under 5 seconds", start.getTime() < 5000L);
    }
}
