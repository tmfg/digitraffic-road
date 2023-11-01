package fi.livi.digitraffic.tie.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.roadstation.RoadStationDao;
import fi.livi.digitraffic.tie.dao.roadstation.SensorValueDao;
import fi.livi.digitraffic.tie.dao.roadstation.SensorValueHistoryDao;
import fi.livi.digitraffic.tie.service.roadstation.SensorDataUpdateService;

/**
 * This class is only for tests. Normal scheduled sensor value db task is override here
 */
@Service
public class SensorDataTestUpdateService extends SensorDataUpdateService {
    @Autowired
    public SensorDataTestUpdateService(SensorValueDao sensorValueDao,
                                       RoadStationSensorService roadStationSensorService,
                                       RoadStationDao roadStationDao,
                                       DataStatusService dataStatusService,
                                       SensorValueHistoryDao sensorValueHistoryDao) {
        super(sensorValueDao, roadStationSensorService, roadStationDao, dataStatusService, sensorValueHistoryDao);
    }

    @Transactional
    public void flushTmsBuffer() {
        // Override scheduled task
        persistLamSensorValues();
    }

    @Transactional
    public void flushWeatherBuffer() {
        // Override scheduled task
        persistWeatherSensorValues();
    }
}
