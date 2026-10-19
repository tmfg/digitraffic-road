package fi.livi.digitraffic.tie.conf.mqtt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;

import tools.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.common.service.locking.CachedLockingService;
import fi.livi.digitraffic.common.service.locking.LockingService;
import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.DataDatex2SituationTestHelper;
import fi.livi.digitraffic.tie.controller.trafficmessage.MessageConverter;
import fi.livi.digitraffic.tie.external.tloik.ims.v1_2_2.MessageTypeEnum;
import fi.livi.digitraffic.tie.helper.GZipUtils;
import fi.livi.digitraffic.tie.model.data.SituationMqttMessage;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.SimppeliVersion;
import fi.livi.digitraffic.tie.service.data.MqttService;
import fi.livi.digitraffic.tie.service.mqtt.MqttRelayQueue;

/**
 * Verifies that the traffic-message-v3 MQTT payload for SIMPPELI messages is produced through the same
 * DTO as the REST API, so that MQTT subscribers and HTTP clients receive identical JSON.
 */
public class DatexII35MqttConfigurationV3Test extends AbstractDaemonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MessageConverter messageConverter;

    private MqttRelayQueue mqttRelay;
    private MqttService mqttService;
    private DatexII35MqttConfigurationV3 configuration;

    @BeforeEach
    void setUp() {
        mqttRelay = mock(MqttRelayQueue.class);
        mqttService = mock(MqttService.class);

        final CachedLockingService cachedLockingService = mock(CachedLockingService.class);
        when(cachedLockingService.hasLock()).thenReturn(true);
        final LockingService lockingService = mock(LockingService.class);
        when(lockingService.createCachedLockingService(anyString())).thenReturn(cachedLockingService);

        configuration = new DatexII35MqttConfigurationV3(mqttRelay, objectMapper, lockingService, mqttService,
                messageConverter);
    }

    /**
     * The published SIMPPELI payload must equal the DTO-serialised JSON returned by the REST API,
     * not the raw message stored in the database.
     */
    @Test
    public void pollAndSendMessages_simppeli_payloadMatchesRestApiDto() {
        final String rawMessage = DataDatex2SituationTestHelper.DEFAULT_SIMPPELI_MESSAGE;
        when(mqttService.findMessagesAfter(any())).thenReturn(Pair.of(Instant.now(),
                List.of(simppeliMessage(rawMessage))));

        configuration.pollAndSendMessages();

        final String published = GZipUtils.decompressBase64String(capturePublishedPayload());
        final String expected = objectMapper.writeValueAsString(messageConverter.convertToFeature(rawMessage, true, MODIFIED_AT));

        assertEquals(expected, published,
                "MQTT payload must be the DTO-serialised JSON, identical to what the REST API returns.");
    }

    /**
     * Fields that are not part of the documented DTO must not leak to MQTT subscribers.
     */
    @Test
    public void pollAndSendMessages_simppeli_unknownFieldsAreNotPublished() {
        final String rawMessage = DataDatex2SituationTestHelper.DEFAULT_SIMPPELI_MESSAGE
                .replaceFirst("\\{", "{\"internalOnlyField\": \"should-not-be-published\",");
        assertTrue(rawMessage.contains("internalOnlyField"), "Test setup should add an unknown field.");

        when(mqttService.findMessagesAfter(any())).thenReturn(Pair.of(Instant.now(),
                List.of(simppeliMessage(rawMessage))));

        configuration.pollAndSendMessages();

        final String published = GZipUtils.decompressBase64String(capturePublishedPayload());

        assertNotEquals(rawMessage, published, "The raw stored message must not be published as-is.");
        assertFalse(published.contains("internalOnlyField"),
                "Fields outside the DTO must be dropped before publishing.");
    }

    private String capturePublishedPayload() {
        final ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(mqttRelay).queueMqttMessage(anyString(), payloadCaptor.capture(), any());
        return payloadCaptor.getValue();
    }

    /**
     * The stored message has no update timestamp, so it must be taken from the database row.
     */
    @Test
    public void pollAndSendMessages_simppeli_dataUpdatedTimeComesFromTheRow() {
        when(mqttService.findMessagesAfter(any())).thenReturn(Pair.of(Instant.now(),
                List.of(simppeliMessage(DataDatex2SituationTestHelper.DEFAULT_SIMPPELI_MESSAGE))));

        configuration.pollAndSendMessages();

        final String published = GZipUtils.decompressBase64String(capturePublishedPayload());
        assertTrue(published.replace(" ", "").contains("\"dataUpdatedTime\":\"2026-01-02T03:04:05.678Z\""),
                "dataUpdatedTime must carry the row's modification time, was: " + published);
    }

    private static final Instant MODIFIED_AT = Instant.parse("2026-01-02T03:04:05.678Z");

    private static SituationMqttMessage simppeliMessage(final String message) {
        return new SituationMqttMessage() {
            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public Instant getModifiedAt() {
                return MODIFIED_AT;
            }

            @Override
            public String getSituationType() {
                return "TRAFFIC_ANNOUNCEMENT";
            }

            @Override
            public String getMessageType() {
                return MessageTypeEnum.SIMPPELI.value();
            }

            @Override
            public String getMessageVersion() {
                return SimppeliVersion.V_0_2_18.version;
            }
        };
    }
}
