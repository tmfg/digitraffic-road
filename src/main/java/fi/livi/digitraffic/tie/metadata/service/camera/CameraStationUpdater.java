package fi.livi.digitraffic.tie.metadata.service.camera;

import static fi.livi.digitraffic.tie.metadata.model.CollectionStatus.isPermanentlyDeletedKeruunTila;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.MetadataUpdatedMessageDto.UpdateType;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuCameraStationMetadataService;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.EsiasentoVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.KameraVO;

@ConditionalOnNotWebApplication
@Service
public class CameraStationUpdater {
    private static final Logger log = LoggerFactory.getLogger(CameraStationUpdater.class);

    private final LotjuCameraStationMetadataService lotjuCameraStationMetadataService;
    private final CameraStationUpdateService cameraStationUpdateService;
    private final CameraPresetService cameraPresetService;
    private final RoadStationService roadStationService;

    private final ReentrantLock lock = new ReentrantLock();

    @Autowired
    public CameraStationUpdater(final LotjuCameraStationMetadataService lotjuCameraStationMetadataService,
                                final CameraStationUpdateService cameraStationUpdateService,
                                final CameraPresetService cameraPresetService,
                                final RoadStationService roadStationService) {
        this.lotjuCameraStationMetadataService = lotjuCameraStationMetadataService;
        this.cameraStationUpdateService = cameraStationUpdateService;
        this.cameraPresetService = cameraPresetService;
        this.roadStationService = roadStationService;
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 450000)
    public boolean updateCameras() {
        log.info("method=updateCameras start");

        Set<Long> camerasLotjuIds = lotjuCameraStationMetadataService.getKamerasLotjuids();
        final Pair<Integer, Integer> updatedInsertedCount =
            camerasLotjuIds.stream().map(lotjuId -> updateCameraStationAndPresets(lotjuId))
                .collect(Collectors.reducing((p1, p2) -> Pair.of(p1.getLeft() + p2.getLeft(), p1.getRight() + p2.getRight())))
                .orElse(Pair.of(0, 0));

        long obsoletePresets = cameraPresetService.obsoleteCameraPresetsExcludingCameraLotjuIds(camerasLotjuIds);
        long obsoletedRoadStations = cameraPresetService.obsoleteCameraRoadStationsWithoutPublishablePresets();

        log.info("obsoletedCameraPresetsCount={} CameraPresets that are not active", obsoletePresets);
        log.info("obsoletedRoadStationsCount={} Camera RoadStations without active presets", obsoletedRoadStations);
        log.info("updatedCameraPresetsCount={} CameraPresets", updatedInsertedCount.getLeft());
        log.info("insertedCameraPresetsCount={} CameraPresets", updatedInsertedCount.getRight());
        final boolean updatedCameras = updatedInsertedCount.getLeft() > 0 || updatedInsertedCount.getRight() > 0;
        log.info("method=updateCameras end updated={}", updatedCameras);

        return updatedCameras;
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    public int updateCameraStationsStatuses() {

        int updated = 0;
        log.info("method=updateCameraStationsStatuses start");
        final Set<Long> kamerasLotjuids = lotjuCameraStationMetadataService.getKamerasLotjuids();

        for (Long kameraLotjuId : kamerasLotjuids) {
            if (updateCameraStation(kameraLotjuId) ) {
                updated++;
            }
        }

        return updated;
    }

    /**
     * @param kameraLotjuId to update
     * @return Pair of updated and inserted count of presets
     */
    private Pair<Integer, Integer> updateCameraStationAndPresets(final long kameraLotjuId) {
        lock.lock();
        try {

            log.info("method=updateCameraStationAndPresets got the lock");
            final KameraVO kamera = lotjuCameraStationMetadataService.getKamera(kameraLotjuId);
            if (!validate(kamera)) {
                return Pair.of(0,0);
            }
            final List<EsiasentoVO> eas = lotjuCameraStationMetadataService.getEsiasentos(kameraLotjuId);
            return cameraStationUpdateService.updateOrInsertRoadStationAndPresets(kamera, eas);

        } finally {
            lock.unlock();
        }
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 5000)
    public boolean updateCameraStation(final long cameraLotjuId) {

        lock.lock();
        try {

            log.info("method=updateCameraStation start");
            // If camera station doesn't exist, we have to create it and the presets.
            if (roadStationService.findByTypeAndLotjuId(RoadStationType.CAMERA_STATION, cameraLotjuId) == null) {
                final Pair<Integer, Integer> updated = updateCameraStationAndPresets(cameraLotjuId);
                return updated.getLeft() > 0 || updated.getRight() > 0;
            }

            // Otherwise we update only the station
            final KameraVO kamera = lotjuCameraStationMetadataService.getKamera(cameraLotjuId);
            if (!validate(kamera)) {
                return false;
            }
            return cameraStationUpdateService.updateCamera(kamera);

        } finally {
            lock.unlock();
        }
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 5000)
    public boolean updateCameraPreset(final long presetLotjuId) {

        lock.lock();
        try {

            log.info("method=updateCameraPreset got the lock");
            final EsiasentoVO esiasento = lotjuCameraStationMetadataService.getEsiasento(presetLotjuId);

            // If camera preset doesn't exist, we have to create it -> just update the whole station
            if (cameraPresetService.findCameraPresetByLotjuId(presetLotjuId) == null) {
                final Pair<Integer, Integer> updated = updateCameraStationAndPresets(esiasento.getKameraId());
                return updated.getLeft() > 0 || updated.getRight() > 0;
            }

            // Otherwise update only the given preset
            final KameraVO kamera = lotjuCameraStationMetadataService.getKamera(esiasento.getKameraId());
            if (validate(kamera)) {
                return cameraStationUpdateService.updatePreset(esiasento, kamera);
            } else {
                return false;
            }

        } finally {
            lock.unlock();
        }
    }

    private boolean validate(final KameraVO kamera) {
        final boolean valid = kamera.getVanhaId() != null;
        if (!valid && !isPermanentlyDeletedKeruunTila(kamera.getKeruunTila())) {
            log.error("{} is invalid: has null vanhaId", ToStringHelper.toString(kamera));
        }
        return valid;
    }
}
