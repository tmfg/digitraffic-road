package fi.livi.digitraffic.tie.metadata.service.roadweather;

import java.util.List;
import java.util.Map;

import fi.livi.digitraffic.tie.metadata.geojson.roadweather.RoadWeatherStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.RoadWeatherSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadWeatherStation;

public interface RoadWeatherStationService {

    Map<Long, RoadWeatherStation> findAllRoadWeatherStationsMappedByLotjuId();

    RoadWeatherStation save(RoadWeatherStation roadWeatherStation);

    List<RoadWeatherSensor> findAllRoadStationSensors();

    Map<Long, List<RoadWeatherSensor>> findAllRoadStationSensorsMappedByRoadStationLotjuId();

    RoadWeatherSensor save(RoadWeatherSensor roadWeatherSensor);

    RoadWeatherStationFeatureCollection findAllNonObsoleteRoadWeatherStationAsFeatureCollection();
}
