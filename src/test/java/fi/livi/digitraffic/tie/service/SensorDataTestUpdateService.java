package fi.livi.digitraffic.tie.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.dao.SensorValueHistoryDao;
import fi.livi.digitraffic.tie.dao.v1.RoadStationDao;
import fi.livi.digitraffic.tie.dao.v1.SensorValueDao;

/**
 * This class is only for tests. Normal scheduled sensor value db task is override here
 */
@Service
public class SensorDataTestUpdateService extends fi.livi.digitraffic.tie.service.v1.SensorDataUpdateService {
    @Autowired
    public SensorDataTestUpdateService(SensorValueDao sensorValueDao,
                                       RoadStationSensorService roadStationSensorService,
                                       RoadStationDao roadStationDao,
                                       DataStatusService dataStatusService,
                                       SensorValueHistoryDao sensorValueHistoryDao) {
        super(sensorValueDao, roadStationSensorService, roadStationDao, dataStatusService, sensorValueHistoryDao);
    }

    public void flushTmsBuffer() {
        // Override scheduled task
        persistLamSensorValues();
    }

    public void flushWeatherBuffer() {
        // Override scheduled task
        persistWeatherSensorValues();
    }
}
