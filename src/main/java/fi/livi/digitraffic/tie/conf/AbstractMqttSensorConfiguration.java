package fi.livi.digitraffic.tie.conf;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.data.dto.SensorValueDto;
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

    private ZonedDateTime lastUpdated;
    private MqttRelayService.StatisticsType StatisticsType;
    private int counter = 0;
    private int emptyDataCounter = 0;

    public AbstractMqttSensorConfiguration(final MqttRelayService mqttRelay,
                                           final RoadStationSensorService roadStationSensorService,
                                           final ObjectMapper objectMapper,
                                           final RoadStationType roadStationType,

                                           final String statusTopic,
                                           final String messageTopic,
                                           final Logger logger) {

        this.mqttRelay = mqttRelay;
        this.roadStationSensorService = roadStationSensorService;
        this.objectMapper = objectMapper;
        this.roadStationType = roadStationType;
        this.statusTopic = statusTopic;
        this.messageTopic = messageTopic;
        this.logger = logger;

        if (roadStationType == RoadStationType.TMS_STATION) {
            StatisticsType = MqttRelayService.StatisticsType.TMS;
        } else {
            StatisticsType = MqttRelayService.StatisticsType.WEATHER;
        }

        lastUpdated = roadStationSensorService.getSensorValueLastUpdated(roadStationType);

        if (lastUpdated == null) {
            lastUpdated = ZonedDateTime.now();
        }
    }

    // Implement this with @Scheduled
    public abstract void pollData();

    protected void handleData() {
        counter++;

        final List<SensorValueDto> data = roadStationSensorService.findAllPublicNonObsoleteRoadStationSensorValuesUpdatedAfter(
            lastUpdated,
            roadStationType);

        if (data.isEmpty()) {
            emptyDataCounter++;
        } else {
            emptyDataCounter = 0;
        }

        // Listeners are notified every 10th time
        if (counter >= 10) {
            try {
                mqttRelay.sendMqttMessage(statusTopic, emptyDataCounter < 10 ? MqttRelayService.statusOK : MqttRelayService.statusNOCONTENT);
            } catch (Exception e) {
                logger.error("error sending status", e);
            }

            counter = 0;
        }

        final AtomicInteger messagesCount = new AtomicInteger(0);

        data.forEach(sensorValueDto -> {
            lastUpdated = DateHelper.getNewest(lastUpdated, sensorValueDto.getUpdatedTime());

            try {
                mqttRelay.sendMqttMessage(
                    String.format(messageTopic, sensorValueDto.getRoadStationNaturalId(), sensorValueDto.getSensorNaturalId()),
                    objectMapper.writeValueAsString(sensorValueDto));

                messagesCount.incrementAndGet();
            } catch (Exception e) {
                logger.error("error sending message", e);
            }
        });

        mqttRelay.sentMqttStatistics(StatisticsType, messagesCount.get());
    }
}
