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
import fi.livi.digitraffic.tie.metadata.service.camera.AbstractCameraStationAttributeUpdater;
import fi.livi.digitraffic.tie.metadata.service.tms.AbstractTmsStationAttributeUpdater;
import fi.livi.digitraffic.tie.metadata.service.weather.AbstractWeatherStationAttributeUpdater;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.KameraVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.LamAsemaVO;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.TiesaaAsemaVO;

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
    public RoadStation findByTypeAndNaturalId(final RoadStationType type, Long naturalId) {
        return roadStationRepository.findByTypeAndNaturalId(type, naturalId);
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
    public RoadStation save(final RoadStation roadStation) {
        return roadStationRepository.save(roadStation);
    }

    @Transactional
    public RoadAddress save(final RoadAddress roadAddress) {
        return roadAddressRepository.save(roadAddress);
    }

    @Transactional(readOnly = true)
    public List<RoadStation> findAll() {
        return roadStationRepository.findAll();
    }

    @Transactional
    public boolean updateRoadStation(LamAsemaVO from) {
        RoadStation rs = roadStationRepository.findByTypeAndLotjuId(RoadStationType.TMS_STATION, from.getId());
        return rs != null && AbstractTmsStationAttributeUpdater.updateRoadStationAttributes(from, rs);
    }

    @Transactional
    public boolean updateRoadStation(TiesaaAsemaVO from) {
        log.info("A: {}: {}", from.getId(),from.getKeruunTila());
        RoadStation rs = roadStationRepository.findByTypeAndLotjuId(RoadStationType.WEATHER_STATION, from.getId());
        boolean value = rs != null && AbstractWeatherStationAttributeUpdater.updateRoadStationAttributes(from, rs);
        log.info("B: {}: {}", from.getId(),from.getKeruunTila());
        return value;
    }

    @Transactional
    public boolean updateRoadStation(KameraVO from) {
        RoadStation rs = roadStationRepository.findByTypeAndLotjuId(RoadStationType.CAMERA_STATION, from.getId());
        return rs != null && AbstractCameraStationAttributeUpdater.updateRoadStationAttributes(from, rs);
    }

}
