package fi.livi.digitraffic.tie.data.service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.transaction.Transactional;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import fi.livi.digitraffic.tie.AbstractJpaTest;
import fi.livi.digitraffic.tie.data.dao.LockingDao;

@Transactional(Transactional.TxType.NOT_SUPPORTED)
@Import(LockingDao.class)
public class LockingServiceTest extends AbstractJpaTest {
    private static final Logger log = LoggerFactory.getLogger(LockingServiceTest.class);

    private List<Long> lockStarts = new CopyOnWriteArrayList<>();
    private List<String> lockerInstanceIds  = new CopyOnWriteArrayList<>();

    private static final String LOCK = "lock";
    private static final int LOCK_EXPIRATION_S = 2;
    private static final int LOCK_EXPIRATION_DELTA_S = 2;
    private static final int LOCK_COUNT = 3;
    private static final int THREAD_COUNT = 2;

    private static final int LOCKING_TIME_EXTRA = 20; // how long it takes after obtaining lock to get timestamp?

    @Autowired
    private LockingDao lockingDao;

    @Test
    public void testMultipleInstancesLocking() {

        final ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);
        final Collection<Future<?>> futures = new ArrayList<>();

        for (int i = 1; i <= THREAD_COUNT; i++) {
            final LockingService lockingService = new LockingService(lockingDao);
            futures.add(pool.submit(new Locker(LOCK, lockingService)));
        }

        while (futures.stream().anyMatch(f -> !f.isDone())) {
            sleep(100);
        }

        Assert.assertEquals(lockStarts.size(), THREAD_COUNT * LOCK_COUNT);

        Long prev = null;
        for (Long start: lockStarts) {
            if (prev != null) {
                log.info("START={} DIFF={} s", start, (double)(start-prev)/1000.0);
            } else {
                log.info("START={}", start);
            }
            prev = start;
        }

        // Check that same instance won't get lock consecutively
        String prevInstance = null;
        for (String instance: lockerInstanceIds) {
            if (prev != null) {
                Assert.assertTrue("Same instance got lock consecutively", !instance.equals(prevInstance));
            }
            prevInstance = instance;
        }

        // Check that next lock is acquire after expiration
        Long prevStart = null;
        for (Long start: lockStarts) {
            if (prevStart != null) {
                // Check that locks won't overlap
                assertNoOverlap(start, prevStart);
            }
            prevStart = start;
        }
    }

    private void assertNoOverlap(final Long start, final Long prevStart) {
        ZonedDateTime startZdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(start), ZoneOffset.UTC);
        final long startLimit = prevStart + LOCK_EXPIRATION_S * 1000 - LOCKING_TIME_EXTRA;
        Assert.assertTrue(String.format("start %s should be ge than %s", startZdt,  ZonedDateTime.ofInstant(Instant.ofEpochMilli(startLimit), ZoneOffset.UTC)),
            start >= startLimit);

        final long endLimit = prevStart + (LOCK_EXPIRATION_S + LOCK_EXPIRATION_DELTA_S) * 1000;
        Assert.assertTrue(String.format("Start %s should be le than %s", startZdt, ZonedDateTime.ofInstant(Instant.ofEpochMilli(endLimit), ZoneOffset.UTC)),
            start <= endLimit);
    }

    @Test
    public void testLockingAfterExpiration() throws InterruptedException {
        final String LOCK_NAME_1 = "Lock1";
        final String LOCK_NAME_2 = "Lock2";
        final int EXPIRATION_SECONDS = 5;

        final LockingService lockingService1 = new LockingService(lockingDao);
        final LockingService lockingService2 = new LockingService(lockingDao);

        // Acquire 1. lock @ instance 1
        boolean locked1 = lockingService1.acquireLock(LOCK_NAME_1, EXPIRATION_SECONDS);
        long locked1Time = System.currentTimeMillis();
        Assert.assertTrue(locked1);

        // Another lock can be acquired @ instance 2
        boolean locked2 = lockingService1.acquireLock(LOCK_NAME_2, EXPIRATION_SECONDS);
        Assert.assertTrue(locked2);

        // Try to acquire 1. lock again @ instance 2 -> fail
        boolean locked1Second = lockingService2.acquireLock(LOCK_NAME_1, EXPIRATION_SECONDS);
        Assert.assertFalse(locked1Second);

        // Lock 2 can be acquired after 5 seconds
        while (!locked1Second) {
            locked1Second = lockingService2.acquireLock(LOCK_NAME_1, EXPIRATION_SECONDS);
            long now = System.currentTimeMillis();
            final double timeFromLocking = (double) (now - locked1Time) / 1000.0;
            if (timeFromLocking > 4.95 ) {
                log.info("LOCK_NAME_1 acquired: {}, time from locking {} seconds", locked1Second, timeFromLocking);
            }
            if (locked1Time > (now - (EXPIRATION_SECONDS -1)*1000) ) {
                Assert.assertFalse("Lock acquired before expiration", locked1Second);
            } else if (locked1Time < (now - (EXPIRATION_SECONDS+1) * 1000) ) {
                Assert.assertTrue("Failed to lock after expiration", locked1Second);
            }
        }
    }

    private class Locker implements Runnable {
        private final String lock;
        private final LockingService lockingService;

        Locker(final String lock, final LockingService lockingService) {
            this.lock = lock;
            this.lockingService = lockingService;
        }

        @Override
        public void run() {
            int counter = 0;
            while (counter < LOCK_COUNT) {
                final boolean locked = lockingService.acquireLock(lock, LOCK_EXPIRATION_S);
                synchronized(LOCK) {
                    final long timestamp = System.currentTimeMillis();
                    if (locked) {
                        log.info("Acquired Lock=[{}] for instanceId=[{}] at {}", lock, lockingService.getInstanceId(), ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC));
                        lockStarts.add(timestamp);
                        lockerInstanceIds.add(lockingService.getInstanceId());
                        counter++;
                    }
                }
                if (locked) {
                    // Sleep little more than expiration time so another thread should get the lock
                    sleep((LOCK_EXPIRATION_S + LOCK_EXPIRATION_DELTA_S) * 1000);
                }
            }
        }
    }

    private void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error("Sleep error" , e);
        }
    }

}
