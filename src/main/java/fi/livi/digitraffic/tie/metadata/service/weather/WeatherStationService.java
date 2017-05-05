package fi.livi.digitraffic.tie.metadata.service.weather;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.metadata.converter.WeatherStationMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.metadata.dao.SensorValueRepository;
import fi.livi.digitraffic.tie.metadata.dao.WeatherStationRepository;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;
import fi.livi.digitraffic.tie.metadata.model.WeatherStation;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;

@Service
public class WeatherStationService {

    private static final Logger log = LoggerFactory.getLogger(WeatherStationService.class);
    private final WeatherStationRepository weatherStationRepository;
    private final SensorValueRepository sensorValueRepository;
    private final StaticDataStatusService staticDataStatusService;
    private final WeatherStationMetadata2FeatureConverter weatherStationMetadata2FeatureConverter;

    @Autowired
    public WeatherStationService(final WeatherStationRepository weatherStationRepository,
                                 final SensorValueRepository sensorValueRepository,
                                 final StaticDataStatusService staticDataStatusService,
                                 final WeatherStationMetadata2FeatureConverter weatherStationMetadata2FeatureConverter) {

        this.weatherStationRepository = weatherStationRepository;
        this.sensorValueRepository = sensorValueRepository;
        this.staticDataStatusService = staticDataStatusService;
        this.weatherStationMetadata2FeatureConverter = weatherStationMetadata2FeatureConverter;
    }

    @Transactional(readOnly = true)
    public Map<Long, WeatherStation> findAllWeatherStationsMappedByLotjuId() {
        final List<WeatherStation> all = weatherStationRepository.findAll();
        return all.stream().filter(ws -> ws.getLotjuId() != null).collect(Collectors.toMap(WeatherStation::getLotjuId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public Map<Long, WeatherStation> findAllPublishableWeatherStationsMappedByLotjuId() {
        final List<WeatherStation> all = findAllPublishableWeatherStations();
        return all.stream().collect(Collectors.toMap(p -> p.getLotjuId(), p -> p));
    }

    @Transactional
    public WeatherStation save(final WeatherStation weatherStation) {
        try {
            final WeatherStation rws = weatherStationRepository.save(weatherStation);
            weatherStationRepository.flush();
            return rws;
        } catch (Exception e) {
            log.error("Could not save " + weatherStation);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public WeatherStationFeatureCollection findAllPublishableWeatherStationAsFeatureCollection(final boolean onlyUpdateInfo) {
        final MetadataUpdated sensorsUpdated = staticDataStatusService.findMetadataUpdatedByMetadataType(MetadataType.WEATHER_STATION_SENSOR);
        final MetadataUpdated stationsUpdated = staticDataStatusService.findMetadataUpdatedByMetadataType(MetadataType.WEATHER_STATION);
        final ZonedDateTime updated = DateHelper.getNewest(sensorsUpdated != null ? sensorsUpdated.getUpdatedTime() : null,
                                                     stationsUpdated != null ? stationsUpdated.getUpdatedTime() : null);

        return weatherStationMetadata2FeatureConverter.convert(
                !onlyUpdateInfo ?
                    weatherStationRepository.findByRoadStationPublishableIsTrueOrderByRoadStation_NaturalId() :
                    Collections.emptyList(),
                updated);
    }

    @Transactional(readOnly = true)
    public List<WeatherStation> findAllPublishableWeatherStations() {
        return weatherStationRepository.findByRoadStationPublishableIsTrueOrderByRoadStation_NaturalId();
    }

    @Transactional(readOnly = true)
    public Map<Long, WeatherStation> findAllWeatherStationsWithoutLotjuIdMappedByByRoadStationNaturalId() {
        final List<WeatherStation> allStations = weatherStationRepository.findByLotjuIdIsNull();
        return allStations.stream().filter(ws -> ws.getRoadStationNaturalId() != null).collect(Collectors.toMap(WeatherStation::getRoadStationNaturalId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public List<WeatherStation> findAllWeatherStationsWithoutRoadStation() {
        return weatherStationRepository.findByRoadStationIsNull();
    }
}
