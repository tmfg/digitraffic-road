package fi.livi.digitraffic.tie.conf;

import java.util.List;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;

/**
 * Provides a CacheManager bean for @DataJpaTest slice tests.
 *
 * In a full application context, Spring Boot's CacheAutoConfiguration (from spring-boot-starter-cache)
 * auto-detects Cache beans and creates a CacheManager via the "generic" cache provider.
 * However, @DataJpaTest only loads JPA-related auto-configurations and does NOT include
 * CacheAutoConfiguration. Since RoadCacheConfiguration has @EnableCaching, a CacheManager
 * must be present — otherwise the context fails with "no CacheResolver specified".
 *
 * This changed between Spring Boot 3 and 4: in Spring Boot 3, RoadCacheConfiguration
 * implemented CachingConfigurer which directly provided cache resolution without needing
 * a separate CacheManager bean. Spring Boot 4 removed CachingConfigurer, so now
 * @EnableCaching relies on finding a CacheManager bean in the context.
 */
@TestConfiguration
public class TestCacheManagerConfiguration {

    @Bean
    public CacheManager cacheManager(final List<Cache> caches) {
        final SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(caches);
        return cacheManager;
    }
}

