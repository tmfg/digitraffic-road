package fi.livi.digitraffic.tie.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.roadstation.RoadStationDao;
import fi.livi.digitraffic.tie.dao.roadstation.RoadStationRepository;
import fi.livi.digitraffic.tie.model.roadstation.RoadStation;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;

@Service
public class RoadStationService {

    private final RoadStationRepository roadStationRepository;
    private final RoadStationDao roadStationDao;

    @Autowired
    public RoadStationService(final RoadStationRepository roadStationRepository,
                              final RoadStationDao roadStationDao) {
        this.roadStationRepository = roadStationRepository;
        this.roadStationDao = roadStationDao;
    }

    @Transactional(readOnly = true)
    public List<RoadStation> findByType(final RoadStationType type) {
        return roadStationRepository.findByType(type);
    }

    @Transactional(readOnly = true)
    public Optional<RoadStation> findByTypeAndLotjuId(final RoadStationType type, final Long lotjuId) {
        return roadStationRepository.findByTypeAndLotjuId(type, lotjuId);
    }

    @Transactional(readOnly = true)
    public List<RoadStation> findAll() {
        return roadStationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> getNaturalIdMappings(final RoadStationType type) {
        return roadStationDao.findPublishableNaturalIdsMappedByRoadStationsId(type);
    }
}
