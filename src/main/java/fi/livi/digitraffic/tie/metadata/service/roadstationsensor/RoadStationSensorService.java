package fi.livi.digitraffic.tie.metadata.service.roadstationsensor;

import java.util.List;
import java.util.Map;

import fi.livi.digitraffic.tie.data.model.dto.RoadStationSensorValueDto;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;

public interface RoadStationSensorService {
    List<RoadStationSensor> findAllNonObsoleteRoadStationSensors();

    Map<Long, List<RoadStationSensorValueDto>> findAllNonObsoleteRoadWeatherStationSensorValues();
}
