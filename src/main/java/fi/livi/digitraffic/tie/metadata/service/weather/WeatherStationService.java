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
import fi.livi.digitraffic.tie.metadata.dao.WeatherStationRepository;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.WeatherStation;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.TiesaaAsemaVO;

@Service
public class WeatherStationService {

    private static final Logger log = LoggerFactory.getLogger(WeatherStationService.class);
    private final WeatherStationRepository weatherStationRepository;
    private final StaticDataStatusService staticDataStatusService;
    private final RoadStationService roadStationService;
    private final WeatherStationMetadata2FeatureConverter weatherStationMetadata2FeatureConverter;

    @Autowired
    public WeatherStationService(final WeatherStationRepository weatherStationRepository,
                                 final StaticDataStatusService staticDataStatusService,
                                 final RoadStationService roadStationService,
                                 final WeatherStationMetadata2FeatureConverter weatherStationMetadata2FeatureConverter) {

        this.weatherStationRepository = weatherStationRepository;
        this.staticDataStatusService = staticDataStatusService;
        this.roadStationService = roadStationService;
        this.weatherStationMetadata2FeatureConverter = weatherStationMetadata2FeatureConverter;
    }

    @Transactional(readOnly = true)
    public Map<Long, WeatherStation> findAllWeatherStationsMappedByLotjuId() {
        final List<WeatherStation> all = weatherStationRepository.findAll();
        return all.parallelStream().filter(ws -> ws.getLotjuId() != null).collect(Collectors.toMap(WeatherStation::getLotjuId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public Map<Long, WeatherStation> findAllPublishableWeatherStationsMappedByLotjuId() {
        final List<WeatherStation> all = findAllPublishableWeatherStations();
        return all.parallelStream().collect(Collectors.toMap(p -> p.getLotjuId(), p -> p));
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

    @Transactional
    public void fixOrphanRoadStations() {
        List<WeatherStation> weatherStations =
            findAllWeatherStationsWithoutRoadStation();

        final Map<Long, RoadStation> orphansNaturalIdToRoadStationMap =
            roadStationService.findOrphansByTypeMappedByNaturalId(RoadStationType.WEATHER_STATION);

        weatherStations.forEach(ws -> {
            setRoadStationIfNotSet(ws, ws.getRoadStationNaturalId(), orphansNaturalIdToRoadStationMap);
            if (ws.getRoadStation().getId() == null) {
                roadStationService.save(ws.getRoadStation());
                log.info("Created new RoadStation " + ws.getRoadStation());
            }
        });
    }

    @Transactional
    public void fixNullLotjuIds(final List<TiesaaAsemaVO> tiesaaAsemas) {
        Map<Long, WeatherStation> naturalIdToWeatherStationMap =
            findAllWeatherStationsWithoutLotjuIdMappedByByRoadStationNaturalId();

        tiesaaAsemas.forEach(tiesaaAsema -> {

            WeatherStation ws = tiesaaAsema.getVanhaId() != null ?
                                naturalIdToWeatherStationMap.get(tiesaaAsema.getVanhaId().longValue()) : null;
            if (ws != null) {
                ws.setLotjuId(tiesaaAsema.getId());
                ws.getRoadStation().setLotjuId(tiesaaAsema.getId());
            }
        });
    }

    private static void setRoadStationIfNotSet(WeatherStation rws, Long tsaVanhaId, Map<Long, RoadStation> orphansNaturalIdToRoadStationMap) {
        RoadStation rs = rws.getRoadStation();

        if (rs == null) {
            rs = tsaVanhaId != null ? orphansNaturalIdToRoadStationMap.remove(tsaVanhaId) : null;
            if (rs == null) {
                rs = new RoadStation(RoadStationType.WEATHER_STATION);
            }
            rws.setRoadStation(rs);
        }
    }

    @Transactional
    public void save(WeatherStation rws) {
        weatherStationRepository.save(rws);
    }
}
