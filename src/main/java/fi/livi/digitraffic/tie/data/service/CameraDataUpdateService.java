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

    private static final ExecutorService threadpool = Executors.newFixedThreadPool(5);

    @Autowired
    CameraDataUpdateService(final CameraImageUpdateService cameraImageUpdateService) {
        this.cameraImageUpdateService = cameraImageUpdateService;
    }

    public void updateCameraData(final List<Kuva> data) throws SQLException {

        final Collection<Kuva> latestKuvas = filterLatestKuvasAndMapByPresetId(data);
        final long maxExecutionTime = latestKuvas.size() * 10000; // 10 s per image
        final List<Future<Boolean>> futures = new ArrayList<>();
        final StopWatch start = StopWatch.createStarted();

        latestKuvas.forEach(kuva -> {
            final ImageUpdateTask task = new ImageUpdateTask(kuva, cameraImageUpdateService);
            futures.add(threadpool.submit(task));
        });

        while ( futures.parallelStream().filter(f -> !f.isDone()).findFirst().isPresent() ) {
            try {
                if (start.getTime() > maxExecutionTime) {
                    log.error("Max execution time for ImageUpdateTasks to complete exceeded with {} kuvas and max total time of {} ms", latestKuvas.size(), maxExecutionTime);
                    long stopped = futures.stream().filter(f -> f.cancel(true)).count();
                    log.error("Stopped {} uncompleted update executions of {} ImageUpdateTasks", stopped, futures.size());
                    break;
                } else {
                    Thread.sleep(100L);
                }
            } catch (InterruptedException e) {
                log.debug("InterruptedException", e);
            }
        }

        log.info("Updating {} weather camera images took {} ms", futures.size(), start.getTime());
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

    private static class ImageUpdateTask implements Callable<Boolean> {

        private final Kuva kuva;
        private CameraImageUpdateService cameraImageUpdateService;

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
