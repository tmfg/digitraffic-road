package fi.livi.digitraffic.tie.data.service;

import static fi.ely.lotju.lam.proto.LAMRealtimeProtos.Lam;
import static fi.ely.lotju.tiesaa.proto.TiesaaProtos.TiesaaMittatieto;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.SensorValueDao;
import fi.livi.digitraffic.tie.data.dao.SensorValueUpdateParameterDto;
import fi.livi.digitraffic.tie.helper.TimestampCache;
import fi.livi.digitraffic.tie.metadata.dao.RoadStationDao;
import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.DataStatusService;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;

@Service
public class SensorDataUpdateService {
    private static final Logger log = LoggerFactory.getLogger(SensorDataUpdateService.class);

    private final HashMap<RoadStationType, Set<Long>> allowedSensorsLotjuIds = new HashMap<>();
    private final HashMap<RoadStationType, Long> allowedSensorsLastUpdatedTimeMillis = new HashMap<>();
    private static final long allowedSensorExpirationMillis = 300000; // 5min

    private final SensorValueDao sensorValueDao;
    private final RoadStationSensorService roadStationSensorService;
    private final RoadStationDao roadStationDao;
    private final DataStatusService dataStatusService;

    @Autowired
    public SensorDataUpdateService(final SensorValueDao sensorValueDao, final RoadStationSensorService roadStationSensorService,
                                   final RoadStationDao roadStationDao, final DataStatusService dataStatusService) {
        this.sensorValueDao = sensorValueDao;
        this.roadStationSensorService = roadStationSensorService;
        this.roadStationDao = roadStationDao;
        this.dataStatusService = dataStatusService;
    }

    private Set<Long> getAllowedRoadStationSensorsLotjuIds(final RoadStationType roadStationType) {
        if (allowedSensorsLotjuIds.get(roadStationType) == null || allowedSensorsLastUpdatedTimeMillis.get(roadStationType) < System.currentTimeMillis() - allowedSensorExpirationMillis) {
            final List<RoadStationSensor> allowedTmsSensors =
                roadStationSensorService.findAllPublishableRoadStationSensors(roadStationType);

            allowedSensorsLotjuIds.put(roadStationType, allowedTmsSensors.stream().map(s -> s.getLotjuId()).collect(Collectors.toSet()));

            allowedSensorsLastUpdatedTimeMillis.put(roadStationType, System.currentTimeMillis());
            log.info("method=getAllowedRoadStationSensorsLotjuIds fetched sensorCount={} for roadStationType={}", allowedSensorsLotjuIds.get(roadStationType).size(), roadStationType);
        }
        return allowedSensorsLotjuIds.get(roadStationType);
    }

    /**
     * Updates tms sensors data to db
     * @param data
     * @return count of updated db rows
     */
    @Transactional
    public int updateLamData(final List<Lam> data) {
        final StopWatch stopWatch = StopWatch.createStarted();

        Map<Long, Long> allowedStationsLotjuIdtoIds = roadStationDao.findPublishableRoadStationsIdsMappedByLotjuId(RoadStationType.TMS_STATION);

        final long initialDataRowCount = data.stream().mapToLong(lam -> lam.getAnturiList().size()).sum();

        final List<Lam> filteredByStation =
            data.stream().filter(lam -> allowedStationsLotjuIdtoIds.containsKey(lam.getAsemaId())).collect(Collectors.toList());
        final long filteredByStationRowCount = filteredByStation.stream().mapToLong(lam -> lam.getAnturiList().size()).sum();

        if (filteredByStation.size() < data.size()) {
            log.warn("method=updateLamData filter data from originalCount={} with missingTmsStationsCount={} to resultCount={}" ,
                     data.size(), data.size()-filteredByStation.size(), filteredByStation.size());
        }

        final List<LotjuAnturiWrapper<Lam.Anturi>> wrappedAnturiValues = wrapLamData(filteredByStation);
        final List<LotjuAnturiWrapper<Lam.Anturi>> filteredByNewest = filterNewestAnturiValues(wrappedAnturiValues);

        if (filteredByNewest.size() < filteredByStationRowCount) {
            log.info("method=updateLamData filter data rows from originalCount={} with oldDataCount={} to resultCount={}",
                     filteredByStationRowCount, filteredByStationRowCount-filteredByNewest.size(), filteredByNewest.size());
        }

        final long stationsCount = filteredByNewest.stream().map(a -> a.getAsemaLotjuId()).distinct().count();

        final TimestampCache timestampCache = new TimestampCache();

        List<SensorValueUpdateParameterDto> params =
            filteredByNewest.stream()
            .filter(wrapper -> getAllowedRoadStationSensorsLotjuIds(RoadStationType.TMS_STATION).contains(wrapper.getAnturi().getLaskennallinenAnturiId()))
                            .map(anturi -> new SensorValueUpdateParameterDto(anturi, allowedStationsLotjuIdtoIds.get(anturi.getAsemaLotjuId()), timestampCache))
            .collect(Collectors.toList());

        final Timestamp max = getMaxMeasured(params);

        final int rows = sensorValueDao.updateLamSensorData(params);
        dataStatusService.updateDataUpdated(DataType.getSensorValueMeasuredDataType(RoadStationType.TMS_STATION), max.toInstant());
        dataStatusService.updateDataUpdated(DataType.getSensorValueUpdatedDataType(RoadStationType.TMS_STATION),
                                            dataStatusService.getTransactionStartTime());

        stopWatch.stop();

        log.info("method=updateWeatherData initial data rowCount={} filtered to updateRowCount={}",
                 initialDataRowCount, filteredByNewest.size());
        log.info("method=updateLamData update tms sensors data for updateCount={} sensors of stationCount={} stations . hasRealtime={} . hasNonRealtime={} tookMs={}",
                 rows, stationsCount, filteredByStation.stream().anyMatch(lam -> lam.getIsRealtime()), filteredByStation.stream().anyMatch(lam -> !lam.getIsRealtime()), stopWatch.getTime());
        return rows;
    }

    private Timestamp getMaxMeasured(final List<SensorValueUpdateParameterDto> params) {
        final StopWatch stopWatch = StopWatch.createStarted();
        final Timestamp max = params.stream().max(Comparator.comparing(SensorValueUpdateParameterDto::getMeasured)).get().getMeasured();
        log.info("method=getMaxMeasured tookMs={}", stopWatch.getTime());
        return max;
    }

    /**
     * Updates weather data to db
     * @param data
     * @return count of updated db rows
     */
    @Transactional
    public int updateWeatherData(final List<TiesaaMittatieto> data) {
        final StopWatch stopWatch = StopWatch.createStarted();

        final Map<Long, Long> allowedStationsLotjuIdtoIds = roadStationDao.findPublishableRoadStationsIdsMappedByLotjuId(RoadStationType.WEATHER_STATION);

        final long initialDataRowCount = data.stream().mapToLong(tiesaa -> tiesaa.getAnturiList().size()).sum();

        final List<TiesaaMittatieto> filteredByStation =
            data.stream().filter(tiesaa -> allowedStationsLotjuIdtoIds.containsKey(tiesaa.getAsemaId())).collect(Collectors.toList());

        final long filteredByStationRowCount = filteredByStation.stream().mapToLong(lam -> lam.getAnturiList().size()).sum();

        if (filteredByStation.size() < data.size()) {
            log.warn("method=updateWeatherData filter data from originalCount={} with missingWeatherStationsCount={} to resultCount={}" ,
                     data.size(), data.size()-filteredByStation.size(), filteredByStation.size());
        }

        final List<LotjuAnturiWrapper<TiesaaMittatieto.Anturi>> wrappedAnturiValues = wrapTiesaaData(filteredByStation);
        final List<LotjuAnturiWrapper<TiesaaMittatieto.Anturi>> filteredByNewest = filterNewestAnturiValues(wrappedAnturiValues);

        if (filteredByNewest.size() < filteredByStationRowCount) {
            log.info("method=updateWeatherData filter data rows from originalCount={} with oldDataCount={} to resultCount={}",
                     filteredByStationRowCount, filteredByStationRowCount-filteredByNewest.size(), filteredByNewest.size());
        }

        final TimestampCache timestampCache = new TimestampCache();

        final long stationsCount = filteredByNewest.stream().map(a -> a.getAsemaLotjuId()).distinct().count();

        List<SensorValueUpdateParameterDto> params =
            filteredByNewest.stream()
                .filter(wrapper -> getAllowedRoadStationSensorsLotjuIds(RoadStationType.WEATHER_STATION).contains(wrapper.getAnturi().getLaskennallinenAnturiId()))
                .map(anturi -> new SensorValueUpdateParameterDto(anturi, timestampCache, allowedStationsLotjuIdtoIds.get(anturi.getAsemaLotjuId())))
                .collect(Collectors.toList());

        final Timestamp max = getMaxMeasured(params);
        final int rows = sensorValueDao.updateWeatherSensorData(params);
        dataStatusService.updateDataUpdated(DataType.getSensorValueMeasuredDataType(RoadStationType.WEATHER_STATION), max.toInstant());
        dataStatusService.updateDataUpdated(DataType.getSensorValueUpdatedDataType(RoadStationType.WEATHER_STATION),
                                            dataStatusService.getTransactionStartTime());
        stopWatch.stop();
        log.info("method=updateWeatherData initial data rowCount={} filtered to updateRowCount={}",
                 initialDataRowCount, filteredByNewest.size());
        log.info("method=updateWeatherData update weather sensors data for updateCount={} sensors of stationCount={} stations tookMs={}",
                 rows, stationsCount, stopWatch.getTime());
        return rows;
    }

    private static List<LotjuAnturiWrapper<Lam.Anturi>> wrapLamData(final List<Lam> lams) {
        return lams.stream()
            .flatMap(lam -> lam.getAnturiList().stream()
                .map(anturi -> new LotjuAnturiWrapper<>(lam.getAsemaId(), anturi.getLaskennallinenAnturiId(),
                                                        anturi, lam.getAika()))
            ).collect(Collectors.toList());
    }

    private static List<LotjuAnturiWrapper<TiesaaMittatieto.Anturi>> wrapTiesaaData(final List<TiesaaMittatieto> tiesaaMittatietos) {
        return tiesaaMittatietos.stream()
            .flatMap(tiesaa -> tiesaa.getAnturiList().stream()
                               .map(anturi -> new LotjuAnturiWrapper<>(tiesaa.getAsemaId(), anturi.getLaskennallinenAnturiId(),
                                                                       anturi, tiesaa.getAika()))
            ).collect(Collectors.toList());
    }

    private static <T> List<LotjuAnturiWrapper<T>> filterNewestAnturiValues(final List<LotjuAnturiWrapper<T>> wrappedValues) {
        final HashMap<Long, Map<Long, LotjuAnturiWrapper<T>>> stationIdToSensoridToSensorData = new HashMap<>();

        for (final LotjuAnturiWrapper<T> anturiCandidate : wrappedValues) {
            Map<Long, LotjuAnturiWrapper<T>> sensoridToSensorData = stationIdToSensoridToSensorData.get(anturiCandidate.getAsemaLotjuId());
            if (sensoridToSensorData == null) {
                sensoridToSensorData = new HashMap<>();
                stationIdToSensoridToSensorData.put(anturiCandidate.getAsemaLotjuId(), sensoridToSensorData);
            }
            LotjuAnturiWrapper<T> currentAnturi = sensoridToSensorData.get(anturiCandidate.getAnturiLotjuId());
            if (currentAnturi == null || currentAnturi.getAika() < anturiCandidate.getAika()) {
                sensoridToSensorData.put(anturiCandidate.getAnturiLotjuId(), anturiCandidate);
            }
        }

        return stationIdToSensoridToSensorData.values()
            .stream()
            .map(Map::values)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }
}
