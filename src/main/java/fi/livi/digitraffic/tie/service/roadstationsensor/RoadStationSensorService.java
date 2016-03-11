package fi.livi.digitraffic.tie.service.roadstationsensor;

import java.util.List;

import fi.livi.digitraffic.tie.model.RoadStationSensor;

public interface RoadStationSensorService {
    List<RoadStationSensor> findAllNonObsoleteRoadStationSensors();
}
