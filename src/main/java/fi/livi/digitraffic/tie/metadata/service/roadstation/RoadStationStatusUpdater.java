package fi.livi.digitraffic.tie.metadata.service.roadstation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuCameraClient;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuTmsStationClient;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuWeatherStationClient;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.KameraVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29.LamAsemaVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29.TiesaaAsemaVO;

@Service
public class RoadStationStatusUpdater {
    private static final Logger log = LoggerFactory.getLogger(RoadStationStatusUpdater.class);

    private final LotjuCameraClient lotjuCameraClient;
    private final LotjuTmsStationClient lotjuTmsStationClient;
    private final LotjuWeatherStationClient lotjuWeatherStationClient;
    private RoadStationService roadStationService;

    public RoadStationStatusUpdater(final LotjuCameraClient lotjuCameraClient,
                                    final LotjuTmsStationClient lotjuTmsStationClient,
                                    final LotjuWeatherStationClient lotjuWeatherStationClient,
                                    final RoadStationService roadStationService) {
        this.lotjuCameraClient = lotjuCameraClient;
        this.lotjuTmsStationClient = lotjuTmsStationClient;
        this.lotjuWeatherStationClient = lotjuWeatherStationClient;
        this.roadStationService = roadStationService;
    }

    @Transactional
    public boolean updateTmsStationsStatuses() {
        if (lotjuTmsStationClient == null) {
            log.info("Not updating TMS stations statuses because LotjuTmsStationClient not defined");
            return false;
        }
        log.info("Update TMS stations statuses");
        final List<LamAsemaVO> allLams = lotjuTmsStationClient.getLamAsemas();
        final Map<Long, RoadStation> lotjuIdRoadStationMap = getLotjuIdRoadStationMap(RoadStationType.TMS_STATION);
        final AtomicBoolean updated = new AtomicBoolean(false);

        allLams.parallelStream().forEach(from -> {
            RoadStation to = lotjuIdRoadStationMap.get(from.getId());
            updateRoadStationStatuses(to, CollectionStatus.convertKeruunTila(from.getKeruunTila()), from.isJulkinen(), updated);
        });
        return updated.get();
    }

    @Transactional
    public boolean updateWeatherStationsStatuses() {
        if (lotjuWeatherStationClient == null) {
            log.info("Not updating weather stations statuses because LotjuWeatherStationClient not defined");
            return false;
        }
        log.info("Update weather stations statuses");
        final List<TiesaaAsemaVO> allTiesaaAsemas = lotjuWeatherStationClient.getTiesaaAsemmas();
        final Map<Long, RoadStation> lotjuIdRoadStationMap = getLotjuIdRoadStationMap(RoadStationType.WEATHER_STATION);
        final AtomicBoolean updated = new AtomicBoolean(false);

        allTiesaaAsemas.parallelStream().forEach(from -> {
            RoadStation to = lotjuIdRoadStationMap.get(from.getId());
            updateRoadStationStatuses(to, CollectionStatus.convertKeruunTila(from.getKeruunTila()), from.isJulkinen(), updated);
        });
        return updated.get();
    }

    @Transactional
    public boolean updateCameraStationsStatuses() {
        if (lotjuWeatherStationClient == null) {
            log.info("Not updating camera stations statuses because LotjuCameraClient not defined");
            return false;
        }
        log.info("Update camera stations statuses");
        final List<KameraVO> allKameras = lotjuCameraClient.getKameras();
        final Map<Long, RoadStation> lotjuIdRoadStationMap = getLotjuIdRoadStationMap(RoadStationType.CAMERA_STATION);
        final AtomicBoolean updated = new AtomicBoolean(false);

        allKameras.parallelStream().forEach(from -> {
            RoadStation to = lotjuIdRoadStationMap.get(from.getId());
            updateRoadStationStatuses(to, CollectionStatus.convertKeruunTila(from.getKeruunTila()), from.isJulkinen(), updated);
        });
        return updated.get();
    }

    private void updateRoadStationStatuses(RoadStation to, CollectionStatus collectionStatus, Boolean julkinen, AtomicBoolean updated) {
        if (to != null) {
            final int hash = HashCodeBuilder.reflectionHashCode(to);
            to.setCollectionStatus(collectionStatus);
            to.setPublic(julkinen == null || julkinen);
            updated.compareAndSet(false,
                                 HashCodeBuilder.reflectionHashCode(to) != hash);
        }
    }

    private Map<Long, RoadStation> getLotjuIdRoadStationMap(final RoadStationType roadStationType) {
        List<RoadStation> tmsStations = roadStationService.findByType(roadStationType);
        return tmsStations.parallelStream().filter(rs -> rs.getLotjuId() != null).collect(Collectors.toMap(RoadStation::getLotjuId, Function.identity()));
    }
}
