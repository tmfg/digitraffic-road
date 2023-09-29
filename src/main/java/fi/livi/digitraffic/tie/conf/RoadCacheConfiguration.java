package fi.livi.digitraffic.tie.conf;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class RoadCacheConfiguration implements CachingConfigurer {

    public static final String CACHE_REVERSE_GEOCODE = "reverseGeocode";
    public static final String CACHE_MAINTENANCE_ROUTES = "maintenanceTracking";
    public static final String CACHE_MAINTENANCE_ROUTES_LATES = "maintenanceTrackingLatest";
    public static final String CACHE_MAINTENANCE_DOMAIN_NAMES = "maintenanceTrackingDomainNames";

    @Bean(name = CACHE_REVERSE_GEOCODE)
    public CaffeineCache cacheGeocode(
            @Value("${cache.reverseGeocode.ms}")
            final long durationMs) {
        return createCache(CACHE_REVERSE_GEOCODE, durationMs, null);
    }

    @Bean(name = CACHE_MAINTENANCE_ROUTES)
    public CaffeineCache cacheRoutes(
            @Value("${cache.maintenance.routes.ms}")
            final long durationMs,
            @Value("${cache.maintenance.routes.size}")
            final int size) {
        return createCache(CACHE_MAINTENANCE_ROUTES, durationMs, size);
    }

    @Bean(name = CACHE_MAINTENANCE_DOMAIN_NAMES)
    public CaffeineCache cacheDomainNames(
            @Value("${cache.maintenance.domain.names.ms}")
            final long durationMs) {
        return createCache(CACHE_MAINTENANCE_DOMAIN_NAMES, durationMs, null);
    }

    @Bean(name = CACHE_MAINTENANCE_ROUTES_LATES)
    public CaffeineCache cacheRoutesLatest(
            @Value("${cache.maintenance.routes.latest.ms}")
            final long durationMs,
            @Value("${cache.maintenance.routes.latest.size}")
            final int size) {
        return createCache(CACHE_MAINTENANCE_ROUTES_LATES, durationMs, size);
    }

    private CaffeineCache createCache(final String cacheName, final long expireAfterWritedMs, final Integer size) {
        final Caffeine<Object, Object> builder =
                Caffeine.newBuilder()
                        .expireAfterWrite(expireAfterWritedMs, TimeUnit.MILLISECONDS)
                        .recordStats();
        if (size != null) {
            builder.maximumSize(size);
        }

        return new CaffeineCache(cacheName, builder.build());
    }

}