package fi.livi.digitraffic.tie.service.weathercam;

import static fi.livi.digitraffic.tie.model.DataType.CAMERA_STATION_IMAGE_UPDATED;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;

import fi.ely.lotju.kamera.proto.KuvaProtos;
import fi.livi.digitraffic.common.annotation.PerformanceMonitor;
import fi.livi.digitraffic.common.util.ThreadUtil;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.service.DataStatusService;

@ConditionalOnNotWebApplication
@Component
public class CameraImageUpdateManager {
    private static final Logger log = LoggerFactory.getLogger(CameraImageUpdateManager.class);

    private final int imageUpdateTimeout;
    private final CameraImageUpdateHandler cameraImageUpdateHandler;
    private final DataStatusService dataStatusService;

    private static final ExecutorService jobThreadPool = Executors.newFixedThreadPool(5);
    private static final ExecutorService updateTaskThreadPool = Executors.newFixedThreadPool(5);

    @Autowired
    CameraImageUpdateManager(@Value("${camera-image-uploader.imageUpdateTimeout}")
                             final int imageUpdateTimeout,
                             final CameraImageUpdateHandler cameraImageUpdateHandler,
                             final DataStatusService dataStatusService) {
        this.imageUpdateTimeout = imageUpdateTimeout;
        this.cameraImageUpdateHandler = cameraImageUpdateHandler;
        this.dataStatusService = dataStatusService;
    }

    // Log only on warn and error level. Warn when over 10 s execution time as normally its around 6 s.
    @PerformanceMonitor(maxInfoExcecutionTime = 100000, maxWarnExcecutionTime = 10000)
    public int updateCameraData(final List<KuvaProtos.Kuva> data) {
        final Collection<KuvaProtos.Kuva> latestKuvas = filterLatest(data);
        final List<Future<Boolean>> futures = new ArrayList<>();
        final StopWatch start = StopWatch.createStarted();

        latestKuvas.forEach(kuva -> {
            final UpdateJobManager task = new UpdateJobManager(kuva, cameraImageUpdateHandler, imageUpdateTimeout);
            futures.add(jobThreadPool.submit(task));
        });

        final Instant latestUpdate = getLatestUpdateTime(latestKuvas);
        dataStatusService.updateDataUpdated(CAMERA_STATION_IMAGE_UPDATED, latestUpdate);

        while ( futures.stream().anyMatch(f -> !f.isDone()) ) {
            ThreadUtil.delayMs(100L);
        }
        final long updateCount = futures.parallelStream().filter(p -> {
            try {
                    return p.get();
                } catch (final Exception e) {
                    log.error("method=updateCameraData UpdateJobManager task failed with error" , e);
                    return false;
                }
        }).count();


        log.info("method=updateCameraData Updating success for weather camera images updateCount={} of futuresCount={} failedCount={} tookMs={}", updateCount, futures.size(), futures.size()-updateCount, start.getTime());
        return (int) updateCount;
    }

    private Instant getLatestUpdateTime(final Collection<KuvaProtos.Kuva> latestKuvas) {
        try {
            return DateHelper.toInstant(latestKuvas.stream().mapToLong(KuvaProtos.Kuva::getAikaleima).max().orElseThrow());
        } catch (final NoSuchElementException e) {
            return null;
        }
    }

    private Collection<KuvaProtos.Kuva> filterLatest(final List<KuvaProtos.Kuva> data) {
        // Collect newest kuva per preset
        final HashMap<Long, KuvaProtos.Kuva> kuvaMappedByPresetLotjuId = new HashMap<>();
        data.forEach(kuva -> {
            if (kuva.hasEsiasentoId()) {
                final KuvaProtos.Kuva currentKamera = kuvaMappedByPresetLotjuId.get(kuva.getEsiasentoId());

                if ( currentKamera == null || currentKamera.getAikaleima() < kuva.getAikaleima()) {
                    if (currentKamera != null) {
                        log.info("Replace {} with {}", currentKamera.getAikaleima(), kuva.getAikaleima());
                    }
                    kuvaMappedByPresetLotjuId.put(kuva.getEsiasentoId(), kuva);
                }
            } else {
                log.warn("Kuva esiasentoId is not set: {}", ToStringHelper.toString(kuva));
            }
        });
        return kuvaMappedByPresetLotjuId.values();
    }


    private static class UpdateJobManager implements Callable<Boolean> {

        private final long timeout;
        private final ImageUpdateTask task;

        private UpdateJobManager(final KuvaProtos.Kuva kuva, final CameraImageUpdateHandler cameraImageUpdateHandler, final long timeout) {
            this.timeout = timeout;
            this.task = new ImageUpdateTask(kuva, cameraImageUpdateHandler);
        }

        @Override
        public Boolean call() {
            Future<Boolean> future = null;
            String presetId = null;
            try {
                presetId = CameraImageUpdateHandler.resolvePresetIdFrom(null, task.kuva);
                future = updateTaskThreadPool.submit(task);
                return future.get(timeout, TimeUnit.MILLISECONDS);
            } catch (final TimeoutException e) {
                log.error("ImageUpdateTasks failed to complete for presetId={} before timeoutMs={} ms", presetId, timeout);
            } catch (final Exception e) {
                log.error(String.format("ImageUpdateTasks failed to complete for presetId=%s with exception", presetId), e);
            } finally {
                // This is safe even if task is already finished
                if (future != null) {
                    future.cancel(true);
                }
            }
            return false;
        }
    }

    private static class ImageUpdateTask implements Callable<Boolean> {
        private final KuvaProtos.Kuva kuva;
        private final CameraImageUpdateHandler cameraImageUpdateHandler;

        private ImageUpdateTask(final KuvaProtos.Kuva kuva, final CameraImageUpdateHandler cameraImageUpdateHandler) {
            this.kuva = kuva;
            this.cameraImageUpdateHandler = cameraImageUpdateHandler;
        }

        @Override
        public Boolean call() {
            try {
                return cameraImageUpdateHandler.handleKuva(kuva);
            } catch (final Exception e) {
                log.error(String.format("Error while calling cameraImageUpdateService.handleKuva with %s", ToStringHelper.toString(kuva)), e);
                throw new RuntimeException(e);
            }
        }
    }
}
