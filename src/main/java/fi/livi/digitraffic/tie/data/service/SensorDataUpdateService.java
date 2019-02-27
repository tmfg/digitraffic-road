package fi.livi.digitraffic.tie.data.service;

import static fi.ely.lotju.lam.proto.LAMRealtimeProtos.Lam;
import static fi.ely.lotju.tiesaa.proto.TiesaaProtos.TiesaaMittatieto;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.ely.lotju.lam.proto.LAMRealtimeProtos.Lam.Anturi;
import fi.livi.digitraffic.tie.data.dao.SensorValueDao;
import fi.livi.digitraffic.tie.data.dao.SensorValueUpdateParameterDto;
import fi.livi.digitraffic.tie.helper.TimestampCache;
import fi.livi.digitraffic.tie.metadata.dao.RoadStationDao;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;

@Service
public class SensorDataUpdateService {
    private static final Logger log = LoggerFactory.getLogger(SensorDataUpdateService.class);

    private final Set<Long> allowedTmsSensorLotjuIds;
    private final Set<Long> allowedWeatherSensorLotjuIds;

    private final SensorValueDao sensorValueDao;
    private final RoadStationDao roadStationDao;

    @Autowired
    public SensorDataUpdateService(final SensorValueDao sensorValueDao, final RoadStationSensorService roadStationSensorService,
                                   final RoadStationDao roadStationDao) {
        this.sensorValueDao = sensorValueDao;
        this.roadStationDao = roadStationDao;

        final List<RoadStationSensor> allowedTmsSensors =
            roadStationSensorService.findAllPublishableRoadStationSensors(RoadStationType.TMS_STATION);
        allowedTmsSensorLotjuIds = allowedTmsSensors.stream().map(s -> s.getLotjuId()).collect(Collectors.toSet());

        final List<RoadStationSensor> allowedWeatherSensors =
            roadStationSensorService.findAllPublishableRoadStationSensors(RoadStationType.WEATHER_STATION);
        allowedWeatherSensorLotjuIds = allowedWeatherSensors.stream().map(s -> s.getLotjuId()).collect(Collectors.toSet());
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

        final long initialDataRows = data.stream().map(lam -> lam.getAnturiList())
            .flatMap(Collection::stream).count();

        final List<Lam> filteredByStation =
            data.stream().filter(lam -> allowedStationsLotjuIdtoIds.containsKey(lam.getAsemaId())).collect(Collectors.toList());
        final long filteredByStationRows = filteredByStation.stream().map(lam -> lam.getAnturiList())
            .flatMap(Collection::stream).count();

        if (filteredByStation.size() < data.size()) {
            log.warn("method=updateLamData filter data from originalCount={} with missingTmsStationsCount={} to resultCount={}" ,
                     data.size(), data.size()-filteredByStation.size(), filteredByStation.size());
        }


        final List<LotjuAnturiWrapper<Anturi>> filteredByNewest = filterNewestLamValues(filteredByStation);

        if (filteredByNewest.size() < filteredByStationRows) {
            log.info("method=updateLamData filter data rows from originalCount={} with oldDataCount={} to resultCount={}",
                     filteredByStationRows, filteredByStationRows-filteredByNewest.size(), filteredByNewest.size());
        }

        final long stationsCount = filteredByNewest.stream().map(a -> a.getAsemaLotjuId()).distinct().count();

        final TimestampCache timestampCache = new TimestampCache();

        List<SensorValueUpdateParameterDto> params =
            filteredByNewest.stream()
            .filter(wrapper -> allowedTmsSensorLotjuIds.contains(wrapper.getAnturi().getLaskennallinenAnturiId()))
                            .map(anturi -> new SensorValueUpdateParameterDto(anturi, allowedStationsLotjuIdtoIds.get(anturi.getAsemaLotjuId()), timestampCache))
            .collect(Collectors.toList());

        final int rows = sensorValueDao.updateLamSensorData(params);
        stopWatch.stop();

        log.info("method=updateWeatherData initial data rowCount={} filtered to updateRowCount={}",
                 initialDataRows, filteredByNewest.size());
        log.info("method=updateLamData update tms sensors data for updateCount={} sensors of stationCount={} stations . hasRealtime={} . hasNonRealtime={} tookMs={}",
                 rows, stationsCount, filteredByStation.stream().anyMatch(lam -> lam.getIsRealtime()), filteredByStation.stream().anyMatch(lam -> !lam.getIsRealtime()), stopWatch.getTime());
        return rows;
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

        final long initialDataRows = data.stream().map(tiesaa -> tiesaa.getAnturiList())
            .flatMap(Collection::stream).count();

        final List<TiesaaMittatieto> filteredByStation =
            data.stream().filter(tiesaa -> allowedStationsLotjuIdtoIds.containsKey(tiesaa.getAsemaId())).collect(Collectors.toList());

        final long filteredByStationRows = filteredByStation.stream().map(lam -> lam.getAnturiList())
            .flatMap(Collection::stream).count();

        if (filteredByStation.size() < data.size()) {
            log.warn("method=updateWeatherData filter data from originalCount={} with missingWeatherStationsCount={} to resultCount={}" ,
                     data.size(), data.size()-filteredByStation.size(), filteredByStation.size());
        }

        final List<LotjuAnturiWrapper<TiesaaMittatieto.Anturi>> filteredByNewest = filterNewestTiesaaValues(filteredByStation);

        if (filteredByNewest.size() < filteredByStationRows) {
            log.info("method=updateWeatherData filter data rows from originalCount={} with oldDataCount={} to resultCount={}",
                     filteredByStationRows, filteredByStationRows-filteredByNewest.size(), filteredByNewest.size());
        }

        final TimestampCache timestampCache = new TimestampCache();

        final long stationsCount = filteredByNewest.stream().map(a -> a.getAsemaLotjuId()).distinct().count();

        List<SensorValueUpdateParameterDto> params =
            filteredByNewest.stream()
                .filter(wrapper -> allowedWeatherSensorLotjuIds.contains(wrapper.getAnturi().getLaskennallinenAnturiId()))
                .map(anturi -> new SensorValueUpdateParameterDto(anturi, timestampCache, allowedStationsLotjuIdtoIds.get(anturi.getAsemaLotjuId())))
                .collect(Collectors.toList());

        final int rows = sensorValueDao.updateWeatherSensorData(params);
        stopWatch.stop();
        log.info("method=updateWeatherData initial data rowCount={} filtered to updateRowCount={}",
                 initialDataRows, filteredByNewest.size());
        log.info("method=updateWeatherData update weather sensors data for updateCount={} sensors of stationCount={} stations tookMs={}",
                 rows, stationsCount, stopWatch.getTime());
        return rows;
    }


    private List<LotjuAnturiWrapper<Lam.Anturi>> filterNewestLamValues(final List<Lam> data) {

        final HashMap<Long, Map<Long, LotjuAnturiWrapper<Anturi>>> stationIdToSensoridToSensorData = new HashMap<>();

        for (final Lam lamCandidate : data) {
            Map<Long, LotjuAnturiWrapper<Anturi>> sensoridToSensorData = stationIdToSensoridToSensorData.get(lamCandidate.getAsemaId());
            if (sensoridToSensorData == null) {
                sensoridToSensorData = new HashMap<>();
                stationIdToSensoridToSensorData.put(lamCandidate.getAsemaId(), sensoridToSensorData);
            }
            for(Anturi anturiCandidate :lamCandidate.getAnturiList()) {
                final LotjuAnturiWrapper currentAnturi = sensoridToSensorData.get(anturiCandidate.getLaskennallinenAnturiId());
                if (currentAnturi == null || currentAnturi.getAika() < lamCandidate.getAika()) {
                    sensoridToSensorData.put(anturiCandidate.getLaskennallinenAnturiId(),
                        new LotjuAnturiWrapper(anturiCandidate, lamCandidate.getAika(), lamCandidate.getAsemaId()));
                }
            }
        }

        return stationIdToSensoridToSensorData.values()
            .stream()
            .map(Map::values)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    private List<LotjuAnturiWrapper<TiesaaMittatieto.Anturi>> filterNewestTiesaaValues(final List<TiesaaMittatieto> data) {

        final HashMap<Long, Map<Long, LotjuAnturiWrapper<TiesaaMittatieto.Anturi>>> stationIdToSensoridToSensorData = new HashMap<>();

        for (final TiesaaMittatieto tiesaaCandidate : data) {
            Map<Long, LotjuAnturiWrapper<TiesaaMittatieto.Anturi>> sensoridToSensorData = stationIdToSensoridToSensorData.get(tiesaaCandidate.getAsemaId());
            if (sensoridToSensorData == null) {
                sensoridToSensorData = new HashMap<>();
                stationIdToSensoridToSensorData.put(tiesaaCandidate.getAsemaId(), sensoridToSensorData);
            }
            for(TiesaaMittatieto.Anturi anturiCandidate :tiesaaCandidate.getAnturiList()) {
                final LotjuAnturiWrapper currentAnturi = sensoridToSensorData.get(anturiCandidate.getLaskennallinenAnturiId());
                if (currentAnturi == null || currentAnturi.getAika() < tiesaaCandidate.getAika()) {
                    sensoridToSensorData.put(anturiCandidate.getLaskennallinenAnturiId(),
                        new LotjuAnturiWrapper<>(anturiCandidate, tiesaaCandidate.getAika(), tiesaaCandidate.getAsemaId()));
                }
            }
        }

        return stationIdToSensoridToSensorData.values()
            .stream()
            .map(Map::values)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }
}
