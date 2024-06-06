package fi.livi.digitraffic.tie.mqtt;

import static fi.livi.digitraffic.tie.helper.DateHelper.getEpochSeconds;
import static fi.livi.digitraffic.tie.service.mqtt.MqttRelayQueue.StatisticsType.STATUS;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.common.service.locking.CachedLockingService;
import fi.livi.digitraffic.common.service.locking.LockingService;
import fi.livi.digitraffic.tie.service.mqtt.MqttRelayQueue;

public class MqttMessageSenderV2 {
    private final Logger log;
    private final MqttRelayQueue mqttRelay;
    private final ObjectMapper objectMapper;

    private final CachedLockingService cachedLockingService;
    private final String lockName;

    private final AtomicReference<Instant> lastUpdated = new AtomicReference<>();
    private final AtomicReference<Instant> lastError = new AtomicReference<>();
    private final MqttRelayQueue.StatisticsType statisticsType;

    public MqttMessageSenderV2(final Logger log,
                               final MqttRelayQueue mqttRelay,
                               final ObjectMapper objectMapper,
                               final MqttRelayQueue.StatisticsType statisticsType,
                               final LockingService lockingService) {

        this.mqttRelay = mqttRelay;
        this.objectMapper = objectMapper;
        this.log = log;
        this.statisticsType = statisticsType;

        this.lockName = this.getClass().getSimpleName() + '_' + statisticsType;
        this.cachedLockingService = lockingService.createCachedLockingService(lockName);
    }

    public void sendMqttMessages(final Instant lastUpdated, final Collection<MqttDataMessageV2> messages) {
        // Get lock and keep it to prevent sending on multiple nodes

        if (acquireLock()) {
            messages.forEach(this::doSendMqttMessage);
        }

        setLastUpdated(lastUpdated);
    }

    public boolean acquireLock() {
        return cachedLockingService.hasLock();
    }

    private void doSendMqttMessage(final MqttDataMessageV2 message) {
        try {
            log.debug("method=doSendMqttMessage {}", message);
            final String payload = message.getData() instanceof String ? message.getData().toString() : objectMapper.writeValueAsString(message.getData());
            mqttRelay.queueMqttMessage(message.getTopic(), payload, statisticsType);
        } catch (final JsonProcessingException e) {
            setLastError(Instant.now());
            log.error("method=doSendMqttMessage Error sending message", e);
        }
    }

    public void setLastUpdated(final Instant lastUpdatedIn) {
        lastUpdated.set(Objects.requireNonNullElse(lastUpdatedIn, Instant.now()));
    }

    public Instant getLastUpdated() {
        return lastUpdated.get();
    }

    private void setLastError(final Instant lastErrorIn) {
        lastError.set(lastErrorIn);
    }

    private Instant getLastError() {
        return lastError.get();
    }

    public void sendStatusMessage(final String statusTopic) {
        try {
            final MqttStatusMessageV2 message =
                new MqttStatusMessageV2(getEpochSeconds(getLastUpdated()), getEpochSeconds(getLastError()));

            mqttRelay.queueMqttMessage(statusTopic, objectMapper.writeValueAsString(message), STATUS);
        } catch (final Exception e) {
            log.error("method=sendStatus Error sending message", e);
        }
    }
}
