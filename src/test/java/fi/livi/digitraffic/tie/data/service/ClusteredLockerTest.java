package fi.livi.digitraffic.tie.data.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.dao.LockingRepository.LockInfo;
import fi.livi.digitraffic.tie.helper.ThreadUtils;
import fi.livi.digitraffic.tie.service.ClusteredLocker;
import jakarta.transaction.Transactional;

@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class ClusteredLockerTest extends AbstractServiceTest {
    private static final Logger log = LoggerFactory.getLogger(ClusteredLockerTest.class);

    private static final String LOCK = "lock";
    private static final int LOCK_EXPIRATION_S = 1;
    private static final int LOCK_EXPIRATION_DELTA_MS = 100;
    private static final int LOCK_COUNT = 3;
    private static final int THREAD_COUNT = 2;

    @Autowired
    private ClusteredLocker clusteredLocker;

    @Test
    public void testGenerate() {
        final Set<Long> ids = new HashSet<>();
        IntStream.range(0,20).forEach(i -> {
            final long id = ClusteredLocker.generateInstanceId();
            assertFalse(ids.contains(id));
            ids.add(id);
        });
    }

    @Test
    public void multipleInstancesLockingNotOverlapping() {

        final TreeSet<LockInfo> lockInfos = createLockInfoSet();

        final Collection<Future<?>> futures = new ArrayList<>();

        final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        for (int i = 1; i <= THREAD_COUNT; i++) {
            futures.add(executor.submit(new TryLocker(LOCK, clusteredLocker, lockInfos)));
        }

        while (futures.stream().anyMatch(f -> !f.isDone())) {
            ThreadUtils.delayMs(100);
        }

        assertEquals(lockInfos.size(), THREAD_COUNT * LOCK_COUNT);

        LockInfo prev = null;
        for (final LockInfo next: lockInfos) {
            if (prev != null) {
                // Check that locks won't overlap
                log.info("CHECK {} <= {}, instance {} vs {}", prev.getLockExpires(),  next.getLockLocked(), prev.getInstanceId(), next.getInstanceId());
                assertNoOverlapWithExpiration(prev, next);
                // Check that same instance won't get lock consecutively
                assertFalse(prev.getInstanceId().equals(next.getInstanceId()), "Same instance got lock consecutively");
            } else {
                log.info("START={}", next);
            }
            prev = next;
        }
    }

    @Test
    public void lockingAfterExpiration() throws InterruptedException, ExecutionException {
        final String LOCK_NAME_1 = "Lock1";
        final String LOCK_NAME_2 = "Lock2";
        final int EXPIRATION_SECONDS = 1;

        final ExecutorService executor1 = Executors.newFixedThreadPool(1);
        final ExecutorService executor2 = Executors.newFixedThreadPool(1);

        // Acquire 1. lock @ instance 1
        final Future<Boolean> futureLocked1 = tryLock(LOCK_NAME_1, EXPIRATION_SECONDS, executor1);
        waitCompletion(futureLocked1);
        final long locked1Time = System.currentTimeMillis();
        assertTrue(futureLocked1.get());

        // 2. lock can be acquired @ instance 2
        final Future<Boolean> locked2 = tryLock(LOCK_NAME_2, EXPIRATION_SECONDS, executor2);
        waitCompletion(locked2);
        assertTrue(locked2.get());

        // Try to acquire 1. lock again @ instance 2 -> fail
        Future<Boolean> locked1Second = tryLock(LOCK_NAME_1, EXPIRATION_SECONDS, executor2);
        waitCompletion(locked1Second);
        assertFalse(locked1Second.get());

        // Lock 2 can be acquired after 1 seconds
        while (!locked1Second.get()) {
            locked1Second = tryLock(LOCK_NAME_1, EXPIRATION_SECONDS, executor2);
            waitCompletion(locked1Second);

            final long now = System.currentTimeMillis();
            final double timeFromLocking = (double) (now - locked1Time) / 1000.0;
            if (timeFromLocking > 0.95 ) {
                log.info("LOCK_NAME_1 acquired: {}, time from locking {} seconds", locked1Second.get(), timeFromLocking);
            }
            if (locked1Time > (now - (EXPIRATION_SECONDS -1)*1000) ) {
                assertFalse(locked1Second.get(), "Lock acquired before expiration");
            } else if (locked1Time < (now - (EXPIRATION_SECONDS+1) * 1000) ) {
                assertTrue(locked1Second.get(), "Failed to lock after expiration");
            }
        }
    }

    @Test
    public void waitLockNotOverlapping() {
        final TreeSet<LockInfo> lockInfos = createLockInfoSet();

        final Collection<Future<?>> futures = new ArrayList<>();
        final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        for (int i = 1; i <= THREAD_COUNT; i++) {
            futures.add(executor.submit(new WaitLocker(LOCK, clusteredLocker, lockInfos)));
        }

        while (futures.stream().anyMatch(f -> !f.isDone())) {
            ThreadUtils.delayMs(100);
        }

        assertEquals(lockInfos.size(), THREAD_COUNT * LOCK_COUNT);

        LockInfo prev = null;
        for (final LockInfo next: lockInfos) {
            if (prev != null) {
                final long prevStart = prev.getLockLocked().toEpochMilli();
                final long nextStart = next.getLockLocked().toEpochMilli();
                assertTrue(nextStart >= prevStart,
                           String.format("start %s should be ge than previous %s", next.getLockLocked(), prev.getLockLocked()));
                // WaitLocker sleeps 200 ms before releasing the lock. LockService tries to lock every 100 ms, so max start time gap around 300 ms
                assertTrue(nextStart-prevStart >= 200, String.format("Between lock starts should be > 200 ms but was %d", nextStart-prevStart));
                assertTrue(nextStart-prevStart <= 700, String.format("Between lock starts should be < 700 ms but was %d", nextStart-prevStart));
                // Check that same instance won't get lock consecutively
                assertFalse(prev.getInstanceId().equals(next.getInstanceId()), "Same instance got lock consecutively");
            } else {
                log.info("START={}", next);
            }
            prev = next;
        }
    }

    private void waitCompletion(final Future<Boolean> future) {
        while(!future.isDone()) {
            ThreadUtils.delayMs(5);
        }
    }

    private TreeSet<LockInfo> createLockInfoSet() {
        return new TreeSet<>(Comparator.comparing(LockInfo::getLockLocked));
    }

    private void assertNoOverlapWithExpiration(final LockInfo prev, final LockInfo next) {
        assertTrue(prev.getLockExpires().toEpochMilli() <= next.getLockLocked().toEpochMilli(),
            "Pevious expiration should be before next locking time " + prev.getLockExpires() + " vs " + next.getLockLocked());
    }

    private Future<Boolean> tryLock(final String lockName, final int expirationSeconds,
                                    final ExecutorService executorService) {
        return executorService.submit(() -> clusteredLocker.tryLock(lockName, expirationSeconds));
    }

    private class TryLocker implements Runnable {
        private final String lock;
        private final ClusteredLocker clusteredLocker;
        private final TreeSet<LockInfo> lockInfos;

        /**
         * Acquires given lock LOCK_COUNT times
         * @param lock name of the lock
         * @param clusteredLocker the locker
         * @param lockInfos collection where to add infos for acquired locks
         */
        TryLocker(final String lock, final ClusteredLocker clusteredLocker, final TreeSet<LockInfo> lockInfos) {
            this.lock = lock;
            this.clusteredLocker = clusteredLocker;
            this.lockInfos = lockInfos;
        }

        @Override
        public void run() {
            int counter = 0;
            while (counter < LOCK_COUNT) {
                final boolean locked = clusteredLocker.tryLock(lock, LOCK_EXPIRATION_S);
                if (locked) {
                    final LockInfo info = clusteredLocker.getLockInfo(LOCK);
                    lockInfos.add(info);
                    log.info("Acquired Lock {} from {} to {} for instanceId {}", info.getLockName(), info.getLockLocked(), info.getLockExpires(), info.getInstanceId() );
                    counter++;
                    // Sleep little more than expiration time so another thread should get the lock
                    ThreadUtils.delayMs(LOCK_EXPIRATION_S * 1000 + LOCK_EXPIRATION_DELTA_MS);
                }
            }
        }
    }

    private class WaitLocker implements Runnable {
        private final String lock;
        private final ClusteredLocker clusteredLocker;
        private final TreeSet<LockInfo> lockInfos;

        /**
         * Acquires given lock LOCK_COUNT times
         * @param lock name of the lock
         * @param clusteredLocker the locker
         * @param lockInfos collection where to add infos for acquired locks
         */
        WaitLocker(final String lock, final ClusteredLocker clusteredLocker,
                   final TreeSet<LockInfo> lockInfos) {
            this.lock = lock;
            this.clusteredLocker = clusteredLocker;
            this.lockInfos = lockInfos;
        }

        @Override
        public void run() {
            int counter = 0;
            while (counter < LOCK_COUNT) {
                clusteredLocker.lock(lock, LOCK_EXPIRATION_S);
                final LockInfo info = clusteredLocker.getLockInfo(LOCK);
                lockInfos.add(info);
                log.info("Acquired Lock {} from {} to {} for instanceId {}", info.getLockName(), info.getLockLocked(), info.getLockExpires(), info.getInstanceId() );
                counter++;

                // Sleep little and then release the lock for next thread
                ThreadUtils.delayMs(200);
                clusteredLocker.unlock(lock);
                // Sleep to make sure next thread will try the lock
                ThreadUtils.delayMs(200);
            }
        }
    }
}
