package fi.livi.digitraffic.tie.metadata.service.weather;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.converter.WeatherStationMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.metadata.dao.RoadStationSensorRepository;
import fi.livi.digitraffic.tie.metadata.dao.SensorValueRepository;
import fi.livi.digitraffic.tie.metadata.dao.WeatherStationRepository;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;
import fi.livi.digitraffic.tie.metadata.model.SensorValue;
import fi.livi.digitraffic.tie.metadata.model.WeatherStation;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;

@Service
public class WeatherStationServiceImpl implements WeatherStationService {

    private final WeatherStationRepository weatherStationRepository;
    private final RoadStationSensorRepository roadStationSensorRepository;
    private final SensorValueRepository sensorValueRepository;
    private final StaticDataStatusService staticDataStatusService;

    @Autowired
    public WeatherStationServiceImpl(final WeatherStationRepository weatherStationRepository,
                                     final RoadStationSensorRepository roadStationSensorRepository,
                                     final SensorValueRepository sensorValueRepository,
                                     final StaticDataStatusService staticDataStatusService) {

        this.weatherStationRepository = weatherStationRepository;
        this.roadStationSensorRepository = roadStationSensorRepository;
        this.sensorValueRepository = sensorValueRepository;
        this.staticDataStatusService = staticDataStatusService;
    }

    @Transactional(readOnly = true)
    @Override
    public Map<Long, WeatherStation> findAllWeatherStationsMappedByLotjuId() {
        final Map<Long, WeatherStation> map = new HashMap<>();
        final List<WeatherStation> all = weatherStationRepository.findAll();
        for (final WeatherStation weatherStation : all) {
            map.put(weatherStation.getLotjuId(), weatherStation);
        }
        return map;
    }

    @Transactional
    @Override
    public WeatherStation save(final WeatherStation weatherStation) {
        final WeatherStation rws = weatherStationRepository.save(weatherStation);
        weatherStationRepository.flush();
        return rws;
    }

    @Transactional(readOnly = true)
    @Override
    public List<SensorValue> findAllSensorValues() {
        return sensorValueRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public WeatherStation findByLotjuId(long lotjuId) {
        return weatherStationRepository.findByLotjuId(lotjuId);
    }

    @Transactional(readOnly = true)
    @Override
    public WeatherStationFeatureCollection findAllNonObsoletePublicWeatherStationAsFeatureCollection(final boolean onlyUpdateInfo) {

        final MetadataUpdated updated = staticDataStatusService.findMetadataUptadedByMetadataType(MetadataType.WEATHER_STATION);

        return WeatherStationMetadata2FeatureConverter.convert(
                onlyUpdateInfo == false ?
                weatherStationRepository.findByRoadStationObsoleteFalseAndRoadStationIsPublicTrueOrderByRoadStation_NaturalId() :
                Collections.emptyList(),
                updated != null ? updated.getUpdated() : null);
    }
}
