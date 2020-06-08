package fi.livi.digitraffic.tie.conf;

import static fi.livi.digitraffic.tie.service.v1.MqttRelayService.StatisticsType.STATUS;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.service.LockingService;
import fi.livi.digitraffic.tie.service.v1.MqttRelayService;

public abstract class AbstractMqttConfiguration {

    protected final Logger log;
    private final String topicStringFormat;
    protected final String statusTopic;
    private final String mqttClassName;
    protected final long instanceId;
    private final boolean requireLockForSending;

    protected final ObjectMapper objectMapper;
    protected final MqttRelayService mqttRelay;
    private final LockingService lockingService;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private final AtomicReference<ZonedDateTime> lastUpdated = new AtomicReference<>();
    private final AtomicReference<ZonedDateTime> lastError = new AtomicReference<>();
    private final MqttRelayService.StatisticsType statisticsType;

    /**
     * With this constructor sending data messages are send only by one node.
     *
     * @param log Logger to be used for logging
     * @param mqttRelay MqttRelayService to be used
     * @param objectMapper ObjectMapper for json serialization
     * @param topicStringFormat String format for topic generation
     * @param statusTopic Status topic
     * @param statisticsType Status message type
     * @param lockingService LockingService to be used for db locks
     */
    public AbstractMqttConfiguration(final Logger log,
                                     final MqttRelayService mqttRelay,
                                     final ObjectMapper objectMapper,
                                     final String topicStringFormat,
                                     final String statusTopic,
                                     final MqttRelayService.StatisticsType statisticsType,
                                     final LockingService lockingService) {
        this(log, mqttRelay, objectMapper, topicStringFormat, statusTopic, statisticsType, lockingService, true);
    }

    /**
     * With this constructor you can choose if only one node can be sending data messages (@param requireLockForSending) or if any node can send messages.
     *
     * @param log Logger to be used for logging
     * @param mqttRelay MqttRelayService to be used
     * @param objectMapper ObjectMapper for json serialization
     * @param topicStringFormat String format for topic generation
     * @param statusTopic Status topic
     * @param statisticsType Status message type
     * @param lockingService LockingService to be used for db locks
     * @param requireLockForSending Is locking needed for sending data-messages between nodes. If required only one node will be sending messages.
     */
    public AbstractMqttConfiguration(final Logger log,
                                     final MqttRelayService mqttRelay,
                                     final ObjectMapper objectMapper,
                                     final String topicStringFormat,
                                     final String statusTopic,
                                     final MqttRelayService.StatisticsType statisticsType,
                                     final LockingService lockingService,
                                     final boolean requireLockForSending) {

        this.mqttRelay = mqttRelay;
        this.objectMapper = objectMapper;
        this.topicStringFormat = topicStringFormat;
        this.statusTopic = statusTopic;
        this.log = log;
        this.lockingService = lockingService;
        this.requireLockForSending = requireLockForSending;
        this.mqttClassName = getClass().getSuperclass().getSimpleName();
        this.statisticsType = statisticsType;

        instanceId = LockingService.generateInstanceId();
        executor.scheduleAtFixedRate(this::sendStatus, 30, 10, TimeUnit.SECONDS);
    }

    /**
     * Sends message to mqttRelay service to send it to Mqtt
     * @param value DataMessage containing message to send
     */
    protected void sendMqttMessage(final DataMessage value) {
        // Get lock and keep it to prevent sending on multiple nodes
        final boolean lockAcquired = !requireLockForSending || lockingService.tryLock(mqttClassName, 60, instanceId);
        if (lockAcquired) {
            try {
                log.debug("method=sendMqttMessage {}", value);
                mqttRelay.sendMqttMessage(value.getTopic(), objectMapper.writeValueAsString(value.getData()), statisticsType);
                setLastUpdated(value.getLastUpdated());
            } catch (final JsonProcessingException e) {
                setLastError(ZonedDateTime.now());
                log.error("method=sendMqttMessage Error sending message", e);
            }
        }
    }

    protected String getTopic(final Object...topicParams) {
        return String.format(topicStringFormat, topicParams);
    }

    protected void setLastUpdated(final ZonedDateTime lastUpdatedIn) {
        lastUpdated.set(Objects.requireNonNullElse(lastUpdatedIn, ZonedDateTime.now()));
    }

    protected ZonedDateTime getLastUpdated() {
        return lastUpdated.get();
    }

    private void setLastError(final ZonedDateTime lastErrorIn) {
        lastError.set(lastErrorIn);
    }

    private ZonedDateTime getLastError() {
        return lastError.get();
    }

    private void sendStatus() {
        final boolean lockAcquired = lockingService.tryLock(mqttClassName, 60, instanceId);
        if (lockAcquired) {
            try {
                final StatusMessage message = new StatusMessage(getLastUpdated(), getLastError(), "OK", statisticsType.toString());

                mqttRelay.sendMqttMessage(statusTopic, objectMapper.writeValueAsString(message), STATUS);
            } catch (final Exception e) {
                log.error("method=sendStatus Error sending message", e);
            }
        }
    }

    protected class StatusMessage {
        private final ZonedDateTime lastUpdated;
        private final ZonedDateTime lastError;
        private final String status;
        private final String type;

        public StatusMessage(final ZonedDateTime lastUpdated, final ZonedDateTime lastError, final String status, final String type) {
            this.lastUpdated = lastUpdated;
            this.lastError = lastError;
            this.status = status;
            this.type = type;
        }

        public ZonedDateTime getLastUpdated() {
            return lastUpdated;
        }

        public ZonedDateTime getLastError() {
            return lastError;
        }

        public String getStatus() {
            return status;
        }

        public String getType() {
            return type;
        }
    }

    protected class DataMessage {

        private final ZonedDateTime lastUpdated;
        private final String topic;
        private final Object data;

        public DataMessage(final ZonedDateTime lastUpdated, final String topic, final Object data) {
            this.lastUpdated = lastUpdated;
            this.topic = topic;
            this.data = data;
        }

        public ZonedDateTime getLastUpdated() {
            return lastUpdated;
        }

        public String getTopic() {
            return topic;
        }

        public Object getData() {
            return data;
        }

        @Override
        public String toString() {
            return "DataMessage{lastUpdated: " + lastUpdated + ", topic: '" + topic + ", data: " + data + '}';
        }
    }
}
