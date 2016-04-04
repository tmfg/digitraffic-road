package fi.livi.digitraffic.tie.metadata.service.roadweather;

import java.util.Map;

import fi.livi.digitraffic.tie.metadata.model.RoadWeatherStation;

public interface RoadWeatherStationService {

    Map<Long, RoadWeatherStation> findAllRoadWeatherStationsMappedByLotjuId();

    RoadWeatherStation save(RoadWeatherStation roadWeatherStation);

}
