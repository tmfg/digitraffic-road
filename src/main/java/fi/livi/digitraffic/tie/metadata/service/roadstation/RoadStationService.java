package fi.livi.digitraffic.tie.metadata.service.roadstation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.dao.RoadAddressRepository;
import fi.livi.digitraffic.tie.metadata.dao.RoadStationRepository;
import fi.livi.digitraffic.tie.metadata.model.RoadAddress;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;

@Service
public class RoadStationService {

    private static final Logger log = LoggerFactory.getLogger(RoadStationService.class);

    private final RoadStationRepository roadStationRepository;

    private final RoadAddressRepository roadAddressRepository;

    @Autowired
    public RoadStationService(final RoadStationRepository roadStationRepository,
                              final RoadAddressRepository roadAddressRepository) {
        this.roadStationRepository = roadStationRepository;
        this.roadAddressRepository = roadAddressRepository;
    }

    @Transactional
    public RoadStation save(final RoadStation roadStation) {
        try {
            final RoadStation value = roadStationRepository.save(roadStation);
            roadStationRepository.flush();
            return value;
        } catch (Exception e) {
            log.error("Could not save " + roadStation);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<RoadStation> findByType(final RoadStationType type) {
        return roadStationRepository.findByType(type);
    }

    @Transactional(readOnly = true)
    public Map<Long, RoadStation> findByTypeMappedByNaturalId(final RoadStationType type) {
        final List<RoadStation> all = findByType(type);

        final Map<Long, RoadStation> map = new HashMap<>();
        for (final RoadStation roadStation : all) {
            map.put(roadStation.getNaturalId(), roadStation);
        }
        return map;
    }

    @Transactional(readOnly = true)
    public Map<Long, RoadStation> findOrphansByTypeMappedByNaturalId(final RoadStationType type) {
        final List<RoadStation> orphans;
        if (RoadStationType.TMS_STATION == type) {
            orphans = roadStationRepository.findOrphanTmsRoadStations();
        } else if (RoadStationType.CAMERA_STATION == type) {
            orphans = roadStationRepository.findOrphanCameraRoadStations();
        } else if (RoadStationType.WEATHER_STATION == type) {
            orphans = roadStationRepository.findOrphanWeatherRoadStations();
        } else {
            throw new IllegalArgumentException("RoadStationType " + type + " is unknown");
        }

        final Map<Long, RoadStation> map = new HashMap<>();
        for (final RoadStation roadStation : orphans) {
            map.put(roadStation.getNaturalId(), roadStation);
        }
        return map;
    }

    @Transactional(readOnly = true)
    public List<RoadStation> findOrphanWeatherStationRoadStations() {
        return roadStationRepository.findOrphanWeatherRoadStations();
    }

    @Transactional
    public RoadAddress save(final RoadAddress roadAddress) {
        try {
            final RoadAddress value = roadAddressRepository.save(roadAddress);
            roadAddressRepository.flush();
            return value;
        } catch (Exception e) {
            log.error("Could not save " + roadAddress);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<RoadStation> findAll() {
        return roadStationRepository.findAll();
    }
}
