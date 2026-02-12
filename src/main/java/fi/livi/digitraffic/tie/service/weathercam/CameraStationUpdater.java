package fi.livi.digitraffic.tie.service.weathercam;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.common.annotation.PerformanceMonitor;
import fi.livi.digitraffic.common.service.locking.CachedLockingService;
import fi.livi.digitraffic.common.service.locking.LockingService;
import fi.livi.digitraffic.common.util.StringUtil;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.AbstractVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.EsiasentoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraVO;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.roadstation.CollectionStatus;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.service.RoadStationService;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.MetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.lotju.LotjuCameraStationMetadataClientWrapper;

@ConditionalOnNotWebApplication
@Component
public class CameraStationUpdater {
    private static final Logger log = LoggerFactory.getLogger(CameraStationUpdater.class);

    private final LotjuCameraStationMetadataClientWrapper lotjuCameraStationMetadataClientWrapper;
    private final CameraStationUpdateService cameraStationUpdateService;
    private final CameraPresetService cameraPresetService;
    private final RoadStationService roadStationService;
    private final CachedLockingService cachedLockingService;

    @Autowired
    public CameraStationUpdater(final LotjuCameraStationMetadataClientWrapper lotjuCameraStationMetadataClientWrapper,
                                final CameraStationUpdateService cameraStationUpdateService,
                                final CameraPresetService cameraPresetService,
                                final RoadStationService roadStationService,
                                final LockingService lockingService) {
        this.lotjuCameraStationMetadataClientWrapper = lotjuCameraStationMetadataClientWrapper;
        this.cameraStationUpdateService = cameraStationUpdateService;
        this.cameraPresetService = cameraPresetService;
        this.roadStationService = roadStationService;
        this.cachedLockingService = lockingService.createCachedLockingService(this.getClass().getSimpleName());
    }


    /* Takes normally 4 min 3/4 of the time takes to fetch data warn 5 min error 6 min*/
    @PerformanceMonitor(maxWarnExcecutionTime = 300000, maxErrorExcecutionTime = 360000)
    public boolean updateCameras() {
        log.info("method=updateCameras start");
        final StopWatch timeAll = StopWatch.createStarted();
        final StopWatch timeFetch = StopWatch.createStarted();
        final List<KameraVO> kameras = lotjuCameraStationMetadataClientWrapper.getKameras();
        timeFetch.stop();

        final AtomicInteger updated = new AtomicInteger();
        final AtomicInteger inserted = new AtomicInteger();

        final List<Exception> errors = new ArrayList<>();
        final AtomicLong incrementalFetchTookMs = new AtomicLong();
        final AtomicLong incrementalUpdateTookMs = new AtomicLong();
        kameras.forEach(kamera -> {
                try {
                    final Pair<Integer, Integer> result = updateCameraStationAndPresets(kamera, incrementalFetchTookMs, incrementalUpdateTookMs);
                    updated.getAndAdd(result.getLeft());
                    inserted.getAndAdd(result.getRight());
                } catch (final Exception e) {
                    errors.add(e);
                    log.error("method=updateCameras had an error in method updateCameraStationAndPresets with camera lotjuId={}", kamera.getId(), e);
                }
            });

        final StopWatch timeUpdate = StopWatch.createStarted();
        final Set<Long> camerasLotjuIds = kameras.stream().map(AbstractVO::getId).collect(Collectors.toSet());
        final long obsoletePresets = cameraPresetService.obsoleteCameraPresetsExcludingCameraLotjuIds(camerasLotjuIds);
        final long obsoletedRoadStations = cameraPresetService.obsoleteCameraRoadStationsWithoutPublishablePresets();

        log.info("method=updateCameras obsoletedCameraPresetsCount={} obsoletedRoadStationsCount={} updatedCameraPresetsCount={} insertedCameraPresetsCount={}", obsoletePresets, obsoletedRoadStations, updated.get(), inserted.get());
        final boolean updatedCameras = updated.get() > 0 || inserted.get() > 0;
        log.info("method=updateCameras operation=fetch updatedBoolean={} tookMs={} totalTimeMs={}", updatedCameras, timeFetch.getDuration().toMillis() + incrementalFetchTookMs.get(), timeAll.getDuration().toMillis());
        log.info("method=updateCameras operation=update updatedBoolean={} tookMs={} totalTimeMs={}", updatedCameras, timeUpdate.getDuration().toMillis() + incrementalUpdateTookMs.get(), timeAll.getDuration().toMillis());

        if (!errors.isEmpty()) {
            throw new RuntimeException(
                StringUtil.format("method=updateCameras There was {} errors and here is thrown the first of them", errors.size()), errors.getFirst());
        }

        return updatedCameras;
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 70000, maxErrorExcecutionTime = 100000) // FIXME: DPO-2248 This is taking too long
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
                errors.size()), errors.getFirst());
        }

        return updateCount;
    }


    private Pair<Integer, Integer> updateCameraStationAndPresets(final long cameraLotjuId, final AtomicLong incrementalFetchTookMs, final AtomicLong incrementalUpdateTookMs) {
        final KameraVO kamera = lotjuCameraStationMetadataClientWrapper.getKamera(cameraLotjuId);
        if (kamera == null) {
            log.warn("method=updateCameraStationAndPresets No Camera found with lotjuId={}", cameraLotjuId);
            return Pair.of(0,0);
        }
        return updateCameraStationAndPresets(kamera, incrementalFetchTookMs, incrementalUpdateTookMs);
    }

    /**
     * @param kamera Camera to update
     * @return Pair of updated and inserted count of presets
     */
    private Pair<Integer, Integer> updateCameraStationAndPresets(final KameraVO kamera, final AtomicLong incrementalFetchTookMs, final AtomicLong incrementalUpdateTookMs) {
        // Try to get lock for 10s and then gives up
        if (!cachedLockingService.lock(10000)) {
            throw new IllegalStateException(StringUtil.format("method=updateCameraStationAndPresets did not get the lock {}",
                    cachedLockingService.getLockInfoForLogging()));
        }
        try {
            log.debug("method=updateCameraStationAndPresets got the lock");
            if (!validate(kamera)) {
                return Pair.of(0,0);
            }
            final StopWatch timeFetch = StopWatch.createStarted();
            final List<EsiasentoVO> eas = lotjuCameraStationMetadataClientWrapper.getEsiasentos(kamera.getId());
            incrementalFetchTookMs.addAndGet(timeFetch.getDuration().toMillis());
            final StopWatch timeUpdate = StopWatch.createStarted();
            final Pair<Integer, Integer> result =
                    cameraStationUpdateService.updateOrInsertRoadStationAndPresets(kamera, eas);
            incrementalUpdateTookMs.addAndGet(timeUpdate.getDuration().toMillis());
            return result;

        } finally {
            cachedLockingService.deactivate();
        }
    }

    @PerformanceMonitor()
    public boolean updateCameraStation(final long cameraLotjuId,
                                       final MetadataUpdatedMessageDto.UpdateType updateType) {
        log.info("method=updateCameraStationFromJms start lotjuId={} type={}", cameraLotjuId, updateType);
        if (updateType.isDelete()) {
            return cameraStationUpdateService.updateStationToObsoleteWithLotjuId(cameraLotjuId);
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
        if (roadStationService.findByTypeAndLotjuId(RoadStationType.CAMERA_STATION, kamera.getId()).isEmpty()) {
            final Pair<Integer, Integer> updated = updateCameraStationAndPresets(kamera, new AtomicLong(), new AtomicLong());
            return updated.getLeft() > 0 || updated.getRight() > 0;
        }

        // Otherwise we update only the station
        // Try to get lock for 10s and then gives up
        if (!cachedLockingService.lock(10000)) {
            throw new IllegalStateException(StringUtil.format("method=updateCameraStation did not get the lock {}",
                    cachedLockingService.getLockInfoForLogging()));
        }
        try {
            log.debug("method=updateCameraStation got the lock lotjuId={}", kamera.getId());

            if (!validate(kamera)) {
                return false;
            }
            return cameraStationUpdateService.updateCamera(kamera);

        } finally {
            cachedLockingService.deactivate();
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
            final Pair<Integer, Integer> updated = updateCameraStationAndPresets(esiasento.getKameraId(), new AtomicLong(), new AtomicLong());
            return updated.getLeft() > 0 || updated.getRight() > 0;
        }

        // Otherwise update only the given preset
        log.debug("method=updateCameraPreset got the lock lotjuId={}", presetLotjuId);
        final KameraVO kamera = lotjuCameraStationMetadataClientWrapper.getKamera(esiasento.getKameraId());
        if (validate(kamera)) {
            // Try to get lock for 10s and then gives up
            if (!cachedLockingService.lock(10000)) {
                throw new IllegalStateException(StringUtil.format("method=updateCameraPreset did not get the lock {}",
                        cachedLockingService.getLockInfoForLogging()));
            }
            try {
                return cameraStationUpdateService.updatePreset(esiasento, kamera);
            } finally {
                cachedLockingService.deactivate();
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
