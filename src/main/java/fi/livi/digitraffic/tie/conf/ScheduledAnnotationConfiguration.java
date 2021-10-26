package fi.livi.digitraffic.tie.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Purpose of this class is just to have possibility for tests to
 * deactivated the @scheduled jobs in the tests.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(
    name = "dt.scheduled.annotation.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class ScheduledAnnotationConfiguration {
    private static final Logger log = LoggerFactory.getLogger(ScheduledAnnotationConfiguration.class);

    public ScheduledAnnotationConfiguration() {
        // empty, just to init @EnableScheduling annotation
        log.info("Scheduled annotation enabled");
    }
}