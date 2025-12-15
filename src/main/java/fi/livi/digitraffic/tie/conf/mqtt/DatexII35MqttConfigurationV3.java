package fi.livi.digitraffic.tie.conf.mqtt;

import static fi.livi.digitraffic.tie.service.mqtt.MqttRelayQueue.StatisticsType.TRAFFIC_MESSAGE;
import static fi.livi.digitraffic.tie.service.mqtt.MqttRelayQueue.StatisticsType.TRAFFIC_MESSAGE_DATEX;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import fi.livi.digitraffic.tie.helper.GZipUtils;
import fi.livi.digitraffic.tie.model.data.SituationMqttMessage;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.common.annotation.NoJobLogging;
import fi.livi.digitraffic.common.service.locking.LockingService;
import fi.livi.digitraffic.tie.external.tloik.ims.v1_2_2.MessageTypeEnum;
import fi.livi.digitraffic.tie.helper.MqttUtil;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2Version;
import fi.livi.digitraffic.tie.mqtt.MqttDataMessageV2;
import fi.livi.digitraffic.tie.mqtt.MqttMessageSenderV2;
import fi.livi.digitraffic.tie.service.data.MqttService;
import fi.livi.digitraffic.tie.service.mqtt.MqttRelayQueue;

@ConditionalOnBooleanProperty(name = "dt.datex2_35.enabled", matchIfMissing = true)
@ConditionalOnNotWebApplication
@Component
public class DatexII35MqttConfigurationV3 {
    private final MqttMessageSenderV2 mqttMessageSender;
    private final MqttService mqttService;

    private static final String TRAFFIC_MESSAGE_V3_ROOT_TOPIC = "traffic-message-v3";

    // traffic-message-v3/{situationType}/{messageType}
    public static final String TRAFFIC_MESSAGE_V3_TOPIC = TRAFFIC_MESSAGE_V3_ROOT_TOPIC + "/%s/%s";
    // traffic-message-v3/status
    private static final String TRAFFIC_MESSAGE_V3_STATUS_TOPIC = TRAFFIC_MESSAGE_V3_ROOT_TOPIC + "/status";

    private static final Logger LOGGER = getLogger(DatexII35MqttConfigurationV3.class);

    public DatexII35MqttConfigurationV3(final MqttRelayQueue mqttRelay,
                                        final ObjectMapper objectMapper,
                                        final LockingService lockingService,
                                        final MqttService mqttService) {
        this.mqttService = mqttService;

        this.mqttMessageSender =
                new MqttMessageSenderV2(LOGGER, mqttRelay, objectMapper, TRAFFIC_MESSAGE, lockingService);

        mqttMessageSender.setLastUpdated(Instant.now());
    }

    @NoJobLogging
    @Scheduled(fixedDelayString = "${mqtt.TrafficMessage.v2.pollingIntervalMs}")
    public void pollAndSendMessages() {
        if (mqttMessageSender.acquireLock()) {
            try {
                final Pair<Instant, List<SituationMqttMessage>> messages =
                        mqttService.findMessagesAfter(mqttMessageSender.getLastUpdated());

                if(!messages.getRight().isEmpty()) {
                    final List<MqttDataMessageV2> dataMessages = messages.getRight().stream()
                            .map(this::createMqttDataMessage)
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
        final String topic = createTopic(mqttMessage.getMessageType(), mqttMessage.getMessageVersion(), mqttMessage.getSituationType());
        final String message = createMessage(mqttMessage);

        return new MqttDataMessageV2(topic, message);
    }

    private String createMessage(final SituationMqttMessage mqttMessage) {
        if(mqttMessage.getMessageType().equals(MessageTypeEnum.SIMPPELI.value())) {
            return GZipUtils.compressToBase64String(mqttMessage.getMessage());
        }
        
        return mqttMessage.getMessage();
    }

    private String createTopic(final String messageType, final String messageVersion, final String situationType) {
        if(messageType.equals(MessageTypeEnum.DATEX_2.value())) {
            if(messageVersion.equals(Datex2Version.V_3_5.version)) {
                return MqttUtil.getTopicForMessage(TRAFFIC_MESSAGE_V3_TOPIC, "datex2-3.5", situationType);
            } else if(messageVersion.equals(Datex2Version.V_2_2_3.version)) {
                return MqttUtil.getTopicForMessage(TRAFFIC_MESSAGE_V3_TOPIC, "datex2-2.2.3", situationType);
            } else {
                LOGGER.error("Unknown message version {} for DatexII", messageVersion);
                return MqttUtil.getTopicForMessage(TRAFFIC_MESSAGE_V3_TOPIC, "datex2-" + messageVersion, situationType);
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
