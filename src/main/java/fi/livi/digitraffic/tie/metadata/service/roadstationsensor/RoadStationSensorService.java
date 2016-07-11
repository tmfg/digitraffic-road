package fi.livi.digitraffic.tie.metadata.service.roadstationsensor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import fi.livi.digitraffic.tie.data.dto.RoadStationSensorValueDto;
import fi.livi.digitraffic.tie.metadata.dto.RoadStationsSensorsMetadata;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;

public interface RoadStationSensorService {
    List<RoadStationSensor> findAllNonObsoleteRoadStationSensors();

    RoadStationsSensorsMetadata findRoadStationsSensorsMetadata();

    Map<Long, List<RoadStationSensorValueDto>> findAllNonObsoletePublicRoadWeatherStationSensorValues();

    LocalDateTime getLatestMeasurementTime();
}
