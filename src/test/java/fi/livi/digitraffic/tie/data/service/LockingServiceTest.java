package fi.livi.digitraffic.tie.data.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.transaction.Transactional;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractTest;

@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class LockingServiceTest extends AbstractTest {

    private static final Logger log = LoggerFactory.getLogger(AbstractTest.class);

    private ArrayList<Long> lockStarts = new ArrayList<>();
    private ArrayList<String> lockerInstanceIds  = new ArrayList<>();

    @Autowired
    private LockingService lockingService;

    private static final String LOCK = "lock";
    private static final int LOCK_EXPIRATION_S = 2;
    private static final int LOCK_EXPIRATION_DELTA_S = 2;
    private static final int LOCK_COUNT = 3;
    private static final int THREAD_COUNT = 2;


    @Test()
    public void testMultipleInstancesLocking() {

        ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);

        Collection<Future<?>> futures = new ArrayList<>();
        for (int i = 1; i <= THREAD_COUNT; i++) {
            futures.add(pool.submit(new Locker(LOCK, String.valueOf(i))));
        }

        while ( futures.stream().filter(f -> !f.isDone()).count() > 0 ) {
            sleep(100);
        }

        Assert.assertTrue(lockStarts.size() == THREAD_COUNT * LOCK_COUNT);

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
                Assert.assertTrue("", start >= prevStart + LOCK_EXPIRATION_S * 1000);
                Assert.assertTrue(start <= prevStart + (LOCK_EXPIRATION_S + LOCK_EXPIRATION_DELTA_S) * 1000);
            }
            prevStart = start;
        }
    }

    @Test
    public void testLockingExpirationFast() {

        final String LOCK_NAME_1 = "Lock1";
        final String INSTANCE_ID_1 = "1";
        final int EXPIRATION_SECONDS = 5;
        final String LOCK_NAME_2 = "Lock2";
        final String INSTANCE_ID_2 = "2";

        // Acquire 1. lock
        boolean locked1 = lockingService.acquireLock(LOCK_NAME_1, INSTANCE_ID_1, EXPIRATION_SECONDS);
        long locked1Time = System.currentTimeMillis();
        Assert.assertTrue(locked1);

        // Another lock can be acquired
        boolean locked2 = lockingService.acquireLock(LOCK_NAME_2, INSTANCE_ID_2, EXPIRATION_SECONDS);
        Assert.assertTrue(locked2);

        // Try to acquire 1. lock again -> fail
        boolean locked1Second = lockingService.acquireLock(LOCK_NAME_1, INSTANCE_ID_2, EXPIRATION_SECONDS);
        Assert.assertFalse(locked1Second);

        while (!locked1Second) {
            locked1Second = lockingService.acquireLock(LOCK_NAME_1, INSTANCE_ID_2, EXPIRATION_SECONDS);
            long now = System.currentTimeMillis();
            log.info("LOCK_NAME_1 acquired: " + locked1Second + ", time from locking " +  (double)(now-locked1Time)/1000.0 + " seconds" );
            if (locked1Time > (now - (EXPIRATION_SECONDS -1)*1000) ) {
                Assert.assertFalse("Lock acquired before expiration", locked1Second);
            } else if (locked1Time < (now - (EXPIRATION_SECONDS+1) * 1000) ) {
                Assert.assertTrue("Failed to lock after expiration", locked1Second);
            }
        }
    }

    private class Locker implements Runnable {

        private final String lock;
        private final String instanceId;

        Locker(final String lock, final String instanceId) {
            this.lock = lock;
            this.instanceId = instanceId;
        }

        @Override
        public void run() {
            int counter = 0;
            while (counter < LOCK_COUNT) {
                boolean locked = lockingService.acquireLock(lock, instanceId, LOCK_EXPIRATION_S);
                if (locked) {
                    log.info("Acquired Lock [{}] for: [{}]", lock, instanceId);
                    lockStarts.add(System.currentTimeMillis());
                    lockerInstanceIds.add(instanceId);
                    counter++;
                    // Sleep little more than exipration so another thread should get lock
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
