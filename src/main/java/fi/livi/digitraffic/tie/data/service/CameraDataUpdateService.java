package fi.livi.digitraffic.tie.data.service;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void deleteAllImagesForNonPublishablePresets() {
        List<String> presetIdsToDelete = cameraPresetService.findAllNotPublishableCameraPresetsPresetIds();
        presetIdsToDelete.forEach(presetId -> deleteImageQuietly(getPresetImageName(presetId)));
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

        final List<Future<Boolean>> futures = new ArrayList<>();
        StopWatch start = StopWatch.createStarted();

        for (final CameraPreset cameraPreset : cameraPresets) {
            final Kuva kuva = kuvaMappedByPresetLotjuId.remove(cameraPreset.getLotjuId());
            if (kuva == null) {
                log.error("No kuva for preset {}", cameraPreset.toString());
            } else {
                futures.add(handleKuva(kuva, cameraPreset));
            }
        }

        // Handle missing presets to delete possible images from disk
        for (Kuva notFoundPresetsKuva : kuvaMappedByPresetLotjuId.values()) {
            futures.add(handleKuva(notFoundPresetsKuva, null));
        }

        while ( futures.parallelStream().filter(f -> !f.isDone()).findFirst().isPresent() ) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                log.debug("InterruptedException", e);
            }
        }

        log.info("Updating {} weather camera images too {} ms", futures.size(), start.getTime());
    }

    private String resolvePresetId(final Kuva kuva) {
        return kuva.getNimi().substring(0, 8);
    }

    private String getPresetImageName(final String presetId) {
        return  presetId + ".jpg";
    }

    private String getImageFullPath(String imageFileName) {
        return StringUtils.appendIfMissing(sftpUploadFolder, "/") + imageFileName;
    }

    private AsyncResult<Boolean> handleKuva(final Kuva kuva, final CameraPreset cameraPreset) {
        String presetId = cameraPreset != null ? cameraPreset.getPresetId() : resolvePresetId(kuva);
        String filename = getPresetImageName(presetId);
        log.info("Handling " + ToStringHelpper.toString(kuva));

        // Update CameraPreset
        if (cameraPreset != null) {
            ZonedDateTime pictureTaken = DateHelper.toZonedDateTime(kuva.getAika());
            cameraPreset.setPublicExternal(kuva.isJulkinen());
            cameraPreset.setPictureLastModified(pictureTaken);
        }

        return retryTemplate.execute(context -> {
            // Load and save image or delete non public image
            if (cameraPreset != null && cameraPreset.isPublicExternal() && cameraPreset.isPublicInternal()) {
                try {
                    uploadImage(kuva.getUrl(), filename);
                    return new AsyncResult<>(true);
                } catch (IOException e) {
                    log.error("Error reading or writing picture for presetId {} from {} to sftp server path {}",
                            presetId, kuva.getUrl(), getImageFullPath(filename));
                    log.error("Error", e);
                    return new AsyncResult<>(false);
                }
            } else {
                if (cameraPreset == null) {
                    log.info("Could not update non existing camera preset {}", presetId);
                }
                log.info("Delete hidden or missing preset's {} remote image {}", presetId, getImageFullPath(filename));
                return new AsyncResult<>(deleteImageQuietly(filename));
            }
        });
    }

    private boolean deleteImageQuietly(String deleteImageFileName) {
        try (Session session = sftpSessionFactory.getSession()) {
            final String imageRemotePath = getImageFullPath(deleteImageFileName);
            if (session.exists(imageRemotePath) ) {
                log.info("Delete image {}", imageRemotePath);
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
            URLConnection con = url.openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            final String uploadPath = getImageFullPath(uploadImageFileName);
            log.info("Download image {} and upload to sftp server path {}", downloadImageUrl, uploadPath);
            session.write(con.getInputStream(), uploadPath);
        } catch (Exception e) {
            log.error("Error while trying to upload image from {} to file {}", downloadImageUrl, getImageFullPath(uploadImageFileName));
            throw new RuntimeException(e);
        }
    }
}
