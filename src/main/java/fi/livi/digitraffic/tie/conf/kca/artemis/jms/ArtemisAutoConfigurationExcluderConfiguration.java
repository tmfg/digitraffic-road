package fi.livi.digitraffic.tie.conf.kca.artemis.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.artemis.autoconfigure.ArtemisAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Excludes Artemis auto-configuration when kca.artemis.jms.enabled is not true.
 * This prevents Spring Boot from trying to create the JMS connection factory
 * when Artemis is not needed (e.g. local development without a broker).
 */
@Configuration
@ConditionalOnProperty(name = "kca.artemis.jms.enabled", havingValue = "false", matchIfMissing = true)
@EnableAutoConfiguration(exclude = ArtemisAutoConfiguration.class)
public class ArtemisAutoConfigurationExcluderConfiguration {
    private static final Logger log = LoggerFactory.getLogger(ArtemisAutoConfigurationExcluderConfiguration.class);

    ArtemisAutoConfigurationExcluderConfiguration(final Environment env) {
        final String value = env.getProperty("kca.artemis.jms.enabled");
        log.info("method=ArtemisAutoConfigurationExcluder Artemis JMS is disabled, excluding ArtemisAutoConfiguration. kca.artemis.jms.enabled={}",
                value != null ? value : "<not set>");
    }
}
