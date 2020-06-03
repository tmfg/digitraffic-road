package fi.livi.digitraffic.tie.conf;

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
    protected final MqttRelayService mqttRelay;
    protected final ObjectMapper objectMapper;
    private final String topicStringFormat;
    protected final String statusTopic;
    private final String mqttClassName;
    protected final long instanceId;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private final AtomicReference<ZonedDateTime> lastUpdated = new AtomicReference<>();
    private final AtomicReference<ZonedDateTime> lastError = new AtomicReference<>();
    private final MqttRelayService.StatisticsType statisticsType;

    public AbstractMqttConfiguration(final Logger log,
                                     final MqttRelayService mqttRelay,
                                     final ObjectMapper objectMapper,
                                     final String topicStringFormat,
                                     final String statusTopic,
                                     final MqttRelayService.StatisticsType statisticsType) {

        this.mqttRelay = mqttRelay;
        this.objectMapper = objectMapper;
        this.topicStringFormat = topicStringFormat;
        this.statusTopic = statusTopic;
        this.log = log;
        this.mqttClassName = getClass().getSuperclass().getSimpleName();
        this.statisticsType = statisticsType;

        instanceId = LockingService.generateInstanceId();
        executor.scheduleAtFixedRate(this::sendStatus, 30, 10, TimeUnit.SECONDS);
    }

    /**
     * Call this from @Scheduled etc. scheduler to poll new messages and send them to MQTT.
     * Don't call concurrently from same instance.
     */
    public void pollAndSendMessages() {
        final List<DataMessage> messages = pollMessages();
        log.debug("method=pollAndSendMessages polled {} messages to send", messages.size());
        messages.forEach(this::sendMqttMessage);
    }

    /**
     * Implementation should fetch all messages to send to MQTT
     * @return messages to be send to MQTT
     */
    protected abstract List<DataMessage> pollMessages();

    private void sendMqttMessage(final DataMessage value) {
        try {
            log.debug("method=sendMqttMessage {}", value);
            mqttRelay.sendMqttMessage(value.getTopic(),
                                      objectMapper.writeValueAsString(value.getData()),
                                      statisticsType);
            setLastUpdated(value.getLastUpdated());
        } catch (final JsonProcessingException e) {
            setLastError(ZonedDateTime.now());
            log.error("method=sendMqttMessage Error sending message", e);
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
        try {
            final StatusMessage message = new StatusMessage(getLastUpdated(), getLastError(), "OK", statisticsType.toString());

            mqttRelay.sendMqttMessage(statusTopic, objectMapper.writeValueAsString(message));
        } catch (final Exception e) {
            log.error("method=sendStatus Error sending message", e);
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
