package fi.livi.digitraffic.common.service.locking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import fi.livi.digitraffic.common.dao.LockingDao;
import fi.livi.digitraffic.common.util.ThreadUtil;
import fi.livi.digitraffic.tie.AbstractDaemonTest;
import jakarta.transaction.Transactional;

@TestPropertySource(properties = { "dt.scheduled.annotation.enabled=true" })
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class CachedLockingServiceTest extends AbstractDaemonTest {
    private static final Logger log = LoggerFactory.getLogger(CachedLockingServiceTest.class);

    private static final String LOCK_NAME = "lock";
    private static final int KEEP_LOCK_MS = 1000;
    private static final int LOCKING_COUNT = 3;
    private static final int THREAD_COUNT = 2;

    @Autowired
    private LockingDao lockingDao;
    @Autowired
    private GenericApplicationContext applicationContext;

    private CachedLockingService cachedLockingService1;
    private CachedLockingService cachedLockingService2;
    private CachedLockingService cachedLockingService3;

    @BeforeEach
    public void init() {
        // For test, we need to create manually LockingServices for each cluster.
        // We need to clean up the used lock name
        cachedLockingService1 = createCachedLockingServiceBean("1");
        cachedLockingService2 = createCachedLockingServiceBean("2");
        cachedLockingService3 = createCachedLockingServiceBean("3");
    }

    private CachedLockingService createCachedLockingServiceBean(final String beanSuffix) {
        final CachedLockingService cls =
                new LockingService(lockingDao, applicationContext).createCachedLockingService(LOCK_NAME,
                        "lockingService_" + beanSuffix);
        clearCachedLockingServiceUsedLockNames();
        return cls;
    }

    @AfterEach
    public void cleanup() {
        cachedLockingService1.deactivate();
        cachedLockingService2.deactivate();
        cachedLockingService3.deactivate();
        lockingDao.clearExpiredLocks(-10);
        clearCachedLockingServiceUsedLockNames();
    }

    private static void clearCachedLockingServiceUsedLockNames() {
        final Class<CachedLockingService> a = CachedLockingService.class;
        @SuppressWarnings("unchecked")
        final Set<String> bookedLockNames = (Set<String>) ReflectionTestUtils.getField(a, "bookedLockNames");
        if (bookedLockNames != null) {
            bookedLockNames.clear();
        }
    }

    @Test
    public void uniqueIds() {
        final Set<String> ids = new HashSet<>();
        IntStream.range(0, 20).forEach(i -> {
            final String id = new LockingService(lockingDao, applicationContext).getInstanceId();
            assertFalse(ids.contains(id));
            ids.add(id);
        });
    }

    @Test
    public void multipleInstancesLockingNotOverlapping() {

        final Collection<Future<?>> futures = new ArrayList<>();
        final AtomicInteger aquiredLocks = new AtomicInteger(0);
        final AtomicInteger aquiredLocksSum = new AtomicInteger(0);

        final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        futures.add(executor.submit(new TryLocker(cachedLockingService1, aquiredLocks, aquiredLocksSum)));
        futures.add(executor.submit(new TryLocker(cachedLockingService2, aquiredLocks, aquiredLocksSum)));
        futures.add(executor.submit(new TryLocker(cachedLockingService3, aquiredLocks, aquiredLocksSum)));

        while (futures.stream().anyMatch(f -> !f.isDone())) {
            ThreadUtil.delayMs(100);
        }

        assertEquals(aquiredLocksSum.get(), LOCKING_COUNT * 3);
    }

    private static class TryLocker implements Runnable {
        private final CachedLockingService lock;
        private final AtomicInteger aquiredLocks;
        private final AtomicInteger aquiredLocksSum;

        /**
         * Acquires given lock LOCK_COUNT times
         *
         * @param cachedLockingService lock to use
         * @param aquiredLocks         count how many locks has the lock at the time. Should be always 0 or 1.
         */
        TryLocker(final CachedLockingService cachedLockingService, final AtomicInteger aquiredLocks,
                  final AtomicInteger aquiredLocksSum) {
            this.lock = cachedLockingService;
            clearCachedLockingServiceUsedLockNames();
            this.aquiredLocks = aquiredLocks;
            this.aquiredLocksSum = aquiredLocksSum;
        }

        @Override
        public void run() {
            int counter = 0;
            while (counter < LOCKING_COUNT) {
                final boolean locked = lock.hasLock();

                if (locked) {
                    log.info("LOCKED lockedCount={} {}", counter, lock.getLockInfoForLogging());
                    assertEquals(1, aquiredLocks.incrementAndGet());
                    aquiredLocksSum.incrementAndGet();
                    counter++;

                    // Sleep and release lock
                    ThreadUtil.delayMs(KEEP_LOCK_MS);
                    assertEquals(0, aquiredLocks.decrementAndGet());
                    lock.deactivate();
                    // Give time for other threads too to get the lock
                    ThreadUtil.delayMs(100);
                }
            }
            log.info("DONE lockedCount={} {}", counter, lock.getLockInfoForLogging());
        }
    }

}
