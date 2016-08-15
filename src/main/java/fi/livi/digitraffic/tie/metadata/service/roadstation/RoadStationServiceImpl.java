package fi.livi.digitraffic.tie.metadata.service.roadstation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.dao.RoadAddressRepository;
import fi.livi.digitraffic.tie.metadata.dao.RoadStationRepository;
import fi.livi.digitraffic.tie.metadata.model.RoadAddress;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;

@Service
public class RoadStationServiceImpl implements RoadStationService {

    private final RoadStationRepository roadStationRepository;

    private final RoadAddressRepository roadAddressRepository;

    @Autowired
    public RoadStationServiceImpl(final RoadStationRepository roadStationRepository,
                                  final RoadAddressRepository roadAddressRepository) {
        this.roadStationRepository = roadStationRepository;
        this.roadAddressRepository = roadAddressRepository;
    }

    @Transactional
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
    public Map<Long, RoadStation> findByTypeMappedByNaturalId(final RoadStationType type) {
        final List<RoadStation> all = findByType(type);

        final Map<Long, RoadStation> map = new HashMap<>();
        for (final RoadStation roadStation : all) {
            map.put(roadStation.getNaturalId(), roadStation);
        }
        return map;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, RoadStation> findOrphansByTypeMappedByNaturalId(final RoadStationType type) {
        final List<RoadStation> orphans;
        if (RoadStationType.LAM_STATION == type) {
            orphans = roadStationRepository.findOrphanLamStationRoadStations();
        } else if (RoadStationType.CAMERA == type) {
            orphans = roadStationRepository.findOrphanCameraStationRoadStations();
        } else if (RoadStationType.WEATHER_STATION == type) {
            orphans = roadStationRepository.findOrphanWeatherStationRoadStations();
        } else {
            throw new IllegalArgumentException("RoadStationType " + type + " is unknown");
        }

        final Map<Long, RoadStation> map = new HashMap<>();
        for (final RoadStation roadStation : orphans) {
            map.put(roadStation.getNaturalId(), roadStation);
        }
        return map;
    }

    @Override
    @Transactional(readOnly = true)
    public RoadStation findByTypeAndNaturalId(final RoadStationType type, final long naturalId) {
        return roadStationRepository.findByTypeAndNaturalId(type, naturalId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoadStation> findOrphanWeatherStationRoadStations() {
        return roadStationRepository.findOrphanWeatherStationRoadStations();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoadStation> findOrphanCameraStationRoadStations() {
        return roadStationRepository.findOrphanCameraStationRoadStations();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoadStation> findOrphanLamStationRoadStations() {
        return roadStationRepository.findOrphanLamStationRoadStations();
    }

    @Override
    @Transactional
    public RoadAddress save(final RoadAddress roadAddress) {
        final RoadAddress value = roadAddressRepository.save(roadAddress);
        roadAddressRepository.flush();
        return value;
    }

}
