package fi.livi.digitraffic.tie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MetadataApplication {
    public static void main(final String[] args) {
        SpringApplication.run(MetadataApplication.class, args);
    }
}
