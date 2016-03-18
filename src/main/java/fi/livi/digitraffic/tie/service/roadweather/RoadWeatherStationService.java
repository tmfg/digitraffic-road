package fi.livi.digitraffic.tie.service.roadweather;

import java.util.Map;

import fi.livi.digitraffic.tie.model.RoadWeatherStation;

public interface RoadWeatherStationService {

    Map<Long, RoadWeatherStation> findAllRoadWeatherStationsMappedByLotjuId();

    RoadWeatherStation save(RoadWeatherStation roadWeatherStation);

}
