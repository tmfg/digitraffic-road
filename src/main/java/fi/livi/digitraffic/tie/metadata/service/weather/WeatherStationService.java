package fi.livi.digitraffic.tie.metadata.service.weather;

import static fi.livi.digitraffic.tie.helper.DateHelper.getNewest;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.converter.WeatherStationMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.metadata.dao.WeatherStationRepository;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.WeatherStation;
import fi.livi.digitraffic.tie.metadata.model.WeatherStationType;
import fi.livi.digitraffic.tie.metadata.service.DataStatusService;
import fi.livi.digitraffic.tie.metadata.service.UpdateStatus;
import fi.livi.ws.wsdl.lotju.tiesaa._2017._05._02.TiesaaAsemaVO;

@Service
public class WeatherStationService extends AbstractWeatherStationAttributeUpdater {

    private static final Logger log = LoggerFactory.getLogger(WeatherStationService.class);
    private final WeatherStationRepository weatherStationRepository;
    private final DataStatusService dataStatusService;
    private final WeatherStationMetadata2FeatureConverter weatherStationMetadata2FeatureConverter;

    @Autowired
    public WeatherStationService(final WeatherStationRepository weatherStationRepository,
                                 final DataStatusService dataStatusService,
                                 final WeatherStationMetadata2FeatureConverter weatherStationMetadata2FeatureConverter) {
        super(log);
        this.weatherStationRepository = weatherStationRepository;
        this.dataStatusService = dataStatusService;
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
        return weatherStationMetadata2FeatureConverter.convert(
                !onlyUpdateInfo ?
                    weatherStationRepository.findByRoadStationPublishableIsTrueOrderByRoadStation_NaturalId() :
                    Collections.emptyList(),
                getMetadataLastUpdated(),
                getMetadataLastChecked());
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

    @Transactional
    public void fixNullLotjuIds(final List<TiesaaAsemaVO> tiesaaAsemas) {
        Map<Long, WeatherStation> naturalIdToWeatherStationMap =
            findAllWeatherStationsWithoutLotjuIdMappedByByRoadStationNaturalId();

        int updated = 0;
        for( TiesaaAsemaVO tiesaaAsema : tiesaaAsemas) {
            WeatherStation ws = tiesaaAsema.getVanhaId() != null ?
                                naturalIdToWeatherStationMap.get(tiesaaAsema.getVanhaId().longValue()) : null;
            if (ws != null) {
                ws.setLotjuId(tiesaaAsema.getId());
                ws.getRoadStation().setLotjuId(tiesaaAsema.getId());
                updated++;
            }
        }
        if (updated > 0) {
            log.info("Fixed null lotjuIds for updatedCount={} weather stations", updated);
        }
    }

    @Transactional(readOnly = true)
    public WeatherStation findWeatherStationByLotjuId(Long tsaLotjuId) {
        return weatherStationRepository.findByLotjuId(tsaLotjuId);
    }

    @Transactional
    public UpdateStatus updateOrInsertWeatherStation(TiesaaAsemaVO tiesaaAsema) {
        WeatherStation rws = findWeatherStationByLotjuId(tiesaaAsema.getId());

        try {
            if (rws != null) {
                final int hash = HashCodeBuilder.reflectionHashCode(rws);
                final String before = ReflectionToStringBuilder.toString(rws);

                final RoadStation rs = rws.getRoadStation();
                setRoadAddressIfNotSet(rs);

                if (updateWeatherStationAttributes(tiesaaAsema, rws) ||
                    hash != HashCodeBuilder.reflectionHashCode(rws)) {
                    log.info("Updated: \n{} -> \n{}", before, ReflectionToStringBuilder.toString(rws));
                    return UpdateStatus.UPDATED;
                }
                return UpdateStatus.NOT_UPDATED;
            } else {
                rws = new WeatherStation();
                rws.setRoadStation(new RoadStation(RoadStationType.WEATHER_STATION));
                setRoadAddressIfNotSet(rws.getRoadStation());
                updateWeatherStationAttributes(tiesaaAsema, rws);
                weatherStationRepository.save(rws);
                log.info("Created new {}", rws);
                return UpdateStatus.INSERTED;
            }
        } finally {
            // Needed for tests to work :(
            weatherStationRepository.flush();
        }
    }

    private static boolean updateWeatherStationAttributes(final TiesaaAsemaVO from,
                                                          final WeatherStation to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);

        to.setLotjuId(from.getId());
        to.setMaster(from.isMaster() != null ? from.isMaster() : true);
        to.setWeatherStationType(WeatherStationType.fromTiesaaAsemaTyyppi(from.getTyyppi()));

        // Update RoadStation
        return updateRoadStationAttributes(from, to.getRoadStation()) ||
            HashCodeBuilder.reflectionHashCode(to) != hash;
    }

    private ZonedDateTime getMetadataLastUpdated() {
        final ZonedDateTime sensorsUpdated = dataStatusService.findDataUpdatedTimeByDataType(DataType.WEATHER_STATION_SENSOR_METADATA);
        final ZonedDateTime stationsUpdated = dataStatusService.findDataUpdatedTimeByDataType(DataType.WEATHER_STATION_METADATA);
        return getNewest(sensorsUpdated, stationsUpdated);
    }

    public ZonedDateTime getMetadataLastChecked() {
        final ZonedDateTime sensorsUpdated = dataStatusService.findDataUpdatedTimeByDataType(DataType.WEATHER_STATION_SENSOR_METADATA_CHECK);
        final ZonedDateTime stationsUpdated = dataStatusService.findDataUpdatedTimeByDataType(DataType.WEATHER_STATION_METADATA_CHECK);
        return getNewest(sensorsUpdated, stationsUpdated);
    }
}
