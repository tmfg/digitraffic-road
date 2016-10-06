package fi.livi.digitraffic.tie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import fi.livi.digitraffic.tie.annotation.CoverageIgnore;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@ComponentScan(basePackages = "fi.livi.digitraffic.tie")
public class MetadataApplication {
    @CoverageIgnore
    public static void main(final String[] args) {
        SpringApplication.run(MetadataApplication.class, args);
    }
}
