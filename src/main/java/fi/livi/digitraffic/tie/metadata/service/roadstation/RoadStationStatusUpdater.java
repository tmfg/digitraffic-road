package fi.livi.digitraffic.tie.metadata.service.roadstation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.camera.AbstractCameraStationAttributeUpdater;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuCameraStationMetadataService;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuTmsStationMetadataService;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuWeatherStationMetadataService;
import fi.livi.digitraffic.tie.metadata.service.tms.AbstractTmsStationAttributeUpdater;
import fi.livi.digitraffic.tie.metadata.service.weather.AbstractWeatherStationAttributeUpdater;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.KameraVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.LamAsemaVO;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.TiesaaAsemaVO;

@Service
public class RoadStationStatusUpdater {
    private static final Logger log = LoggerFactory.getLogger(RoadStationStatusUpdater.class);

    private final LotjuCameraStationMetadataService lotjuCameraStationMetadataService;
    private final LotjuTmsStationMetadataService lotjuTmsStationMetadataService;
    private final LotjuWeatherStationMetadataService lotjuWeatherStationMetadataService;
    private RoadStationService roadStationService;

    public RoadStationStatusUpdater(final LotjuCameraStationMetadataService lotjuCameraStationMetadataService,
                                    final LotjuTmsStationMetadataService lotjuTmsStationMetadataService,
                                    final LotjuWeatherStationMetadataService lotjuWeatherStationMetadataService,
                                    final RoadStationService roadStationService) {
        this.lotjuCameraStationMetadataService = lotjuCameraStationMetadataService;
        this.lotjuTmsStationMetadataService = lotjuTmsStationMetadataService;
        this.lotjuWeatherStationMetadataService = lotjuWeatherStationMetadataService;
        this.roadStationService = roadStationService;
    }

    @Transactional
    public boolean updateTmsStationsStatuses() {
        if (!lotjuTmsStationMetadataService.isEnabled()) {
            log.info("Not updating TMS stations statuses because LotjuTmsStationMetadataService not enabled");
            return false;
        }
        log.info("Update TMS stations statuses");
        final List<LamAsemaVO> allLams = lotjuTmsStationMetadataService.getLamAsemas();
        final Map<Long, RoadStation> lotjuIdRoadStationMap = getLotjuIdRoadStationMap(RoadStationType.TMS_STATION);
        final AtomicBoolean updated = new AtomicBoolean(false);

        allLams.parallelStream().forEach(from -> {
            RoadStation to = lotjuIdRoadStationMap.get(from.getId());
            if (to != null) {
                updated.compareAndSet(false,
                                      AbstractTmsStationAttributeUpdater.updateRoadStationAttributes(from, to));
            }
        });
        return updated.get();
    }

    @Transactional
    public boolean updateWeatherStationsStatuses() {
        if (!lotjuWeatherStationMetadataService.isEnabled()) {
            log.info("Not updating weather stations statuses because LotjuWeatherStationMetadataClient not enabled");
            return false;
        }
        log.info("Update weather stations statuses");
        final List<TiesaaAsemaVO> allTiesaaAsemas = lotjuWeatherStationMetadataService.getTiesaaAsemmas();
        final Map<Long, RoadStation> lotjuIdRoadStationMap = getLotjuIdRoadStationMap(RoadStationType.WEATHER_STATION);
        final AtomicBoolean updated = new AtomicBoolean(false);

        allTiesaaAsemas.parallelStream().forEach(from -> {
            RoadStation to = lotjuIdRoadStationMap.get(from.getId());
            if (to != null) {
                updated.compareAndSet(false,
                                      AbstractWeatherStationAttributeUpdater.updateRoadStationAttributes(from, to));
            }
        });
        return updated.get();
    }

    @Transactional
    public boolean updateCameraStationsStatuses() {
        if (!lotjuCameraStationMetadataService.isEnabled()) {
            log.info("Not updating camera stations statuses because LotjuCameraStationService not enabled");
            return false;
        }
        log.info("Update camera stations statuses");
        final List<KameraVO> allKameras = lotjuCameraStationMetadataService.getKameras();
        final Map<Long, RoadStation> lotjuIdRoadStationMap = getLotjuIdRoadStationMap(RoadStationType.CAMERA_STATION);
        final AtomicBoolean updated = new AtomicBoolean(false);

        allKameras.parallelStream().forEach(from -> {
            RoadStation to = lotjuIdRoadStationMap.get(from.getId());
            if (to != null) {
                updated.compareAndSet(false,
                                      AbstractCameraStationAttributeUpdater.updateRoadStationAttributes(from, to));
            }
        });
        return updated.get();
    }

    private Map<Long, RoadStation> getLotjuIdRoadStationMap(final RoadStationType roadStationType) {
        List<RoadStation> tmsStations = roadStationService.findByType(roadStationType);
        return tmsStations.parallelStream().filter(rs -> rs.getLotjuId() != null).collect(Collectors.toMap(RoadStation::getLotjuId, Function.identity()));
    }
}
