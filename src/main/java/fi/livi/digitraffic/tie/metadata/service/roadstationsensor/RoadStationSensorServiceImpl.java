package fi.livi.digitraffic.tie.metadata.service.roadstationsensor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.dao.RoadStationSensorRepository;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;

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
        return roadStationSensorRepository.findNonObsoleteRoadStationSensors();
    }
}
