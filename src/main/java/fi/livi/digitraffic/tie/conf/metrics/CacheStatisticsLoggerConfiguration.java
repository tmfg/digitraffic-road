package fi.livi.digitraffic.tie.conf.metrics;

import java.text.DecimalFormat;
import java.util.Objects;

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

    private final DecimalFormat f = new DecimalFormat("#0.00");

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
            cacheManager.getCacheNames().forEach(cn -> {
                final CacheStats s = ((CaffeineCache) Objects.requireNonNull(cacheManager.getCache(cn))).getNativeCache().stats();
                log.info("method=cacheStats cacheName={} hitCount={} missCount={} hitRate={} missRate={} evictionCount={} averageLoadPenaltyMs={}",
                    cn, s.hitCount(), s.missCount(), f.format(s.hitRate()), f.format(s.missRate()), s.evictionCount(), (long)s.averageLoadPenalty()/1000000); // ns -> ms
            });
        } catch (final Exception e) {
            log.error("method=cacheStats Failed to log cache stats", e);
        }
    }
}