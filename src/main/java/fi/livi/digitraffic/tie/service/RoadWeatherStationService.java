package fi.livi.digitraffic.tie.service;

import java.util.Map;

import fi.livi.digitraffic.tie.model.RoadWeatherStation;

public interface RoadWeatherStationService {

    Map<Long, RoadWeatherStation> findAllRoadWeatherStationsMappedByLotjuId();

}
