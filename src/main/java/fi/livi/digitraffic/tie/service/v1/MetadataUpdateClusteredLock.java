package fi.livi.digitraffic.tie.service.v1;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.livi.digitraffic.tie.service.ClusteredLocker;

public class MetadataUpdateClusteredLock {
    private static final Logger log = LoggerFactory.getLogger(MetadataUpdateClusteredLock.class);
    private final String lockName;
    private final StopWatch stopWatch;
    private final ClusteredLocker clusteredLocker;


    public MetadataUpdateClusteredLock(final ClusteredLocker clusteredLocker, final String lockName) {
        this.clusteredLocker = clusteredLocker;
        this.lockName = lockName;
        stopWatch = new StopWatch();
    }

    public void lock() {
        clusteredLocker.lock(lockName, 10000);
        stopWatch.start();
    }

    public void unlock() {
        final long time = stopWatch.getTime();
        stopWatch.reset();
        clusteredLocker.unlock(lockName);
        log.debug("method=unlock lockName={} lockedTimeMs={}", lockName, time);
    }

}
