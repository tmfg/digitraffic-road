package fi.livi.digitraffic.tie.metadata.service.roadweather;

import java.util.List;
import java.util.Map;

import fi.livi.digitraffic.tie.metadata.geojson.roadweather.RoadWeatherStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadWeatherStation;
import fi.livi.digitraffic.tie.metadata.model.SensorValue;

public interface RoadWeatherStationService {

    Map<Long, RoadWeatherStation> findAllRoadWeatherStationsMappedByLotjuId();

    RoadWeatherStation save(RoadWeatherStation roadWeatherStation);

    List<RoadStationSensor> findAllRoadStationSensors();

    Map<Long, RoadStationSensor> findAllRoadStationSensorsMappedByNaturalId();

    RoadStationSensor saveRoadStationSensor(RoadStationSensor roadStationSensor);

    RoadWeatherStationFeatureCollection findAllNonObsoletePublicRoadWeatherStationAsFeatureCollection(boolean onlyUpdateInfo);

    List<SensorValue> findAllSensorValues();
}
