package fi.livi.digitraffic.tie.conf;

import java.time.ZonedDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.data.dto.SensorValueDto;

import fi.livi.digitraffic.tie.data.service.TmsDataStatistics;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;

@ConditionalOnProperty("mqtt.tms.enabled")
@Component
public class TmsMqttConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(TmsMqttConfiguration.class);

    @Lazy // this will not be available if mqtt is not enabled
    private final MqttConfig.VesselGateway vesselGateway;
    private final RoadStationSensorService roadStationSensorService;
    private final ObjectMapper objectMapper;

    private final String TMS_TOPIC = "tms/data/%d";
    private final String TMS_STATUS_TOPIC = "tms/status";

    private ZonedDateTime lastUpdated = null;
    private int counter = 0;

    private final String statusOK = "{\"status\": \"OK\"}";

    @Autowired
    public TmsMqttConfiguration(final MqttConfig.VesselGateway vesselGateway,
                                final RoadStationSensorService roadStationSensorService,
                                final ObjectMapper objectMapper) {
        this.vesselGateway = vesselGateway;
        this.roadStationSensorService = roadStationSensorService;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${mqtt.tms.pollingIntervalMs}")
    public void pollTmsData() {
        counter++;

        final List<SensorValueDto> data = roadStationSensorService.findAllPublicNonObsoleteRoadStationSensorValuesUpdatedAfter(
                lastUpdated,
                RoadStationType.TMS_STATION);

        // Listeners are notified every 10th time
        if (counter >= 10) {
            try {
                sendMessage(statusOK, TMS_STATUS_TOPIC);
            } catch (Exception e) {
                logger.error("error sending status", e);
            }

            counter = 0;
        }

        data.forEach(sensorValueDto -> {
            lastUpdated = DateHelper.getNewest(lastUpdated, sensorValueDto.getUpdatedTime());

            try {
                final String messageAsString = objectMapper.writeValueAsString(sensorValueDto);

                sendMessage(messageAsString, String.format(TMS_TOPIC, sensorValueDto.getSensorNaturalId()));

                TmsDataStatistics.sentTmsStatistics(TmsDataStatistics.ConnectionType.MQTT, 0, 1);
            } catch (Exception e) {
                logger.error("error sending message", e);
            }
        });
    }

    // This must be synchronized, because Paho does not support concurrency!
    private synchronized void sendMessage(final String payLoad, final String topic) {
        vesselGateway.sendToMqtt(topic, payLoad);
    }
}
