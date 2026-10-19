package fi.livi.digitraffic.tie.conf.mqtt;

import static fi.livi.digitraffic.tie.service.mqtt.MqttRelayQueue.StatisticsType.TRAFFIC_MESSAGE;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import fi.livi.digitraffic.tie.helper.GZipUtils;
import fi.livi.digitraffic.tie.model.data.SituationMqttMessage;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.common.annotation.NoJobLogging;
import fi.livi.digitraffic.common.service.locking.LockingService;
import fi.livi.digitraffic.tie.controller.trafficmessage.MessageConverter;
import fi.livi.digitraffic.tie.external.tloik.ims.v1_2_2.MessageTypeEnum;
import fi.livi.digitraffic.tie.helper.MqttUtil;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2Version;
import fi.livi.digitraffic.tie.mqtt.MqttDataMessageV2;
import fi.livi.digitraffic.tie.mqtt.MqttMessageSenderV2;
import fi.livi.digitraffic.tie.service.data.MqttService;
import fi.livi.digitraffic.tie.service.mqtt.MqttRelayQueue;

@ConditionalOnNotWebApplication
@ConditionalOnProperty("mqtt.trafficMessage.v3.enabled")
@Component
public class DatexII35MqttConfigurationV3 {
    private final MqttMessageSenderV2 mqttMessageSender;
    private final MqttService mqttService;
    private final ObjectMapper objectMapper;
    private final MessageConverter messageConverter;

    private static final String TRAFFIC_MESSAGE_V3_ROOT_TOPIC = "traffic-message-v3";

    // traffic-message-v3/{situationType}/{messageType}
    public static final String TRAFFIC_MESSAGE_V3_TOPIC = TRAFFIC_MESSAGE_V3_ROOT_TOPIC + "/%s/%s";
    // traffic-message-v3/status
    private static final String TRAFFIC_MESSAGE_V3_STATUS_TOPIC = TRAFFIC_MESSAGE_V3_ROOT_TOPIC + "/status";

    private static final Logger LOGGER = getLogger(DatexII35MqttConfigurationV3.class);

    public DatexII35MqttConfigurationV3(final MqttRelayQueue mqttRelay,
                                        final ObjectMapper objectMapper,
                                        final LockingService lockingService,
                                        final MqttService mqttService,
                                        final MessageConverter messageConverter) {
        this.mqttService = mqttService;
        this.objectMapper = objectMapper;
        this.messageConverter = messageConverter;

        this.mqttMessageSender =
                new MqttMessageSenderV2(LOGGER, mqttRelay, objectMapper, TRAFFIC_MESSAGE, lockingService);

        mqttMessageSender.setLastUpdated(Instant.now());
    }

    @NoJobLogging
    @Scheduled(fixedDelayString = "${mqtt.trafficMessage.v3.pollingIntervalMs}")
    public void pollAndSendMessages() {
        if (mqttMessageSender.acquireLock()) {
            try {
                final Pair<Instant, List<SituationMqttMessage>> messages =
                        mqttService.findMessagesAfter(mqttMessageSender.getLastUpdated());

                if (!messages.getRight().isEmpty()) {
                    final List<MqttDataMessageV2> dataMessages = messages.getRight().stream()
                            .map(this::createMqttDataMessage)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    mqttMessageSender.sendMqttMessages(messages.getLeft(), dataMessages);
                }

            } catch (final Exception e) {
                LOGGER.error("method=pollAndSendMessages Polling failed", e);
            }
        } else {
            mqttMessageSender.setLastUpdated(Instant.now());
        }
    }

    private MqttDataMessageV2 createMqttDataMessage(final SituationMqttMessage mqttMessage) {
        final String topic = createTopic(mqttMessage.getMessageType(), mqttMessage.getMessageVersion(),
                mqttMessage.getSituationType());
        final String message = createMessage(mqttMessage);

        if (topic == null || message == null) {
            return null;
        }

        return new MqttDataMessageV2(topic, message);
    }

    /**
     * SIMPPELI messages are converted through the same DTO as the REST API before publishing, so that
     * MQTT subscribers receive byte-identical JSON with the corresponding HTTP endpoint. Publishing the
     * stored message as-is would leak fields that are not part of the documented schema and would differ
     * in property order and null handling. DATEX2 messages are published as the stored XML, which is what
     * the REST API returns as well.
     *
     * @return the payload to publish, or null if the message could not be converted
     */
    private String createMessage(final SituationMqttMessage mqttMessage) {
        if (mqttMessage.getMessageType().equals(MessageTypeEnum.SIMPPELI.value())) {
            try {
                final var feature = messageConverter.convertToFeature(mqttMessage.getMessage(), true, mqttMessage.getModifiedAt());
                return GZipUtils.compressToBase64String(objectMapper.writeValueAsString(feature));
            } catch (final Exception e) {
                LOGGER.error("method=createMessage Failed to convert SIMPPELI message situationType={} messageVersion={}",
                        mqttMessage.getSituationType(), mqttMessage.getMessageVersion(), e);
                return null;
            }
        }

        return mqttMessage.getMessage();
    }

    /**
     * An unknown Datex II version is not published. Publishing it under a topic derived from the
     * version string would create a topic that is not documented and that no one is subscribed to.
     */
    private String createTopic(final String messageType, final String messageVersion, final String situationType) {
        if (messageType.equals(MessageTypeEnum.DATEX2.value())) {
            if (messageVersion.equals(Datex2Version.V_3_7.version)) {
                return MqttUtil.getTopicForMessage(TRAFFIC_MESSAGE_V3_TOPIC, "datex2-3.7", situationType);
            } else if (messageVersion.equals(Datex2Version.V_3_5.version)) {
                return MqttUtil.getTopicForMessage(TRAFFIC_MESSAGE_V3_TOPIC, "datex2-3.5", situationType);
            } else if (messageVersion.equals(Datex2Version.V_2_2_3.version)) {
                return MqttUtil.getTopicForMessage(TRAFFIC_MESSAGE_V3_TOPIC, "datex2-2.2.3", situationType);
            } else {
                LOGGER.error("Unknown message version {} for DatexII", messageVersion);
                return null;
            }
        } else if (messageType.equals(MessageTypeEnum.SIMPPELI.value())) {
            return MqttUtil.getTopicForMessage(TRAFFIC_MESSAGE_V3_TOPIC, "simple", situationType);
        } else {
            LOGGER.error("Unknown message type {} with version {}", messageType, messageVersion);
        }

        return null;
    }

    @Scheduled(fixedDelayString = "${mqtt.status.intervalMs}")
    public void sendStatusMessage() {
        if (mqttMessageSender.acquireLock()) {
            mqttMessageSender.sendStatusMessage(TRAFFIC_MESSAGE_V3_STATUS_TOPIC);
        }
    }
}
