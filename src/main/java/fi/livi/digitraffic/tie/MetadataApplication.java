package fi.livi.digitraffic.tie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

import fi.livi.digitraffic.tie.annotation.CoverageIgnore;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class MetadataApplication {
    @CoverageIgnore
    public static void main(final String[] args) {
        SpringApplication.run(MetadataApplication.class, args);
    }

    @Configuration
    @Profile("localhost")
    //@ComponentScan(lazyInit = true)
    static class LocalConfig {
    }
}
