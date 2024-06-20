package fi.livi.digitraffic.tie.conf.kca.artemis.jms.listener;

import org.apache.activemq.artemis.jms.client.ActiveMQMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import fi.livi.digitraffic.common.annotation.NoJobLogging;
import fi.livi.digitraffic.tie.service.jms.JMSMessageHandler;
import fi.livi.digitraffic.tie.service.jms.JMSMessageMarshaller;
import jakarta.annotation.PreDestroy;
import jakarta.jms.JMSException;

public class JMSListenerConfiguration<K> {
    private static final Logger log = LoggerFactory.getLogger(JMSListenerConfiguration.class);
    private final JMSMessageHandler<K> jmsMessageHandler;

    public JMSListenerConfiguration(final JMSMessageHandler.JMSMessageType jmsMessageType,
                                    final JMSMessageHandler.JMSDataUpdater<K> dataUpdater,
                                    final JMSMessageMarshaller<K> jmsMessageMarshaller,
                                    final String instanceId) {
        this.jmsMessageHandler = new JMSMessageHandler<>(jmsMessageType, dataUpdater, jmsMessageMarshaller, instanceId);
    }

    protected void onMessage(final ActiveMQMessage activeMQMessage) throws JMSException {
        try {
            jmsMessageHandler.onMessage(activeMQMessage);
        } catch (final Exception e) {
            log.error("method=onMessage failed {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Drain queue scheduled.
     */
    @NoJobLogging
    @Scheduled(fixedDelayString = "${kca.artemis.jms.drainIntervalMs}")
    public void drainQueueScheduled() {
        jmsMessageHandler.drainQueueScheduled();
    }

    @PreDestroy
    public void onShutdown() {
        jmsMessageHandler.onShutdown();
    }
}
