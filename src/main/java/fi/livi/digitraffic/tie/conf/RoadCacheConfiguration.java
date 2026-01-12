package fi.livi.digitraffic.tie.conf;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

import java.util.concurrent.TimeUnit;

// Fixes RoadCacheConfiguration is not eligible for getting processed by all BeanPostProcessors
// This doesn't need to be processed or e.g. proxied, so marking it as infrastructure role
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Configuration
@EnableCaching
public class RoadCacheConfiguration implements CachingConfigurer {

    public static final String CACHE_REVERSE_GEOCODE = "cacheReverseGeocode";
    public static final String CACHE_MAINTENANCE_ROUTES = "cacheMaintenanceTracking";
    public static final String CACHE_MAINTENANCE_ROUTES_LATES = "cacheMaintenanceTrackingLatest";
    public static final String CACHE_MAINTENANCE_DOMAIN_NAMES = "cacheMaintenanceTrackingDomainNames";
    public static final String CACHE_FREE_FLOW_SPEEDS = "cacheFreeFlowSpeeds";
    public static final String CACHE_BEARING = "cacheBearing";
    public static final String CACHE_THUMBNAILS = "cacheThumbnails";


    @Bean(name = CACHE_REVERSE_GEOCODE)
    public CaffeineCache cacheGeocode(
            @Value("${cache.reverseGeocode.ms}") final long durationMs) {
        return createCache(CACHE_REVERSE_GEOCODE, durationMs, null);
    }

    @Bean(name = CACHE_MAINTENANCE_ROUTES)
    public CaffeineCache cacheRoutes(
            @Value("${cache.maintenance.routes.ms}") final long durationMs,
            @Value("${cache.maintenance.routes.size}") final int size) {
        return createCache(CACHE_MAINTENANCE_ROUTES, durationMs, size);
    }

    @Bean(name = CACHE_MAINTENANCE_DOMAIN_NAMES)
    public CaffeineCache cacheDomainNames(
            @Value("${cache.maintenance.domain.names.ms}") final long durationMs) {
        return createCache(CACHE_MAINTENANCE_DOMAIN_NAMES, durationMs, null);
    }

    @Bean(name = CACHE_MAINTENANCE_ROUTES_LATES)
    public CaffeineCache cacheRoutesLatest(
            @Value("${cache.maintenance.routes.latest.ms}") final long durationMs,
            @Value("${cache.maintenance.routes.latest.size}") final int size) {
        return createCache(CACHE_MAINTENANCE_ROUTES_LATES, durationMs, size);
    }

    @Bean(name = CACHE_FREE_FLOW_SPEEDS)
    public CaffeineCache cacheFreeFlowSpeeds(
            @Value("${cache.free-flow-speeds.ms}") final long durationMs,
            @Value("${cache.free-flow-speeds.size}") final int size) {
        return createCache(CACHE_FREE_FLOW_SPEEDS, durationMs, size);
    }

    @Bean(name = CACHE_BEARING)
    public CaffeineCache cacheBearing() {
        // 24 hour cache should be good
        return createCache(CACHE_BEARING, 1000 * 60 * 60 * 24, 2000);
    }

    @Bean(name = CACHE_THUMBNAILS)
    public CaffeineCache cacheThumbnails() {
        // 15 minutes as camera images update around every 10 minutes
        return createCache(CACHE_THUMBNAILS, 1000 * 60 * 15, 5000, true);
    }

    private CaffeineCache createCache(final String cacheName, final long expireAfterWritedMs, final Integer size) {
        return createCache(cacheName, expireAfterWritedMs, size, false);
    }

    private CaffeineCache createCache(final String cacheName, final long expireAfterWritedMs, final Integer size, final boolean async) {
        final Caffeine<Object, Object> builder =
                Caffeine.newBuilder()
                        .expireAfterWrite(expireAfterWritedMs, TimeUnit.MILLISECONDS)
                        .recordStats();
        if (size != null) {
            builder.maximumSize(size);
        }
        if (async) {
            return new CaffeineCache(cacheName, builder.buildAsync(), true);
        }
        return new CaffeineCache(cacheName, builder.build());
    }

}
