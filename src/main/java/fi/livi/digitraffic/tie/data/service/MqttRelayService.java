package fi.livi.digitraffic.tie.data.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.conf.MqttConfig;

@Component
@ConditionalOnExpression("'${app.type}' == 'daemon' and '${config.test}' != 'true'")
public class MqttRelayService {
    private static final Logger logger = LoggerFactory.getLogger(MqttRelayService.class);

    private static final Map<StatisticsType, Integer> sentStatisticsMap = new ConcurrentHashMap<>();

    @Lazy // this will not be available if mqtt is not enabled
    private final MqttConfig.MqttGateway mqttGateway;

    public enum StatisticsType {TMS, WEATHER}

    public static final String statusOK = "{\"status\": \"OK\"}";
    public static final String statusNOCONTENT = "{\"status\": \"no content\"}";

    @Autowired
    public MqttRelayService(final MqttConfig.MqttGateway mqttGateway) {
        this.mqttGateway = mqttGateway;
    }

    /**
     * Send mqtt message. NOTE! This must be synchronized, because Paho does not support concurrency!
     * @param topic
     * @param payLoad
     */
    public synchronized void sendMqttMessage(final String topic, final String payLoad) {
        mqttGateway.sendToMqtt(topic, payLoad);
    }

    /**
     * Update send messages statistics
     * @param type
     * @param messages
     */
    public synchronized void sentMqttStatistics(final StatisticsType type, final int messages) {
        if (sentStatisticsMap.containsKey(type)) {
            sentStatisticsMap.put(type, sentStatisticsMap.get(type) + messages);
        } else {
            sentStatisticsMap.put(type, messages);
        }
    }

    @Scheduled(fixedRate = 60000)
    private void logMessageCount() {
        for (final StatisticsType type : StatisticsType.values()) {
            final Integer sentMessages = sentStatisticsMap.get(type);

            logger.info("Sent mqtt statistics for type={} messages={}", type, sentMessages != null ? sentMessages : 0);

            sentStatisticsMap.put(type, 0);
        }
    }
}
