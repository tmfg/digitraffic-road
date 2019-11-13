package fi.livi.digitraffic.tie.conf;

import java.util.Arrays;

import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableCaching
public class RoadCacheConfiguration implements CacheManagerCustomizer<ConcurrentMapCacheManager> {

    public static final String FLYWAY_VERSION_CACHE = "FLYWAY_VERSION_CACHE";

    @CacheEvict(allEntries = true, cacheNames = { FLYWAY_VERSION_CACHE })
    @Scheduled(fixedDelay = 5 * 60 * 1000) // 5 min
    public void cacheEvict() {
        // Empty. Purpose is only to clear cache ever 5 minutes
    }

    @Override
    public void customize(ConcurrentMapCacheManager cacheManager) {
        cacheManager.setCacheNames(Arrays.asList(FLYWAY_VERSION_CACHE));
    }
}