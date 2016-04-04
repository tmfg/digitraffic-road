package fi.livi.digitraffic.tie.metadata.service.roadstationsensor;

import java.util.List;

import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;

public interface RoadStationSensorService {
    List<RoadStationSensor> findAllNonObsoleteRoadStationSensors();
}
