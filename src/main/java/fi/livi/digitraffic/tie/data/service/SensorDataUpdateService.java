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
import fi.livi.digitraffic.tie.data.dao.SensorValueDao;
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
        final Collection<LAMRealtimeProtos.Lam> filtered = filterNewestLamValues(data);

        Map<Long, Long> allowedStationsLotjuIdtoIds = roadStationDao.findPublishableRoadStationsIdsMappedByLotjuId(RoadStationType.TMS_STATION);

        if (data.size()-filtered.size() > 0) {
            log.info("filtered={} tms station messages of original count={} -> filteredCount={} messages updated",  data.size()-filtered.size(), data.size(), filtered.size());
        }
        final int rows = sensorValueDao.updateLamSensorData(filtered, allowedTmsSensorLotjuIds, allowedStationsLotjuIdtoIds);
        stopWatch.stop();
        log.info("Update tms sensors data for rows={} sensors of filteredCount={} stations . hasRealtime={} . hasNonRealtime={} tookMs={}",
                 rows, filtered.size(), data.stream().anyMatch(lam -> lam.getIsRealtime()), data.stream().anyMatch(lam -> !lam.getIsRealtime()), stopWatch.getTime());
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
        final Collection<TiesaaProtos.TiesaaMittatieto> filtered = filterNewestTiesaaValues(data);
        Map<Long, Long> allowedStationsLotjuIdtoIds = roadStationDao.findPublishableRoadStationsIdsMappedByLotjuId(RoadStationType.WEATHER_STATION);

        if (data.size()-filtered.size() > 0) {
            log.info("filtered={} weather station messages of original dataCount={} -> filteredCount={} messages updated",  data.size()-filtered.size(), data.size(), filtered.size());
        }
        final int rows = sensorValueDao.updateWeatherSensorData(filtered, allowedWeatherSensorLotjuIds, allowedStationsLotjuIdtoIds);
        stopWatch.stop();
        log.info("Update weather sensors data for rows={} sensors of filteredCount={} stations tookMs={}", rows, filtered.size(), stopWatch.getTime());
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
