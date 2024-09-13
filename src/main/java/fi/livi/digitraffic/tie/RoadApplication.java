package fi.livi.digitraffic.tie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = { "fi.livi.digitraffic.tie", "fi.livi.digitraffic.common"})
@SpringBootApplication
@EnableCaching
public class RoadApplication {
    public static void main(final String[] args) {
        SpringApplication.run(RoadApplication.class, args);
    }
}
