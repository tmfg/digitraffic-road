package fi.livi.digitraffic.tie.conf.kca.artemis.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jms.autoconfigure.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.scheduling.annotation.Scheduled;

import fi.livi.digitraffic.common.annotation.NoJobLogging;
import fi.livi.digitraffic.common.service.locking.CachedLockingService;
import fi.livi.digitraffic.common.service.locking.LockingService;
import fi.livi.digitraffic.common.util.StringUtil;
import jakarta.jms.ConnectionFactory;

/**
 * Custom configurations for Artemis JMS
 */
@ConditionalOnNotWebApplication
@ConditionalOnProperty(name = "kca.artemis.jms.enabled",
                       havingValue = "true")
@Configuration
@EnableJms // Enable Spring-artemis JMS auto configuration
public class ArtemisJMSConfiguration {
    private static final Logger log = LoggerFactory.getLogger(ArtemisJMSConfiguration.class);

    public static final String JMS_LISTENER_CONTAINER_FACTORY_FOR_TOPIC = "jmsListenerContainerFactoryForTopic";
    public static final String JMS_LISTENER_CONTAINER_FACTORY_FOR_QUEUE = "jmsListenerContainerFactoryForQueue";

    /**
     * Recovery interval for listener containers in milliseconds.
     * When a session/connection error occurs, the container waits this long before trying to reconnect.
     * Default Spring value is 5000ms. Using 10s gives the broker more breathing room and avoids rapid retry storms.
     */
    private static final long RECOVERY_INTERVAL_MS = 10_000;

    /**
     * Lock TTL for the JMS distributed lock in seconds.
     * The lock is refreshed every 1 second ({@link CachedLockingService#acquireLock()}).
     * With the default 2-second TTL a single DB round-trip that takes >1s causes a false lock-loss
     * which then triggers stop()->start() and disrupts all JMS consumers (DPO-4559).
     * Using 10s means the lock tolerates up to ~9 consecutive slow/failed DB refreshes
     * before it actually expires, while a real crash is detected within max 10s (acceptable).
     */
    private static final int JMS_LOCK_EXPIRATION_SECONDS = 10;

    private final JmsListenerEndpointRegistry jmsListenerEndpointRegistry;
    private final CachedLockingService lock;

    final static String LOCK_NAME = ArtemisJMSConfiguration.class.getSimpleName() + ".JMS_LOCK";

    private enum Operation {
        CONNECTING,
        DISCONNECTING
    }

    ArtemisJMSConfiguration(final JmsListenerEndpointRegistry jmsListenerEndpointRegistry,
                            final LockingService lockingService) {
        this.jmsListenerEndpointRegistry = jmsListenerEndpointRegistry;
        this.lock = lockingService.createCachedLockingService(LOCK_NAME, JMS_LOCK_EXPIRATION_SECONDS);
        log.info("method=ArtemisJMSConfiguration create {}", lock.toString());
    }

    @NoJobLogging
    @Scheduled(fixedRate = 1000)
    public void connectDisconnect() {
        if (lock.hasLock() && !jmsListenerEndpointRegistry.isRunning() && isStopCompleted()) {
            log.info("method=connectDisconnect operation={} {}", Operation.CONNECTING, lock.getLockInfoForLogging());
            jmsListenerEndpointRegistry.start();
        } else if (!lock.hasLock() && jmsListenerEndpointRegistry.isRunning()) {
            log.info("method=connectDisconnect operation={} {}", Operation.DISCONNECTING, lock.getLockInfoForLogging());
            jmsListenerEndpointRegistry.stop();
        }
    }

    /**
     * Returns true when all containers have fully drained in-flight onMessage() calls after stop().
     * Needed because isRunning() returns false immediately when stop() is called, before draining
     * completes — allowing start() to race against an in-progress stop() causing transient
     * "Session is closed" warnings.
     * getActiveConsumerCount() == 0 is the exact point DMLC itself considers stop complete.
     * Cast is guaranteed: both factories are DefaultJmsListenerContainerFactory which always
     * creates DefaultMessageListenerContainer instances.
     */
    private boolean isStopCompleted() {
        return jmsListenerEndpointRegistry.getListenerContainers().stream()
                .map(c -> (DefaultMessageListenerContainer) c)
                .allMatch(c -> c.getActiveConsumerCount() == 0);
    }

    /**
     * Spring-artemis initializes default DefaultJmsListenerContainerFactory with pub-sub-domain=false.
     * That works for queues but topics needs pub-sub-domain=true. Here we create another DefaultJmsListenerContainerFactory
     * for topics using Spring configured DefaultJmsListenerContainerFactoryConfigurer to get default values from
     * Spring configuration spring.jms.*
     *
     * @param configurer        Spring JMS configurer with default/set values in spring.jms.* properties
     * @param connectionFactory Default JMS ConnectionFactory
     * @return DefaultJmsListenerContainerFactory for topics
     */
    @Bean(name = JMS_LISTENER_CONTAINER_FACTORY_FOR_TOPIC)
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactoryForTopic(
            @Qualifier("jmsListenerContainerFactoryConfigurer")
            final DefaultJmsListenerContainerFactoryConfigurer configurer,
            @Qualifier("jmsConnectionFactory")
            final ConnectionFactory connectionFactory) {
        final DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setPubSubDomain(true); // Topic
        factory.setAutoStartup(false); // Startup only when instance has a lock
        factory.setErrorHandler(t -> log.error(StringUtil.format(
                "method=jmsListenerErrorHandler type={} Execution of JMS message listener failed.",
                "Topic"), t));
        // After a session/connection failure, wait RECOVERY_INTERVAL_MS before trying to reconnect.
        // This avoids rapid retry storms against the broker when the connection is broken.
        factory.setRecoveryInterval(RECOVERY_INTERVAL_MS);
        log.info("method=jmsListenerContainerFactory created type={} instanceId={}, connection info: {}",
                "Topic", lock.getInstanceId(), getConnectionFactoryInfo(connectionFactory));

        return factory;
    }

    /**
     * DefaultJmsListenerContainerFactory for queues
     *
     * @param configurer        Spring JMS configurer with default/set values in spring.jms.* properties
     * @param connectionFactory Default JMS ConnectionFactory
     * @return DefaultJmsListenerContainerFactory for queue
     */
    @Bean(name = JMS_LISTENER_CONTAINER_FACTORY_FOR_QUEUE)
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactoryForQueue(
            @Qualifier("jmsListenerContainerFactoryConfigurer")
            final DefaultJmsListenerContainerFactoryConfigurer configurer,
            @Qualifier("jmsConnectionFactory")
            final ConnectionFactory connectionFactory) {
        final DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setPubSubDomain(false); // Queue
        factory.setAutoStartup(false); // Startup only when instance has a lock
        factory.setErrorHandler(t -> log.error(StringUtil.format(
                "method=jmsListenerErrorHandler type={} Execution of JMS message listener failed.",
                "Queue"), t));
        factory.setRecoveryInterval(RECOVERY_INTERVAL_MS);
        log.info("method=jmsListenerContainerFactory created type={} instanceId={}, connection info: {}",
                "Queue", lock.getInstanceId(), getConnectionFactoryInfo(connectionFactory));

        return factory;
    }

    private String getConnectionFactoryInfo(final ConnectionFactory connectionFactory) {
        try {
            final ConnectionFactory cf = ((CachingConnectionFactory) connectionFactory).getTargetConnectionFactory();
            if (cf != null) {
                // Replace key=value -pairs with key = value to not include them in search indexing
                return cf.toString().replace("=", " = ");
            }
        } catch (final Exception e) {
            log.error("method=getConnectionFactoryInfo Could not get connection factory info", e);
        }
        return "Could not get connection factory info";
    }
}
