package fi.livi.digitraffic.tie.data.service;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.SensorValueDao;
import fi.livi.digitraffic.tie.lotju.xsd.lam.Lam;
import fi.livi.digitraffic.tie.lotju.xsd.tiesaa.Tiesaa;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;

@Service
public class SensorDataUpdateService {
    private static final Logger log = LoggerFactory.getLogger(SensorDataUpdateService.class);


    private final Set<Long> allowedTmsSensorLotjuIds;
    private final Set<Long> allowedWeatherSensorLotjuIds;


    private final SensorValueDao sensorValueDao;

    @Autowired
    public SensorDataUpdateService(final SensorValueDao sensorValueDao,
                                   final RoadStationSensorService roadStationSensorService) throws SQLException {
        this.sensorValueDao = sensorValueDao;

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
     * @return true if success
     */
    @Transactional
    public int updateLamData(final List<Lam> data) {

        final StopWatch stopWatch = StopWatch.createStarted();
        final Collection<Lam> filtered = filterNewestLamValues(data);
        final int rows = sensorValueDao.updateLamSensorData(filtered, allowedTmsSensorLotjuIds);
        stopWatch.stop();
        log.info("Update tms sensors data for {} sensors of {} stations took {} ms",
            rows,
            filtered.size(),
            stopWatch.getTime());
        return rows;
    }

    /**
     * Updates weather data to db
     * @param data
     * @return true if success
     */
    @Transactional
    public int updateWeatherData(final Collection<Tiesaa> data) {

        final StopWatch stopWatch = StopWatch.createStarted();
        final Collection<Tiesaa> filtered = filterNewestTiesaaValues(data);
        final int rows = sensorValueDao.updateWeatherSensorData(filtered, allowedWeatherSensorLotjuIds);
        stopWatch.stop();

        log.info("Update weather sensors data for {} sensors of {} stations took {} ms",
                 rows,
                 filtered.size(),
                 stopWatch.getTime());
        return rows;
    }

    private static Collection<Lam> filterNewestLamValues(final List<Lam> data) {
        // Collect newest data per station
        HashMap<Long, Lam> tmsMapByLamStationLotjuId = new HashMap<>();
        for (Lam lam : data) {
            Lam currentLam = tmsMapByLamStationLotjuId.get(lam.getAsemaId());
            if (currentLam == null || lam.getAika().toGregorianCalendar().before(currentLam.getAika().toGregorianCalendar())) {
                if (currentLam != null) {
                    log.info("Replace " + currentLam.getAika() + " with " + lam.getAika());
                }
                tmsMapByLamStationLotjuId.put(lam.getAsemaId(), lam);
            }
        }
        return tmsMapByLamStationLotjuId.values();
    }

    private static Collection<Tiesaa> filterNewestTiesaaValues(final Collection<Tiesaa> data) {
        // Collect newest data per station
        HashMap<Long, Tiesaa> tiesaaMapByTmsStationLotjuId = new HashMap<>();
        for (Tiesaa tiesaa : data) {
            Tiesaa currentTiesaa = tiesaaMapByTmsStationLotjuId.get(tiesaa.getAsemaId());
            if (currentTiesaa == null || tiesaa.getAika().toGregorianCalendar().before(currentTiesaa.getAika().toGregorianCalendar())) {
                if (currentTiesaa != null) {
                    log.info("Replace " + currentTiesaa.getAika() + " with " + tiesaa.getAika());
                }
                tiesaaMapByTmsStationLotjuId.put(tiesaa.getAsemaId(), tiesaa);
            }
        }
        return tiesaaMapByTmsStationLotjuId.values();
    }
}
