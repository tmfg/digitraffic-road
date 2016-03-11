package fi.livi.digitraffic.tie.service.roadstationsensor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.RoadStationSensorRepository;
import fi.livi.digitraffic.tie.model.RoadStationSensor;

@Service
public class RoadStationSensorServiceImpl implements RoadStationSensorService {
    private final RoadStationSensorRepository roadStationSensorRepository;

    @Autowired
    public RoadStationSensorServiceImpl(final RoadStationSensorRepository roadStationSensorRepository) {
        this.roadStationSensorRepository = roadStationSensorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoadStationSensor> findAllNonObsoleteRoadStationSensors() {
        return roadStationSensorRepository.findByObsoleteDateIsNull();
    }
}
