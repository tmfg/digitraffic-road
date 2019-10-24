package fi.livi.digitraffic.tie.data.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LockingService {

    private static final Logger log = LoggerFactory.getLogger(LockingService.class);
    private LockingServiceInternal lockingServiceInternal;

    public LockingService(final LockingServiceInternal lockingServiceInternal) {
        this.lockingServiceInternal = lockingServiceInternal;
    }

    /**
     * Acquires the lock.
     *
     * <p>If the lock is not available then the current thread will sleep
     * until the lock has been acquired.
     *
     * @param lockName lock name to acquire
     * @param expirationSeconds How long will lock stay valid if it is not refreshed or released.
     */
    public void lock(final String lockName, final int expirationSeconds) {
        while (!tryLock(lockName, expirationSeconds)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error("method=lock Error sleep interrupted", e);
                throw new RuntimeException(e);
            }
        }
    }
    /**
     * Acquires the lock only if it is free at the time of invocation.
     *
     * <p>Acquires the lock if it is available and returns immediately
     * with the value {@code true}.
     * If the lock is not available then this method will return
     * immediately with the value {@code false}.
     *
     * @param lockName lock name to acquire
     * @param expirationSeconds How long will lock stay valid if it is not refreshed or released.
     * @return {@code true} if the lock was acquired and
     *         {@code false} otherwise
     */
    public boolean tryLock(final String lockName, final int expirationSeconds) {
        return lockingServiceInternal.tryLock(lockName, expirationSeconds);
    }

    /**
     * Releases the lock.
     *
     * @param lockName lock name to acquire
     */
    public void unlock(final String lockName) {
        lockingServiceInternal.unlock(lockName);
    }

    /**
     * Unique instance id in multi node environment.
     *
     * @return node instance id
     */
    public long getThreadId() {
        return lockingServiceInternal.getThreadId();
    }
}
