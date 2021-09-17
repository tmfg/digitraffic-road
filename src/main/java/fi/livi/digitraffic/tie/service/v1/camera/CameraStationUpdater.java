package fi.livi.digitraffic.tie.service.v1.camera;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.AbstractVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.EsiasentoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraVO;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.CollectionStatus;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.service.ClusteredLocker;
import fi.livi.digitraffic.tie.service.RoadStationService;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.MetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.v1.MetadataUpdateClusteredLock;
import fi.livi.digitraffic.tie.service.v1.lotju.LotjuCameraStationMetadataClientWrapper;

@ConditionalOnNotWebApplication
@Component
public class CameraStationUpdater {
    private static final Logger log = LoggerFactory.getLogger(CameraStationUpdater.class);

    private final LotjuCameraStationMetadataClientWrapper lotjuCameraStationMetadataClientWrapper;
    private final CameraStationUpdateService cameraStationUpdateService;
    private final CameraPresetService cameraPresetService;
    private final RoadStationService roadStationService;
    private final MetadataUpdateClusteredLock lock;

    @Autowired
    public CameraStationUpdater(final LotjuCameraStationMetadataClientWrapper lotjuCameraStationMetadataClientWrapper,
                                final CameraStationUpdateService cameraStationUpdateService,
                                final CameraPresetService cameraPresetService,
                                final RoadStationService roadStationService,
                                final ClusteredLocker clusteredLocker) {
        this.lotjuCameraStationMetadataClientWrapper = lotjuCameraStationMetadataClientWrapper;
        this.cameraStationUpdateService = cameraStationUpdateService;
        this.cameraPresetService = cameraPresetService;
        this.roadStationService = roadStationService;
        this.lock = new MetadataUpdateClusteredLock(clusteredLocker, this.getClass().getSimpleName());
    }


    @PerformanceMonitor(maxWarnExcecutionTime = 450000)
    public boolean updateCameras() {
        log.info("method=updateCameras start");
        final List<KameraVO> kameras = lotjuCameraStationMetadataClientWrapper.getKameras();

        AtomicInteger updated = new AtomicInteger();
        AtomicInteger inserted = new AtomicInteger();

        final List<Exception> errors = new ArrayList<>();

        kameras.forEach(kamera -> {
                try {
                    final Pair<Integer, Integer> result = updateCameraStationAndPresets(kamera);
                    updated.getAndAdd(result.getLeft());
                    inserted.getAndAdd(result.getRight());
                } catch (final Exception e) {
                    errors.add(e);
                    log.error(String.format("method=updateCameras had an error in method updateCameraStationAndPresets with camera lotjuId=%d", kamera.getId()), e);
                }
            });

        final Set<Long> camerasLotjuIds = kameras.stream().map(AbstractVO::getId).collect(Collectors.toSet());
        long obsoletePresets = cameraPresetService.obsoleteCameraPresetsExcludingCameraLotjuIds(camerasLotjuIds);
        long obsoletedRoadStations = cameraPresetService.obsoleteCameraRoadStationsWithoutPublishablePresets();

        log.info("obsoletedCameraPresetsCount={} CameraPresets that are not active", obsoletePresets);
        log.info("obsoletedRoadStationsCount={} Camera RoadStations without active presets", obsoletedRoadStations);
        log.info("updatedCameraPresetsCount={} CameraPresets", updated.get());
        log.info("insertedCameraPresetsCount={} CameraPresets", inserted.get());
        final boolean updatedCameras = updated.get() > 0 || inserted.get() > 0;
        log.info("method=updateCameras end updatedBoolean={}", updatedCameras);

        if (!errors.isEmpty()) {
            throw new RuntimeException(
                String.format("There was %d errors in method=updateCameras and here is thrown the first of them", errors.size()), errors.get(0));
        }

        return updatedCameras;
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    public int updateCameraStationsStatuses() {
        log.info("method=updateCameraStationsStatuses start");
        final List<KameraVO> kameras = lotjuCameraStationMetadataClientWrapper.getKameras();

        final List<Exception> errors = new ArrayList<>();
        final int updateCount = kameras.stream().mapToInt(kamera -> {
            try {
                log.info("method=updateCameraStationsStatuses update camera with lotjuId={}", kamera.getId());
                return updateCameraStation(kamera) ? 1 : 0;
            } catch (final Exception e) {
                errors.add(e);
                log.error(String.format("method=updateCameraStationsStatuses had an error in method updateCameraStation with camera lotjuId=%d", kamera.getId()), e);
                return 0;
            }
        }).sum();

        if (!errors.isEmpty()) {
            throw new RuntimeException(
                String.format("There was %d errors in method=updateCameraStationsStatuses and here is thrown the first one of them",
                errors.size()), errors.get(0));
        }

        return updateCount;
    }


    private Pair<Integer, Integer> updateCameraStationAndPresets(final long cameraLotjuId) {
        final KameraVO kamera = lotjuCameraStationMetadataClientWrapper.getKamera(cameraLotjuId);
        if (kamera == null) {
            log.warn("method=updateCameraStationAndPresets No Camera found with lotjuId={}", cameraLotjuId);
            return Pair.of(0,0);
        }
        return updateCameraStationAndPresets(kamera);
    }

    /**
     * @param kamera Camera to update
     * @return Pair of updated and inserted count of presets
     */
    private Pair<Integer, Integer> updateCameraStationAndPresets(final KameraVO kamera) {
        lock.lock();
        try {
            log.debug("method=updateCameraStationAndPresets got the lock");
            if (!validate(kamera)) {
                return Pair.of(0,0);
            }
            final List<EsiasentoVO> eas = lotjuCameraStationMetadataClientWrapper.getEsiasentos(kamera.getId());
            return cameraStationUpdateService.updateOrInsertRoadStationAndPresets(kamera, eas);

        } finally {
            lock.unlock();
        }
    }

    @PerformanceMonitor()
    public boolean updateCameraStation(final long cameraLotjuId,
                                       final MetadataUpdatedMessageDto.UpdateType updateType) {
        log.info("method=updateCameraStationFromJms start lotjuId={} type={}", cameraLotjuId, updateType);
        if (updateType.isDelete()) {
            return cameraStationUpdateService.obsoleteStationWithLotjuId(cameraLotjuId);
        } else {
            final KameraVO kamera = lotjuCameraStationMetadataClientWrapper.getKamera(cameraLotjuId);
            if (kamera == null) {
                log.warn("method=updateCameraStation No Camera with lotjuId={} found", cameraLotjuId);
                return false;
            }
            return updateCameraStation(kamera);
        }
    }

    private boolean updateCameraStation(final KameraVO kamera) {
        // If camera station doesn't exist, we have to create it and the presets.
        if (roadStationService.findByTypeAndLotjuId(RoadStationType.CAMERA_STATION, kamera.getId()) == null) {
            final Pair<Integer, Integer> updated = updateCameraStationAndPresets(kamera);
            return updated.getLeft() > 0 || updated.getRight() > 0;
        }

        // Otherwise we update only the station
        lock.lock();
        try {
            log.debug("method=updateCameraStation got the lock lotjuId={}", kamera.getId());

            if (!validate(kamera)) {
                return false;
            }
            return cameraStationUpdateService.updateCamera(kamera);

        } finally {
            lock.unlock();
        }
    }

    @PerformanceMonitor()
    public boolean updateCameraPreset(final long presetLotjuId,
                                      final MetadataUpdatedMessageDto.UpdateType updateType) {
        log.info("method=updateCameraPreset start lotjuId={} type={}", presetLotjuId, updateType);

        if (updateType.isDelete()) {
            return cameraPresetService.obsoleteCameraPresetWithLotjuId(presetLotjuId);
        }

        final EsiasentoVO esiasento = lotjuCameraStationMetadataClientWrapper.getEsiasento(presetLotjuId);

        if (esiasento == null) {
            log.warn("method=updateCameraPreset No CameraPreset with lotjuId={} found", presetLotjuId);
            return false;
        }

        // If camera preset doesn't exist, we have to create it -> just update the whole station
        if (cameraPresetService.findCameraPresetByLotjuId(presetLotjuId) == null) {
            final Pair<Integer, Integer> updated = updateCameraStationAndPresets(esiasento.getKameraId());
            return updated.getLeft() > 0 || updated.getRight() > 0;
        }

        // Otherwise update only the given preset
        log.debug("method=updateCameraPreset got the lock lotjuId={}", presetLotjuId);
        final KameraVO kamera = lotjuCameraStationMetadataClientWrapper.getKamera(esiasento.getKameraId());
        if (validate(kamera)) {
            lock.lock();
            try {
                return cameraStationUpdateService.updatePreset(esiasento, kamera);
            } finally {
                lock.unlock();
            }
        } else {
            return false;
        }
    }

    private boolean validate(final KameraVO kamera) {
        final boolean valid = kamera.getVanhaId() != null;
        if (!valid && !CollectionStatus.isPermanentlyDeletedKeruunTila(kamera.getKeruunTila())) {
            log.error("{} is invalid: has null vanhaId", ToStringHelper.toString(kamera));
        }
        return valid;
    }
}
