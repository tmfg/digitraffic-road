package fi.livi.digitraffic.tie.helper;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.dto.wazefeed.ReverseGeocode;

@Component
public class RoadCacheHelper {
    private static final String WAZE_REVERSE_GEOCODE_CACHE = "wazeReverseGeocode";
    private final CacheManager cacheManager;

    public RoadCacheHelper() {
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
        cacheManager.init();

        cacheManager.createCache(
            WAZE_REVERSE_GEOCODE_CACHE,
            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, ReverseGeocode.class, ResourcePoolsBuilder.heap(50))
        );
    }

    public Cache<String, ReverseGeocode> getWazeReverseGeocodeCache() {
        return cacheManager.getCache(WAZE_REVERSE_GEOCODE_CACHE, String.class, ReverseGeocode.class);
    }
}