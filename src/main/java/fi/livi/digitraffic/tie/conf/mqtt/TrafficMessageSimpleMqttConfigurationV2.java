package fi.livi.digitraffic.tie.conf.mqtt;

import static fi.livi.digitraffic.tie.service.mqtt.MqttRelayQueue.StatisticsType.TRAFFIC_MESSAGE_SIMPLE;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.helper.GZipUtils;
import fi.livi.digitraffic.tie.helper.MqttUtil;
import fi.livi.digitraffic.tie.mqtt.MqttDataMessageV2;
import fi.livi.digitraffic.tie.mqtt.MqttMessageSenderV2;
import fi.livi.digitraffic.tie.service.ClusteredLocker;
import fi.livi.digitraffic.tie.service.mqtt.MqttRelayQueue;
import fi.livi.digitraffic.tie.service.trafficmessage.v1.TrafficMessageMqttDataServiceV1;

@ConditionalOnProperty("mqtt.trafficMessage.simple.v2.enabled")
@ConditionalOnNotWebApplication
@Component
public class TrafficMessageSimpleMqttConfigurationV2 {
    private static final String TRAFFIC_MESSAGE_SIMPLE_V2_ROOT_TOPIC = "traffic-message-v2/simple";
    // traffic-message-v2/{situationType}
    public static final String TRAFFIC_MESSAGE_SIMPLE_V2_TOPIC = TRAFFIC_MESSAGE_SIMPLE_V2_ROOT_TOPIC + "/%s";
    // traffic-message-v2/status
    private static final String TRAFFIC_MESSAGE_V2_STATUS_TOPIC = TRAFFIC_MESSAGE_SIMPLE_V2_ROOT_TOPIC + "/status";

    private static final Logger LOGGER = getLogger(TrafficMessageSimpleMqttConfigurationV2.class);

    private final TrafficMessageMqttDataServiceV1 trafficMessageMqttDataServiceV1;
    private final ObjectMapper objectMapper;
    private final MqttMessageSenderV2 mqttMessageSender;

    @Autowired
    public TrafficMessageSimpleMqttConfigurationV2(final TrafficMessageMqttDataServiceV1 trafficMessageMqttDataServiceV1,
                                                   final MqttRelayQueue mqttRelay,
                                                   final ObjectMapper objectMapper,
                                                   final ClusteredLocker clusteredLocker) {
        this.trafficMessageMqttDataServiceV1 = trafficMessageMqttDataServiceV1;
        this.objectMapper = objectMapper;
        this.mqttMessageSender = new MqttMessageSenderV2(LOGGER, mqttRelay, objectMapper, TRAFFIC_MESSAGE_SIMPLE, clusteredLocker);

        mqttMessageSender.setLastUpdated(Instant.now());
    }


    @Scheduled(fixedDelayString = "${mqtt.TrafficMessage.v2.pollingIntervalMs}")
    public void pollAndSendMessages() {
        if (mqttMessageSender.acquireLock()) {
            try {
                final Pair<Instant, List<TrafficAnnouncementFeature>> trafficMessagesPair =
                    trafficMessageMqttDataServiceV1.findSimpleTrafficMessagesForMqttCreatedAfter(mqttMessageSender.getLastUpdated());

                if(!trafficMessagesPair.getRight().isEmpty()) {
                    final List<MqttDataMessageV2> dataMessages = trafficMessagesPair.getRight().stream()
                        .map(this::createMqttDataMessage)
                        .filter(Objects::nonNull)
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
    private MqttDataMessageV2 createMqttDataMessage(final TrafficAnnouncementFeature trafficMessage) {
        final String topic = MqttUtil.getTopicForMessage(TRAFFIC_MESSAGE_SIMPLE_V2_TOPIC, trafficMessage.getProperties().getSituationType());
        try {
            final String featureJson = objectMapper.writeValueAsString(trafficMessage);
            final String compressedBase64String = GZipUtils.compressToBase64String(featureJson);
            LOGGER.debug("method=createMqttDataMessage compressed from {} to {} bytes. base64String: {}", featureJson.getBytes().length, compressedBase64String.getBytes().length, compressedBase64String);
            return new MqttDataMessageV2(topic, compressedBase64String);
        } catch (final Exception e) {
            LOGGER.error(String.format("method=createMqttDataMessage failed situationId=%s version %s", trafficMessage.getProperties().situationId, trafficMessage.getProperties().version), e);
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