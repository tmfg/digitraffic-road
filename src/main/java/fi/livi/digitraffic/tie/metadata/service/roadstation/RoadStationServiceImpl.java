package fi.livi.digitraffic.tie.metadata.service.roadstation;

import java.util.List;

import fi.livi.digitraffic.tie.metadata.dao.RoadStationRepository;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoadStationServiceImpl implements RoadStationService {

    private static final Logger LOG = Logger.getLogger(RoadStationServiceImpl.class);

    private final RoadStationRepository roadStationRepository;

    @Autowired
    public RoadStationServiceImpl(final RoadStationRepository roadStationRepository) {
        this.roadStationRepository = roadStationRepository;
    }

    @Override
    public RoadStation save(final RoadStation roadStation) {
        final RoadStation value = roadStationRepository.save(roadStation);
        roadStationRepository.flush();
        return value;

    }

    @Override
    @Transactional(readOnly = true)
    public List<RoadStation> findByType(final RoadStationType type) {
        return roadStationRepository.findByType(type);
    }

    @Transactional(readOnly = true)
    @Override
    public List<RoadStation> findOrphansByType(final RoadStationType type) {
        return roadStationRepository.findOrphansByType(type.getTypeNumber());
    }
}