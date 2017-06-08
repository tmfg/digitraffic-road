package fi.livi.digitraffic.tie.data.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.lotju.xsd.kamera.Kuva;

@Service
public class CameraDataUpdateService {
    private static final Logger log = LoggerFactory.getLogger(CameraDataUpdateService.class);

    private final CameraImageUpdateService cameraImageUpdateService;

    private static final ExecutorService jobThreadPool = Executors.newFixedThreadPool(5);
    private static final ExecutorService updateTaskThreadPool = Executors.newFixedThreadPool(5);

    @Autowired
    CameraDataUpdateService(final CameraImageUpdateService cameraImageUpdateService) {
        this.cameraImageUpdateService = cameraImageUpdateService;
    }

    public long updateCameraData(final List<Kuva> data) throws SQLException {

        final Collection<Kuva> latestKuvas = filterLatestKuvasAndMapByPresetId(data);
        final List<Future<Boolean>> futures = new ArrayList<>();
        final StopWatch start = StopWatch.createStarted();

        latestKuvas.forEach(kuva -> {
            final UpdateJobManager task = new UpdateJobManager(kuva, cameraImageUpdateService, 20000);
            futures.add(jobThreadPool.submit(task));
        });

        while ( futures.parallelStream().filter(f -> !f.isDone()).findFirst().isPresent() ) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                log.debug("InterruptedException", e);
            }
        }
        final long updateCount = futures.parallelStream().filter(p -> {
                try {
                    return p.get();
                } catch (Exception e) {
                    return false;
                }
            }).count();

        log.info("Updating success for {} weather camera images of {} took {} ms", updateCount, futures.size(), start.getTime());
        return updateCount;
    }

    private Collection<Kuva> filterLatestKuvasAndMapByPresetId(final List<Kuva> data) {
        // Collect newest kuva per preset
        final HashMap<Long, Kuva> kuvaMappedByPresetLotjuId = new HashMap<>();
        data.forEach(kuva -> {
            if (kuva.getEsiasentoId() != null) {
                Kuva currentKamera = kuvaMappedByPresetLotjuId.get(kuva.getEsiasentoId());
                if ( currentKamera == null || kuva.getAika().compare(currentKamera.getAika()) > 0 ) {
                    if (currentKamera != null) {
                        log.info("Replace " + currentKamera.getAika() + " with " + kuva.getAika());
                    }
                    kuvaMappedByPresetLotjuId.put(kuva.getEsiasentoId(), kuva);
                }
            } else {
                log.warn("Kuva esiasentoId is null: {}", ToStringHelper.toString(kuva));
            }
        });
        return kuvaMappedByPresetLotjuId.values();
    }


    private class UpdateJobManager implements Callable<Boolean> {

        protected final long timeout;
        protected final ImageUpdateTask task;

        public UpdateJobManager(final Kuva kuva, final CameraImageUpdateService cameraImageUpdateService, final long timeout) {
            this.timeout = timeout;
            this.task = new ImageUpdateTask(kuva, cameraImageUpdateService);
        }

        @Override
        public Boolean call() {
            final Future<Boolean> future = updateTaskThreadPool.submit(task);
            try {
                return future.get(timeout, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                log.error("ImageUpdateTasks failed to complete before timeout {} ms", timeout);
            } catch (Exception e) {
                log.error("ImageUpdateTasks failed to complete with exception", e);
            } finally {
                // This is safe even if task is already finished
                future.cancel(true);
            }

            return false;
        }
    }

    private static class ImageUpdateTask implements Callable<Boolean> {

        private final Kuva kuva;
        private final CameraImageUpdateService cameraImageUpdateService;

        public ImageUpdateTask(final Kuva kuva, CameraImageUpdateService cameraImageUpdateService) {
            this.kuva = kuva;
            this.cameraImageUpdateService = cameraImageUpdateService;
        }

        @Override
        public Boolean call() throws InterruptedException {
            return cameraImageUpdateService.handleKuva(kuva);
        }
    }
}
