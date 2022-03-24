package fi.livi.digitraffic.tie.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.livi.digitraffic.tie.service.ClusteredLocker;
import fi.livi.digitraffic.tie.service.v1.MqttRelayQueue;
import org.slf4j.Logger;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static fi.livi.digitraffic.tie.helper.MqttUtil.getEpochSeconds;
import static fi.livi.digitraffic.tie.service.v1.MqttRelayQueue.StatisticsType.STATUS;

public class MqttMessageSenderV2 {
    private final Logger log;
    private final MqttRelayQueue mqttRelay;
    private final ObjectMapper objectMapper;

    private final ClusteredLocker clusteredLocker;
    private final String lockName;
    private final long instanceId;

    private final AtomicReference<ZonedDateTime> lastUpdated = new AtomicReference<>();
    private final AtomicReference<ZonedDateTime> lastError = new AtomicReference<>();
    private final MqttRelayQueue.StatisticsType statisticsType;
    
    public MqttMessageSenderV2(final Logger log,
                               final MqttRelayQueue mqttRelay,
                               final ObjectMapper objectMapper,
                               final MqttRelayQueue.StatisticsType statisticsType,
                               final ClusteredLocker clusteredLocker) {

        this.mqttRelay = mqttRelay;
        this.objectMapper = objectMapper;
        this.log = log;
        this.statisticsType = statisticsType;

        this.clusteredLocker = clusteredLocker;
        this.lockName = this.getClass().getSimpleName() + '_' + statisticsType;
        this.instanceId = ClusteredLocker.generateInstanceId();
    }

    public void sendMqttMessages(final ZonedDateTime lastUpdated, final Collection<MqttDataMessageV2> messages) {
        // Get lock and keep it to prevent sending on multiple nodes

        if (acquireLock()) {
            messages.forEach(this::doSendMqttMessage);
        }

        if(lastUpdated != null) {
            setLastUpdated(lastUpdated);
        }
    }

    public boolean acquireLock() {
        return clusteredLocker.tryLock(lockName, 60, instanceId);
    }

    private void doSendMqttMessage(final MqttDataMessageV2 message) {
        try {
            log.debug("method=sendMqttMessage {}", message);
            mqttRelay.queueMqttMessage(message.getTopic(), objectMapper.writeValueAsString(message.getData()), statisticsType);
        } catch (final JsonProcessingException e) {
            setLastError(ZonedDateTime.now());
            log.error("method=sendMqttMessage Error sending message", e);
        }
    }

    public void setLastUpdated(final ZonedDateTime lastUpdatedIn) {
        lastUpdated.set(Objects.requireNonNullElse(lastUpdatedIn, ZonedDateTime.now()));
    }

    public ZonedDateTime getLastUpdated() {
        return lastUpdated.get();
    }

    private void setLastError(final ZonedDateTime lastErrorIn) {
        lastError.set(lastErrorIn);
    }

    private ZonedDateTime getLastError() {
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
