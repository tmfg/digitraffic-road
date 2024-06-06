package fi.livi.digitraffic.common.service.locking;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;

import fi.livi.digitraffic.common.dao.LockingDao;
import fi.livi.digitraffic.common.util.ThreadUtil;
import fi.livi.digitraffic.tie.AbstractDaemonTest;

public class LockingServiceTest extends AbstractDaemonTest {

    private static final String LOCKNAME1 = "testLock_1";
    private static final String LOCKNAME2 = "testLock_2";

    @Autowired
    private LockingService lockingService1;

    @Autowired
    private LockingDao lockingDao;

    @Autowired
    private GenericApplicationContext applicationContext;

    private LockingService lockingService2;

    private final static String BEAN = "lockingService2";

    @BeforeEach
    public void init() {
        lockingService1.clearExpiredLocks(-1000);
        applicationContext.registerBean(BEAN, LockingService.class,
                () -> new LockingService(lockingDao, applicationContext));
        lockingService2 = applicationContext.getBean(BEAN, LockingService.class);
    }

    @AfterEach
    public void clean() {
        applicationContext.removeBeanDefinition(BEAN);
        lockingService1.clearExpiredLocks(-1000);
    }

    @Test
    public void acquireLock() {
        assertTrue(lockingService1.acquireLock(LOCKNAME1, 10));
    }

    @Test
    public void lockTwice() {
        assertTrue(lockingService1.acquireLock(LOCKNAME1, 10));
        assertTrue(lockingService1.acquireLock(LOCKNAME1, 10));
        assertFalse(lockingService2.acquireLock(LOCKNAME1, 10));
        assertFalse(lockingService2.acquireLock(LOCKNAME1, 10));
        assertTrue(lockingService1.acquireLock(LOCKNAME1, 10));
        assertFalse(lockingService2.acquireLock(LOCKNAME1, 10));
    }

    @Test
    public void secondLock() {
        assertTrue(lockingService2.acquireLock(LOCKNAME2, 10));
        assertTrue(lockingService2.acquireLock(LOCKNAME1,10));
        assertFalse(lockingService1.acquireLock(LOCKNAME2, 10));
        assertFalse(lockingService1.acquireLock(LOCKNAME1, 10));
    }

    @Test

    public void releaseLock() {
        assertTrue(lockingService1.acquireLock(LOCKNAME1, 10));
        assertTrue(lockingService1.acquireLock(LOCKNAME1, 10));

        // wrong id, won't release
        lockingService2.unlock(LOCKNAME1);
        assertFalse(lockingService2.acquireLock(LOCKNAME1, 10));

        // correct id
        lockingService1.unlock(LOCKNAME1);
        assertTrue(lockingService2.acquireLock(LOCKNAME1, 10));
    }

    @Test

    public void lockWithAnotherInstanceAfterExpiration() {
        final int expirationSeconds = 1;
        final int expirationMs = 1000 * expirationSeconds;

        // Acquire lock for ID1
        assertTrue(lockingService1.acquireLock(LOCKNAME1, expirationSeconds));
        final StopWatch locked1Time = StopWatch.createStarted();

        // Another lock can be acquired
        assertTrue(lockingService2.acquireLock(LOCKNAME2, expirationSeconds));

        // Try to acquire 1. lock with another id, but id1 is holding the lock
        assertFalse(lockingService2.acquireLock(LOCKNAME1, expirationSeconds));

        // Wait for ID2 to get the lock or expiration to pass by second
        while (!lockingService2.acquireLock(LOCKNAME1, expirationSeconds) && locked1Time.getTime() < expirationMs+1000) {
            ThreadUtil.delayMs(100);
        }

        final boolean locked = lockingService2.acquireLock(LOCKNAME1, expirationSeconds);
        if (locked && locked1Time.getTime() < expirationMs) {
            fail("Locked before expiration. Lock has been locked for " + locked1Time.getTime() + " ms and expiration is " + expirationMs + " ms");
        } else if (!locked && locked1Time.getTime() > expirationMs) {
            fail("Failed to lock after expiration. Lock has been locked for " + locked1Time.getTime() + " ms and expiration is " + expirationMs + " ms");
        }
        assertTrue(locked, "Failed to lock after expiration");
    }
}
