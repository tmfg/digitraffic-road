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

    @Override
    public Map<Long, RoadStation> findOrphansByTypeMappedByNaturalId(RoadStationType type) {
        List<RoadStation> orphans;
        if (RoadStationType.LAM_STATION == type) {
            orphans = roadStationRepository.findOrphanLamStationRoadStations();
        } else if (RoadStationType.CAMERA == type) {
            orphans = roadStationRepository.findOrphanCameraStationRoadStations();
        } else if (RoadStationType.WEATHER_STATION == type) {
            orphans = roadStationRepository.findOrphanWeatherStationRoadStations();
        } else {
            throw new IllegalArgumentException("RoadStationType " + type + " is unknown");
        }

        Map<Long, RoadStation> map = new HashMap<>();
        for (RoadStation roadStation : orphans) {
            map.put(roadStation.getNaturalId(), roadStation);
        }
        return map;
    }

    @Override
    public Map<Long, RoadStation> findByTypeMappedByNaturalId(RoadStationType type) {
        List<RoadStation> all = findByType(type);

        Map<Long, RoadStation> map = new HashMap<>();
        for (RoadStation roadStation : all) {
            map.put(roadStation.getNaturalId(), roadStation);
        }
        return map;
    }

    @Override
    public RoadStation findByTypeAndNaturalId(RoadStationType type, long naturalId) {
        return roadStationRepository.findByTypeAndNaturalId(type, naturalId);
    }


    @Transactional(readOnly = true)
    @Override
    public List<RoadStation> findOrphanWeatherStationRoadStations() {
        return roadStationRepository.findOrphanWeatherStationRoadStations();
    }

    @Transactional(readOnly = true)
    @Override
    public List<RoadStation> findOrphanCameraStationRoadStations() {
        return roadStationRepository.findOrphanCameraStationRoadStations();
    }

    @Transactional(readOnly = true)
    @Override
    public List<RoadStation> findOrphanLamStationRoadStations() {
        return roadStationRepository.findOrphanLamStationRoadStations();
    }

    @Transactional
    @Override
    public RoadAddress save(final RoadAddress roadAddress) {
        final RoadAddress value = roadAddressRepository.save(roadAddress);
        roadAddressRepository.flush();
        return value;
    }

}
