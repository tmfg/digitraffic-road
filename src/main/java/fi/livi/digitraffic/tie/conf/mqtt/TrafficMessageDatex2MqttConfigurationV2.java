package fi.livi.digitraffic.tie.conf.mqtt;

import static fi.livi.digitraffic.tie.service.mqtt.MqttRelayQueue.StatisticsType.TRAFFIC_MESSAGE_DATEX;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.helper.MqttUtil;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2;
import fi.livi.digitraffic.tie.mqtt.MqttDataMessageV2;
import fi.livi.digitraffic.tie.mqtt.MqttMessageSenderV2;
import fi.livi.digitraffic.tie.service.ClusteredLocker;
import fi.livi.digitraffic.tie.service.mqtt.MqttRelayQueue;
import fi.livi.digitraffic.tie.service.trafficmessage.v1.TrafficMessageMqttDataServiceV1;

@ConditionalOnProperty("mqtt.trafficMessage.datex2.v2.enabled")
@ConditionalOnNotWebApplication
@Component
public class TrafficMessageDatex2MqttConfigurationV2 {
    private static final String TRAFFIC_MESSAGE_DATEX2_V2_ROOT_TOPIC = "traffic-message-v2/datex2";
    // traffic-message-v2/{situationType}
    public static final String TRAFFIC_MESSAGE_V2_TOPIC = TRAFFIC_MESSAGE_DATEX2_V2_ROOT_TOPIC + "/%s";
    // traffic-message-v2/status
    private static final String TRAFFIC_MESSAGE_V2_STATUS_TOPIC = TRAFFIC_MESSAGE_DATEX2_V2_ROOT_TOPIC + "/status";

    private static final Logger LOGGER = getLogger(TrafficMessageDatex2MqttConfigurationV2.class);

    private final TrafficMessageMqttDataServiceV1 trafficMessageMqttDataServiceV1;
    private final MqttMessageSenderV2 mqttMessageSender;

    @Autowired
    public TrafficMessageDatex2MqttConfigurationV2(final TrafficMessageMqttDataServiceV1 trafficMessageMqttDataServiceV1,
                                                   final MqttRelayQueue mqttRelay,
                                                   final ObjectMapper objectMapper,
                                                   final ClusteredLocker clusteredLocker) {
        this.trafficMessageMqttDataServiceV1 = trafficMessageMqttDataServiceV1;
        this.mqttMessageSender = new MqttMessageSenderV2(LOGGER, mqttRelay, objectMapper, TRAFFIC_MESSAGE_DATEX, clusteredLocker);

        mqttMessageSender.setLastUpdated(Instant.now());
    }


    @Scheduled(fixedDelayString = "${mqtt.TrafficMessage.v2.pollingIntervalMs}")
    public void pollAndSendMessages() {
        if (mqttMessageSender.acquireLock()) {
            try {
                final Pair<Instant, List<Datex2>> trafficMessagesPair =
                    trafficMessageMqttDataServiceV1.findDatex2TrafficMessagesForMqttCreatedAfter(mqttMessageSender.getLastUpdated());

                if(!trafficMessagesPair.getRight().isEmpty()) {
                    final List<MqttDataMessageV2> dataMessages = trafficMessagesPair.getRight().stream()
                        .map(this::createMqttDataMessage)
                        .collect(Collectors.toList());
                    mqttMessageSender.sendMqttMessages(trafficMessagesPair.getLeft(), dataMessages);
                }

            } catch (final Exception e) {
                LOGGER.error("method=pollAndSendMessages Polling failed", e);
            }
        } else {
            mqttMessageSender.setLastUpdated(Instant.now());
        }
    }

    /**
     * @param trafficMessage to convert
     * @return message or null if failed
     */
    private MqttDataMessageV2 createMqttDataMessage(final Datex2 trafficMessage) {
        final String topic = MqttUtil.getTopicForMessage(TRAFFIC_MESSAGE_V2_TOPIC, trafficMessage.getSituationType());
        try {
            final String datex2 = trafficMessage.getMessage();
            return new MqttDataMessageV2(topic, datex2);
        } catch (final Exception e) {
            LOGGER.error(String.format("method=createMqttDataMessage failed situationId=%s versionTime: %s", trafficMessage.getSituations().get(0).getSituationId(), trafficMessage.getSituations().get(0).getVersionTime()), e);
            return null;
        }

    }

    @Scheduled(fixedDelayString = "${mqtt.status.intervalMs}")
    public void sendStatusMessage() {
        if (mqttMessageSender.acquireLock()) {
            mqttMessageSender.sendStatusMessage(TRAFFIC_MESSAGE_V2_STATUS_TOPIC);
        }
    }
}