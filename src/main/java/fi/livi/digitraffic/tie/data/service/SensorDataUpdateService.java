package fi.livi.digitraffic.tie.data.service;

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

import fi.ely.lotju.lam.proto.LAMRealtimeProtos;
import fi.ely.lotju.tiesaa.proto.TiesaaProtos;
import fi.livi.digitraffic.tie.data.dao.SensorValueUpdateParameterDto;
import fi.livi.digitraffic.tie.data.dao.SensorValueDao;
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
            roadStationSensorService.findAllNonObsoleteAndAllowedRoadStationSensors(RoadStationType.TMS_STATION);
        allowedTmsSensorLotjuIds = allowedTmsSensors.stream().map(s -> s.getLotjuId()).collect(Collectors.toSet());

        final List<RoadStationSensor> allowedWeatherSensors =
            roadStationSensorService.findAllNonObsoleteAndAllowedRoadStationSensors(RoadStationType.WEATHER_STATION);
        allowedWeatherSensorLotjuIds = allowedWeatherSensors.stream().map(s -> s.getLotjuId()).collect(Collectors.toSet());
    }

    /**
     * Updates tms sensors data to db
     * @param data
     * @return count of updated db rows
     */
    @Transactional
    public int updateLamData(final List<LAMRealtimeProtos.Lam> data) {
        final StopWatch stopWatch = StopWatch.createStarted();
        final Collection<LAMRealtimeProtos.Lam> filteredByNewest = filterNewestLamValues(data);

        if (data.size()-filteredByNewest.size() > 0) {
            log.info("method=updateLamData filtered={} tms stations from originalCount={} to filteredCount={}",  data.size()-filteredByNewest.size(), data.size(), filteredByNewest.size());
        }

        Map<Long, Long> allowedStationsLotjuIdtoIds = roadStationDao.findPublishableRoadStationsIdsMappedByLotjuId(RoadStationType.TMS_STATION);

        final List<LAMRealtimeProtos.Lam> filteredByStation =
            filteredByNewest.stream().filter(lam -> allowedStationsLotjuIdtoIds.containsKey(lam.getAsemaId())).collect(Collectors.toList());

        if (filteredByStation.size() < filteredByNewest.size()) {
            log.warn("method=updateLamData filtered data for {} missing tms stations" , filteredByNewest.size()-filteredByStation.size());
        }

        final TimestampCache timestampCache = new TimestampCache();

        List<SensorValueUpdateParameterDto> params = filteredByStation.stream()
            .flatMap(lam -> lam.getAnturiList().stream()
                            .filter(anturi -> allowedTmsSensorLotjuIds.contains(anturi.getLaskennallinenAnturiId()))
                            .map(anturi -> new SensorValueUpdateParameterDto(lam, anturi, allowedStationsLotjuIdtoIds.get(lam.getAsemaId()), timestampCache)))
            .collect(Collectors.toList());

        final int rows = sensorValueDao.updateLamSensorData(params);
        stopWatch.stop();
        log.info("method=updateLamData Update tms sensors data for rows={} sensors of stationCount={} stations . hasRealtime={} . hasNonRealtime={} tookMs={}",
                 rows, filteredByStation.size(), data.stream().anyMatch(lam -> lam.getIsRealtime()), data.stream().anyMatch(lam -> !lam.getIsRealtime()), stopWatch.getTime());
        return rows;
    }

    /**
     * Updates weather data to db
     * @param data
     * @return count of updated db rows
     */
    @Transactional
    public int updateWeatherData(final List<TiesaaProtos.TiesaaMittatieto> data) {
        final StopWatch stopWatch = StopWatch.createStarted();
        final Collection<TiesaaProtos.TiesaaMittatieto> filteredByNewest = filterNewestTiesaaValues(data);

        if (data.size()-filteredByNewest.size() > 0) {
            log.info("method=updateWeatherData filtered={} weather stations from originalCount={} to filteredCount={}",  data.size()-filteredByNewest.size(), data.size(), filteredByNewest.size());
        }

        Map<Long, Long> allowedStationsLotjuIdtoIds = roadStationDao.findPublishableRoadStationsIdsMappedByLotjuId(RoadStationType.WEATHER_STATION);

        final List<TiesaaProtos.TiesaaMittatieto> filteredByStation =
            filteredByNewest.stream().filter(tiesaa -> allowedStationsLotjuIdtoIds.containsKey(tiesaa.getAsemaId())).collect(Collectors.toList());

        if (filteredByStation.size() < filteredByNewest.size()) {
            log.warn("method=updateWeatherData filtered data for {} missing weather stations" , filteredByNewest.size()-filteredByStation.size());
        }

        final TimestampCache timestampCache = new TimestampCache();

        List<SensorValueUpdateParameterDto> params = filteredByStation.stream()
            .flatMap(tiesaa -> tiesaa.getAnturiList().stream()
                               .filter(anturi -> allowedWeatherSensorLotjuIds.contains(anturi.getLaskennallinenAnturiId()))
                               .map(anturi -> new SensorValueUpdateParameterDto(tiesaa, anturi, allowedStationsLotjuIdtoIds.get(tiesaa.getAsemaId()), timestampCache)))
            .collect(Collectors.toList());

        final int rows = sensorValueDao.updateWeatherSensorData(params);
        stopWatch.stop();
        log.info("method=updateWeatherData Update weather sensors data for rows={} sensors of stationCount={} stations tookMs={}",
                 rows, filteredByStation.size(), stopWatch.getTime());
        return rows;
    }

    private static Collection<LAMRealtimeProtos.Lam> filterNewestLamValues(final List<LAMRealtimeProtos.Lam> data) {
        // Collect newest data per station
        final HashMap<Long, LAMRealtimeProtos.Lam> tmsMapByLamStationLotjuId = new HashMap<>();

        for (final LAMRealtimeProtos.Lam lam : data) {
            final LAMRealtimeProtos.Lam currentLam = tmsMapByLamStationLotjuId.get(lam.getAsemaId());
            if (currentLam == null || lam.getAika() > currentLam.getAika()) {
                if (currentLam != null) {
                    log.debug("Replace lam " + currentLam.getAika() + " with " + lam.getAika());
                }
                tmsMapByLamStationLotjuId.put(lam.getAsemaId(), lam);
            }
        }
        return tmsMapByLamStationLotjuId.values();
    }

    private static Collection<TiesaaProtos.TiesaaMittatieto> filterNewestTiesaaValues(final List<TiesaaProtos.TiesaaMittatieto> data) {
        // Collect newest data per station
        final HashMap<Long, TiesaaProtos.TiesaaMittatieto> tiesaaMapByTmsStationLotjuId = new HashMap<>();

        for (final TiesaaProtos.TiesaaMittatieto tiesaaCandidate : data) {
            final TiesaaProtos.TiesaaMittatieto currentTiesaa = tiesaaMapByTmsStationLotjuId.get(tiesaaCandidate.getAsemaId());

            if (currentTiesaa == null || currentTiesaa.getAika() < tiesaaCandidate.getAika()) {
                if (currentTiesaa != null) {
                    log.debug("Replace tiesaa " + currentTiesaa.getAika() + " with " + tiesaaCandidate.getAika());
                }
                tiesaaMapByTmsStationLotjuId.put(tiesaaCandidate.getAsemaId(), tiesaaCandidate);
            }
        }
        return tiesaaMapByTmsStationLotjuId.values();
    }
}
