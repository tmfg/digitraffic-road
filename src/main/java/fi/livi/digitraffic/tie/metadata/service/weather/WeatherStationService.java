package fi.livi.digitraffic.tie.metadata.service.weather;

import java.util.List;
import java.util.Map;

import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.SensorValue;
import fi.livi.digitraffic.tie.metadata.model.WeatherStation;

public interface WeatherStationService {

    Map<Long, WeatherStation> findAllWeatherStationsMappedByLotjuId();

    WeatherStation save(WeatherStation weatherStation);

    WeatherStationFeatureCollection findAllNonObsoletePublicWeatherStationAsFeatureCollection(boolean onlyUpdateInfo);

    List<SensorValue> findAllSensorValues();

    WeatherStation findByLotjuId(long lotjuId);
}
