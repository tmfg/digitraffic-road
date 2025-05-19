package fi.livi.digitraffic.common.dao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import fi.livi.digitraffic.common.util.ThreadUtil;
import fi.livi.digitraffic.tie.AbstractDaemonTest;
import jakarta.transaction.Transactional;

public class LockingDaoTest extends AbstractDaemonTest {

    private static final String LOCKNAME1 = "testLock_1";
    private static final String LOCKNAME2 = "testLock_2";
    private static final String ID1 = "test_id_1";
    private static final String ID2 = "test_id_2";

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    private LockingDao lockingDao;

    @BeforeEach
    @Transactional
    public void removeLocks() {
        jdbcTemplate.execute("delete from locking_table");
    }

    @Test
    public void lockOnce() {
        assertTrue(lockingDao.acquireLock(LOCKNAME1, ID1, 10));
        assertTrue(lockingDao.hasLock(LOCKNAME1, ID1));
    }

    @Test
    public void lockTwice() {
        assertTrue(lockingDao.acquireLock(LOCKNAME1, ID1, 10));
        assertTrue(lockingDao.hasLock(LOCKNAME1, ID1));
        assertFalse(lockingDao.hasLock(LOCKNAME1, ID2));

        assertFalse(lockingDao.acquireLock(LOCKNAME1, ID2, 10));
        assertTrue(lockingDao.hasLock(LOCKNAME1, ID1));
        assertFalse(lockingDao.hasLock(LOCKNAME1, ID2));
    }

    @Test
    public void secondLock() {
        assertTrue(lockingDao.acquireLock(LOCKNAME2, ID2, 10));
        assertFalse(lockingDao.hasLock(LOCKNAME1, ID2));
        assertFalse(lockingDao.hasLock(LOCKNAME2, ID1));
    }

    @Test
    public void releaseLock() {
        assertTrue(lockingDao.acquireLock(LOCKNAME1, ID1, 10));
        assertTrue(lockingDao.hasLock(LOCKNAME1, ID1));

        // wrong id, won't release
        lockingDao.releaseLock(LOCKNAME1, ID2);
        assertTrue(lockingDao.hasLock(LOCKNAME1, ID1));

        // correct id
        lockingDao.releaseLock(LOCKNAME1, ID1);
        assertFalse(lockingDao.hasLock(LOCKNAME1, ID1));
    }

    @Test
    public void lockWithAnotherInstanceAfterExpiration() {
        final int expirationSeconds = 1;
        final int expirationMs = 1000 * expirationSeconds;

        // Acquire lock for ID1
        assertTrue(lockingDao.acquireLock(LOCKNAME1, ID1, expirationSeconds));
        final StopWatch locked1Time = StopWatch.createStarted();
        final boolean locked1 = lockingDao.hasLock(LOCKNAME1, ID1);
        assertTrue(locked1);

        // Another lock can be acquired
        assertTrue(lockingDao.acquireLock(LOCKNAME2, ID1, expirationSeconds));

        // Try to acquire 1. lock with another id, but id1 is holding the lock
        assertFalse(lockingDao.acquireLock(LOCKNAME1, ID2, expirationSeconds));

        // Wait for ID2 to get the lock or expiration to pass by second
        while (!lockingDao.acquireLock(LOCKNAME1, ID2, expirationSeconds) && locked1Time.getDuration().toMillis() < expirationMs+1000) {
            ThreadUtil.delayMs(100);
        }

        final boolean locked = lockingDao.acquireLock(LOCKNAME1, ID2, expirationSeconds);
        if (locked && locked1Time.getDuration().toMillis() < expirationMs) {
            fail("Locked before expiration. Lock has been locked for " + locked1Time.getDuration().toMillis() + " ms and expiration is " + expirationMs + " ms");
        } else if (!locked && locked1Time.getDuration().toMillis() > expirationMs) {
            fail("Failed to lock after expiration. Lock has been locked for " + locked1Time.getDuration().toMillis() + " ms and expiration is " + expirationMs + " ms");
        }
        assertTrue(locked, "Failed to lock after expiration");
    }
}
