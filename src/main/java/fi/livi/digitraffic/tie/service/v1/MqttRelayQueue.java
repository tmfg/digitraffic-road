package fi.livi.digitraffic.tie.service.v1;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.LongAdder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.conf.MqttConfiguration;

@Component
@ConditionalOnExpression("'${app.type}' == 'daemon' and '${config.test}' != 'true'")
public class MqttRelayQueue {
    private static final Logger logger = LoggerFactory.getLogger(MqttRelayQueue.class);
    private static final int MAX_QUEUE_SIZE = 100000;

    private static final Map<StatisticsType, LongAdder> sentStatisticsMap = new ConcurrentHashMap<>();
    private static final Map<StatisticsType, LongAdder> sendErrorStatisticsMap = new ConcurrentHashMap<>();
    private final BlockingQueue<QueueItem> messageList = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);

    private final LongAdder addedMessagesAdder = new LongAdder();
    private long maxQueueLength = 0;

    public enum StatisticsType {TMS, WEATHER, MAINTENANCE_TRACKING, TRAFFIC_MESSAGE, STATUS}

    private static final Integer QOS = 0;

    @Autowired
    public MqttRelayQueue(final MqttConfiguration.MqttGateway mqttGateway) {
        for (final StatisticsType type : StatisticsType.values()) {
            sentStatisticsMap.put(type, new LongAdder());
            sendErrorStatisticsMap.put(type, new LongAdder());
        }

        // in a threadsafe way, take messages from message list and send them to mqtt gateway
        final Thread sender = new Thread(() -> {
            while(true) {
                final QueueItem item = getNextMessage();

                if (item != null) {
                    maxQueueLength = Math.max(maxQueueLength, messageList.size());

                    try {
                        mqttGateway.sendToMqtt(item.topic, item.message);
                        updateSentMqttStatistics(item.statistics, 1);
                    } catch (final Exception e) {
                        if (sendErrorStatisticsMap.isEmpty()) {
                            logger.error("MqttGateway send failure", e);
                        }

                        updateSendErrorMqttStatistics(item.statistics, 1);
                    }
                }
            }
        });

        sender.setName("MqttSenderThread");
        sender.start();
    }

    private QueueItem getNextMessage() {
        try {
            return messageList.take();
        } catch (final InterruptedException ie) {
            logger.error("method=getNextMessage Interrupted", ie);
            Thread.currentThread().interrupt();
        } catch (final Exception e) {
            logger.error("method=getNextMessage Mqtt messageList.take() failed", e);
        }

        return null;
    }

    @Scheduled(fixedRate = 60000)
    public void logMqttQueue() {
        logger.info("prefix=CURRENT queueSize={}", messageList.size());
        logger.info("prefix=MAX queueSize={}", maxQueueLength);
        logger.info("prefix=ADDED queueSize={}", addedMessagesAdder.sumThenReset());

        maxQueueLength = 0;
    }

    /**
     * Add mqtt message to messagelist. Messagelist is synchronized and threadsafe.
     * @param topic Mqtt message topic
     * @param payLoad Mqtt message payload
     * @param statisticsType Statistics type for the message
     */
    public void queueMqttMessage(final String topic, final String payLoad, final StatisticsType statisticsType) {
        if (topic == null || payLoad == null || statisticsType == null) {
            throw new IllegalArgumentException(String.format("All parameters must be set topic:%s, payload:%s, statisticsType:%s",
                                                             topic, payLoad, statisticsType));
        }

        try {
            messageList.add(new QueueItem(topic, payLoad, statisticsType));
            addedMessagesAdder.increment();
        } catch (final IllegalStateException e) {
            logger.error("Mqtt send queue full!");
            messageList.clear();
        }
    }

    /**
     * Update send messages statistics
     * @param type Statistics type
     * @param messages Count of messages send
     */
    public void updateSentMqttStatistics(final StatisticsType type, final int messages) {
        sentStatisticsMap.get(type).add(messages);
    }

    /**
     * Update sendig error statistics
     * @param type Statistics type
     * @param messages Count of messages send
     */
    public void updateSendErrorMqttStatistics(final StatisticsType type, final int messages) {
        sendErrorStatisticsMap.get(type).add(messages);
    }

    @Scheduled(fixedRate = 60000)
    public void logMessageCount() {
        for (final StatisticsType type : StatisticsType.values()) {
            final long sentMessages = sentStatisticsMap.get(type).sumThenReset();
            final long sentErrors = sendErrorStatisticsMap.get(type).sumThenReset();

            logger.info("method=logMessageCount type={} messages={} errors={}", type, sentMessages, sentErrors);
        }
    }

    private static final class QueueItem {
        public final String topic;
        public final String message;
        public final StatisticsType statistics;

        private QueueItem(final String topic, final String message, final StatisticsType statistics) {
            this.topic = topic;
            this.message = message;
            this.statistics = statistics;
        }
    }
}
