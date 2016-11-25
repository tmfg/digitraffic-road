package fi.livi.digitraffic.tie.conf;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import fi.livi.digitraffic.tie.data.controller.SingleTmsDataWebsocketEndpoint;
import fi.livi.digitraffic.tie.data.controller.TmsDataWebsocketEndpoint;
import fi.livi.digitraffic.tie.data.dto.SensorValueDto;
import fi.livi.digitraffic.tie.data.websocket.TmsMessage;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;

@ConditionalOnProperty(name = "websocket.tms.enabled")
@Configuration
public class TmsWebSocketConfiguration {

    private ZonedDateTime lastUpdated = null;
    private final RoadStationSensorService roadStationSensorService;

    @Autowired
    public TmsWebSocketConfiguration(final RoadStationSensorService roadStationSensorService) {
        this.roadStationSensorService = roadStationSensorService;

        lastUpdated = roadStationSensorService.getSensorValueLastUpdated(RoadStationType.TMS_STATION);

        if (lastUpdated == null) {
            lastUpdated = ZonedDateTime.now();
        }
    }

    @Scheduled(fixedRateString = "${websocket.tms.pollingIntervalMs}")
    public void pollTmsData() {

        List<SensorValueDto> data =
                roadStationSensorService.findAllPublicNonObsoleteRoadStationSensorValuesUpdatedAfter(
                        lastUpdated,
                        RoadStationType.TMS_STATION);

        // Single TMS Station listeners are notified every time
        SingleTmsDataWebsocketEndpoint.sendStatus();
        if (data.isEmpty()) {
            TmsDataWebsocketEndpoint.sendStatus();
        }

        data.forEach(sensorValueDto -> {
            lastUpdated = DateHelper.getNewest(lastUpdated, sensorValueDto.getUpdatedTime());
            final TmsMessage message = new TmsMessage(sensorValueDto);
            TmsDataWebsocketEndpoint.sendMessage(message);
            SingleTmsDataWebsocketEndpoint.sendMessage(message);
        });
    }
}
