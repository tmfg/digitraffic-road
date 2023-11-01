package fi.livi.digitraffic.tie.conf.metrics;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import com.github.benmanes.caffeine.cache.stats.CacheStats;

import fi.livi.digitraffic.tie.aop.NoJobLogging;

@Configuration
public class CacheStatisticsLoggerConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CacheStatisticsLoggerConfiguration.class);
    private final CacheManager cacheManager;
    protected static ThreadLocal<DecimalFormat> f =
            ThreadLocal.withInitial(() -> {
                final DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(Locale.US);
                df.applyLocalizedPattern("#0.00");
                return df;
            });

    private final Map<String, CacheStatisticsLogger> statsCounters = new ConcurrentHashMap<>();

    @Autowired
    public CacheStatisticsLoggerConfiguration(final CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Scheduled(fixedRate = 60000)
    @NoJobLogging
    void cacheStats() {
        if (cacheManager == null) {
            log.error("method=cacheStats Failed to log cache stats as cacheManager is null");
            return;
        }
        try {
            cacheManager.getCacheNames().forEach(cn -> updateStatsAndLog(cn, ((CaffeineCache) Objects.requireNonNull(cacheManager.getCache(cn))).getNativeCache().stats()));
        } catch (final Exception e) {
            log.error("method=cacheStats Failed to log cache stats", e);
        }
    }

    private void updateStatsAndLog(final String cacheName, final CacheStats statsSnapshot) {
        if (!statsCounters.containsKey(cacheName)) {
            statsCounters.put(cacheName, new CacheStatisticsLogger(cacheName));
        }
        final CacheStatisticsLogger counter = statsCounters.get(cacheName);
        counter.update(statsSnapshot);
        counter.logCacheStats();
    }

    private static class CacheStatisticsLogger {
        private static final Logger log = LoggerFactory.getLogger(CacheStatisticsLogger.class);
        private final String cacheName;
        private CacheStats previous = CacheStats.empty();
        private CacheStats latest = CacheStats.empty();

        public CacheStatisticsLogger(final String cacheName) {
            Objects.requireNonNull(cacheName);
            this.cacheName = cacheName;
        }

        public void update(final CacheStats latestSnapshot) {
            Objects.requireNonNull(latestSnapshot);
            this.previous = this.latest;
            this.latest = latestSnapshot;
        }

        public String getCacheName() {
            return cacheName;
        }

        public CacheStats countStats() {
            return previous != null ? latest.minus(previous) : latest;
        }

        public void logCacheStats() {
            final CacheStats s = countStats();
            log.info("method=logCacheStats cacheName={} hitCount={} missCount={} hitRate={} missRate={} evictionCount={} loadCount={} averageLoadPenaltyMs={}",
                     getCacheName(),  s.hitCount(), s.missCount(), f.get().format(s.hitRate()), f.get().format(s.missRate()), s.evictionCount(), s.loadCount(), (long)s.averageLoadPenalty()/1000000); // ns -> ms
        }

        @Override
        public int hashCode() {
            return Objects.hash(cacheName);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final CacheStatisticsLogger that = (CacheStatisticsLogger) o;
            return Objects.equals(cacheName, that.cacheName);
        }
    }
}