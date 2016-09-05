package fi.livi.digitraffic.tie.metadata.service.roadstationsensor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import fi.livi.digitraffic.tie.data.dto.SensorValueDto;
import fi.livi.digitraffic.tie.metadata.dto.RoadStationsSensorsMetadata;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.SensorValue;

public interface RoadStationSensorService {
    List<RoadStationSensor> findAllNonObsoleteRoadStationSensors(RoadStationType roadStationType);

    List<RoadStationSensor> findAllRoadStationSensors(RoadStationType roadStationType);

    Map<Long, RoadStationSensor> findAllRoadStationSensorsMappedByNaturalId(RoadStationType roadStationType);

    RoadStationsSensorsMetadata findRoadStationsSensorsMetadata(RoadStationType roadStationType, boolean onlyUpdateInfo);

    Map<Long, List<SensorValueDto>> findAllNonObsoletePublicRoadStationSensorValuesMappedByNaturalId(RoadStationType roadStationType);

    LocalDateTime getLatestMeasurementTime(RoadStationType roadStationType);

    List<SensorValueDto> findAllNonObsoletePublicRoadStationSensorValuesMappedByNaturalId(long roadStationNaturalId, RoadStationType roadStationType);

    RoadStationSensor saveRoadStationSensor(RoadStationSensor sensor);

    Map<Long,List<SensorValue>> findSensorvaluesListMappedByLamLotjuId(List<Long> lamLotjuIds, RoadStationType lamStation);
}
