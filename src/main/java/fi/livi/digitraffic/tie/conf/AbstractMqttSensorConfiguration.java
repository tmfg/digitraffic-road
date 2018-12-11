package fi.livi.digitraffic.tie.conf;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.data.dto.SensorValueDto;
import fi.livi.digitraffic.tie.data.service.LockingService;
import fi.livi.digitraffic.tie.data.service.MqttRelayService;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;

public abstract class AbstractMqttSensorConfiguration {
    private final MqttRelayService mqttRelay;
    private final RoadStationSensorService roadStationSensorService;
    private final ObjectMapper objectMapper;
    private final RoadStationType roadStationType;
    private final Logger logger;
    private final String statusTopic;
    private final String messageTopic;
    private final LockingService lockingService;
    private final String mqttClassName;

    private ZonedDateTime lastUpdated;
    private ZonedDateTime lastError;
    private MqttRelayService.StatisticsType statisticsType;
    private int messageCounter = 0;

    public AbstractMqttSensorConfiguration(final MqttRelayService mqttRelay,
                                           final RoadStationSensorService roadStationSensorService,
                                           final ObjectMapper objectMapper,
                                           final RoadStationType roadStationType,

                                           final String statusTopic,
                                           final String messageTopic,
                                           final Logger logger,
                                           final LockingService lockingService,
                                           final String mqttClassName) {

        this.mqttRelay = mqttRelay;
        this.roadStationSensorService = roadStationSensorService;
        this.objectMapper = objectMapper;
        this.roadStationType = roadStationType;
        this.statusTopic = statusTopic;
        this.messageTopic = messageTopic;
        this.logger = logger;
        this.lockingService = lockingService;
        this.mqttClassName = mqttClassName;

        if (roadStationType == RoadStationType.TMS_STATION) {
            statisticsType = MqttRelayService.StatisticsType.TMS;
        } else {
            statisticsType = MqttRelayService.StatisticsType.WEATHER;
        }

        lastUpdated = roadStationSensorService.getSensorValueLastUpdated(roadStationType);

        if (lastUpdated == null) {
            lastUpdated = ZonedDateTime.now();
        }
    }

    // Implement this with @Scheduled
    public abstract void pollData();

    protected void handleData() {

        final boolean lockAcquired = lockingService.acquireLock(mqttClassName, 60);

        if (lockAcquired) {

            messageCounter++;

            final List<SensorValueDto> data = roadStationSensorService.findAllPublicNonObsoleteRoadStationSensorValuesUpdatedAfter(
                lastUpdated,
                roadStationType);

            // Listeners are notified every 10th time
            if (messageCounter >= 10) {
                try {
                    mqttRelay.sendMqttMessage(statusTopic, objectMapper.writeValueAsString(new StatusMessage(lastUpdated, lastError, "Ok", statisticsType.toString())));
                } catch (Exception e) {
                    logger.error("error sending status", e);
                }

                messageCounter = 0;
            }

            final AtomicInteger messagesCount = new AtomicInteger(0);

            data.forEach(sensorValueDto -> {
                lastUpdated = DateHelper.getNewest(lastUpdated, sensorValueDto.getUpdatedTime());

                try {
                    mqttRelay.sendMqttMessage(
                        String.format(messageTopic, sensorValueDto.getRoadStationNaturalId(), sensorValueDto.getSensorNaturalId()),
                        objectMapper.writeValueAsString(sensorValueDto));

                    messagesCount.incrementAndGet();

                    lastError = null;
                } catch (Exception e) {
                    lastError = ZonedDateTime.now();
                    logger.error("error sending message", e);
                }
            });

            mqttRelay.sentMqttStatistics(statisticsType, messagesCount.get());
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
}
