package fi.livi.digitraffic.tie.data.service;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sun.javafx.binding.StringFormatter;

import fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.lotju.xsd.kamera.Kuva;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;

@Service
public class CameraDataUpdateService {
    private static final Logger log = LoggerFactory.getLogger(CameraDataUpdateService.class);

    private String sftpUploadFolder;

    private CameraPresetService cameraPresetService;

    private final SessionFactory sftpSessionFactory;
    private final RetryTemplate retryTemplate;

    private final HashMap<String, Integer> countMap = new HashMap<>();

    private static final Long MAX_EXECUTION_TIME_PER_IMAGE = 5000L;

    @Autowired
    CameraDataUpdateService(@Value("${camera-image-uploader.sftp.uploadFolder}")
                            final String sftpUploadFolder,
                            final CameraPresetService cameraPresetService,
                            SessionFactory sftpSessionFactory,
                            RetryTemplate retryTemplate) {
        this.sftpUploadFolder = sftpUploadFolder;
        this.cameraPresetService = cameraPresetService;
        this.sftpSessionFactory = sftpSessionFactory;
        this.retryTemplate = retryTemplate;
    }

    @Transactional
    public void updateCameraData(final List<Kuva> data) throws SQLException {
        // Collect newest data per station
        HashMap<Long, Kuva> kuvaMappedByPresetLotjuId = new HashMap<>();
        data.stream().forEach(k -> {
            if (k.getEsiasentoId() != null) {
                Kuva currentKamera = kuvaMappedByPresetLotjuId.get(k.getEsiasentoId());
                if (currentKamera == null || currentKamera.getAika().toGregorianCalendar().before(currentKamera.getAika().toGregorianCalendar())) {
                    if (currentKamera != null) {
                        log.info("Replace " + currentKamera.getAika() + " with " + currentKamera.getAika());
                    }
                    kuvaMappedByPresetLotjuId.put(k.getEsiasentoId(), k);
                }
            } else {
                log.warn("Kuva esiasentoId is null: {}", ToStringHelpper.toString(k));
            }
        });

        final List<CameraPreset> cameraPresets = cameraPresetService.findPublishableCameraPresetByLotjuIdIn(kuvaMappedByPresetLotjuId.keySet());

        final ExecutorService executor = Executors.newFixedThreadPool(5);
        final CompletionService<Boolean> completionService = new ExecutorCompletionService<>(executor);

        final HashMap<String, ImageFetcherAndUploader> fetchers = new HashMap<>();

        for (final CameraPreset cameraPreset : cameraPresets) {
            final Kuva kuva = kuvaMappedByPresetLotjuId.remove(cameraPreset.getLotjuId());
            if (kuva == null) {
                log.error("No kuva for preset {}", cameraPreset.toString());
            } else {
                fetchers.put(
                        cameraPreset.getPresetId(),
                        createImageFetcherAndSubmit(kuva, cameraPreset, completionService));
            }
        }

        // Handle missing presets to delete possible images from disk
        for (Kuva notFoundPresetForKuva : kuvaMappedByPresetLotjuId.values()) {
            fetchers.put(
                    resolvePresetId(notFoundPresetForKuva),
                    createImageFetcherAndSubmit(notFoundPresetForKuva, null, completionService));
        }

        while (!fetchers.isEmpty()) {
            // Remove completed fetchers
            Set<String> completed =
                    fetchers.entrySet().stream().filter(es -> es.getValue().isDone()).map(es -> es.getKey()).collect(Collectors.toSet());
            completed.forEach(presetId -> fetchers.remove(presetId));
            // Collect fetchers that has started
            Set<String> toCancel = fetchers.entrySet().stream().filter(f -> f.getValue().getDuration() >= MAX_EXECUTION_TIME_PER_IMAGE)
                    .map(es -> es.getKey()).collect(Collectors.toSet());
            toCancel.forEach(presetId -> fetchers.remove(presetId).cancel());
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                log.debug(e.getMessage(), e);
            }
        }
        executor.shutdown();
    }

    private ImageFetcherAndUploader createImageFetcherAndSubmit(final Kuva kuva, final CameraPreset preset, final CompletionService<Boolean> completionService) {
        ImageFetcherAndUploader fetcher = new ImageFetcherAndUploader(kuva, preset);
        fetcher.setFuture(completionService.submit(fetcher));
        return fetcher;
    }

    private String resolvePresetId(final Kuva kuva) {
        return kuva.getNimi().substring(0, 8);
    }

    private boolean handleKuva(final Kuva kuva, final CameraPreset cameraPreset) {
        String presetId = cameraPreset != null ? cameraPreset.getPresetId() : resolvePresetId(kuva);
        String filename = presetId + ".jpg";
        log.info("Handling kuva: " + ToStringHelpper.toString(kuva));

        // Update CameraPreset
        if (cameraPreset != null) {
            ZonedDateTime pictureTaken = DateHelper.toZonedDateTime(kuva.getAika());
            cameraPreset.setPublicExternal(kuva.isJulkinen());
            cameraPreset.setPictureLastModified(pictureTaken);
        }
        // Load and save image or delete non public image
        if (cameraPreset != null && cameraPreset.isPublicExternal() && cameraPreset.isPublicInternal()) {
            return retryTemplate.execute(context -> {
                try {
                    context.setAttribute(MetadataApplicationConfiguration.RETRY_OPERATION, StringFormatter.format("UploadImage from %s to %s", kuva.getUrl(), getImageFullPath(filename)));
                    uploadImage(kuva.getUrl(), filename);
                    return true;
                } catch (IOException e) {
                    log.error("Error reading or writing picture for presetId {} from {} to sftp server path {}",
                            presetId, kuva.getUrl(), getImageFullPath(filename));
                    log.error("Error", e);
                    return false;
                }
            });
        } else {
            if (cameraPreset == null) {
                log.info("Could not update non existing camera preset {}", presetId);
            }
            log.info("Delete hidden or missing preset's {} remote image {}", presetId, getImageFullPath(filename));
            return deleteImageQuietly(filename);
        }
    }

    private String getImageFullPath(String imageFileName) {
        return StringUtils.appendIfMissing(sftpUploadFolder, "/") + imageFileName;
    }

    private boolean deleteImageQuietly(String deleteImageFileName) {
        try (Session session = sftpSessionFactory.getSession()) {
            final String imageRemotePath = getImageFullPath(deleteImageFileName);
            if (session.exists(imageRemotePath) ) {
                session.remove(imageRemotePath);
            }
            return true;
        } catch (IOException e) {
            log.error("Failed to remove remote file {}", getImageFullPath(deleteImageFileName));
            return false;
        }
    }

    private void uploadImage(final String downloadImageUrl, final String uploadImageFileName) throws IOException {
        try (final Session session = sftpSessionFactory.getSession()) {
            final URL url = new URL(downloadImageUrl);
            final String uploadPath = getImageFullPath(uploadImageFileName);
            log.info("Download image {} and upload to sftp server path {}", downloadImageUrl, uploadPath);
            session.write(url.openStream(), uploadPath);
        } catch (Exception e) {
            log.error("Error while trying to upload image from {} to file {}", downloadImageUrl, getImageFullPath(uploadImageFileName));
            throw new RuntimeException(e);
        }
    }

    private class ImageFetcherAndUploader implements Callable<Boolean> {

        private final Kuva kuva;
        private final CameraPreset cameraPreset;
        private Future<Boolean> future;
        private StopWatch stopWatch = new StopWatch();

        public ImageFetcherAndUploader(final Kuva kuva, final CameraPreset cameraPreset) {
            this.kuva = kuva;
            this.cameraPreset = cameraPreset;
        }

        @Override
        public Boolean call() throws Exception {
            stopWatch.start();
            return handleKuva(kuva, cameraPreset);
        }

        public Future<Boolean> getFuture() {
            return future;
        }

        public void setFuture(Future<Boolean> future) {
            this.future = future;
        }

        public Long getDuration() {
            return stopWatch.getTime();
        }

        public boolean isDone() {
            return future.isDone();
        }

        public void cancel() {
            future.cancel(true);
        }
    }

}
