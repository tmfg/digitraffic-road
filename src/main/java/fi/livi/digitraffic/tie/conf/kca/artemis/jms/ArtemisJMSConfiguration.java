package fi.livi.digitraffic.tie.conf.kca.artemis.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.connection.CachingConnectionFactory;
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
        this.lock = lockingService.createCachedLockingService(LOCK_NAME);
        log.info("method=ArtemisJMSConfiguration create {}", lock.toString());
    }

    @NoJobLogging
    @Scheduled(fixedRate = 1000)
    public void connectDisconnect() {
        if (lock.hasLock() && !jmsListenerEndpointRegistry.isRunning()) {
            log.info("method=connectDisconnect operation={} {}", Operation.CONNECTING, lock.getLockInfoForLogging());
            jmsListenerEndpointRegistry.start();
        } else if (!lock.hasLock() && jmsListenerEndpointRegistry.isRunning()) {
            log.info("method=connectDisconnect operation={} {}", Operation.DISCONNECTING, lock.getLockInfoForLogging());
            jmsListenerEndpointRegistry.stop();
        }
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
