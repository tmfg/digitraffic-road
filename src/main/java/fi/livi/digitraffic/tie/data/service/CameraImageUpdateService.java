package fi.livi.digitraffic.tie.data.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.lotju.xsd.kamera.Kuva;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;

@Service
public class CameraImageUpdateService {
    private static final Logger log = LoggerFactory.getLogger(CameraImageUpdateService.class);

    private final String sftpUploadFolder;
    private final CameraPresetService cameraPresetService;
    private final SessionFactory sftpSessionFactory;
    private final RetryTemplate retryTemplate;

    @Autowired
    CameraImageUpdateService(@Value("${camera-image-uploader.sftp.uploadFolder}")
                             final String sftpUploadFolder,
                             final CameraPresetService cameraPresetService,
                             final SessionFactory sftpSessionFactory,
                             final RetryTemplate retryTemplate) {
        this.sftpUploadFolder = sftpUploadFolder;
        this.cameraPresetService = cameraPresetService;
        this.sftpSessionFactory = sftpSessionFactory;
        this.retryTemplate = retryTemplate;
    }


    @Transactional(readOnly = true)
    public void deleteAllImagesForNonPublishablePresets() {
        List<String> presetIdsToDelete = cameraPresetService.findAllNotPublishableCameraPresetsPresetIds();
        presetIdsToDelete.forEach(presetId -> deleteImageQuietly(getPresetImageName(presetId)));
    }

    @Transactional
    @Async
    public Future<Boolean> handleKuva(final Kuva kuva, final CameraPreset cameraPreset) {
        log.info("Handling {}", ToStringHelpper.toString(kuva));
        // Update CameraPreset properties
        updateCameraPreset(cameraPreset, kuva);
        // Download image from http-server and upload it to sftp-server
        return updateImage(cameraPreset, kuva);
    }

    private Future<Boolean> updateImage(CameraPreset cameraPreset, Kuva kuva) {
        return retryTemplate.execute(context -> {
            final String presetId = resolvePresetIdFrom(cameraPreset, kuva);
            final String filename = getPresetImageName(presetId);

            // Load and save image or delete non public image
            if (isPublicCameraPreset(cameraPreset)) {
                try {
                    downloadAndUploadImage(kuva.getUrl(), filename);
                    return new AsyncResult<>(true);
                } catch (IOException e) {
                    log.warn("Reading or writing picture for presetId {} from {} to sftp server path {} failed",
                              presetId, kuva.getUrl(), getImageFullPath(filename));
                    return new AsyncResult<>(false);
                }
            } else {
                if (cameraPreset == null) {
                    log.info("Could not update non existing camera preset for kuva {}", ToStringHelpper.toString(kuva));
                }
                log.info("Delete {} preset's {} remote image {}",
                         cameraPreset != null ? "hidden" : "missing", presetId, getImageFullPath(filename));
                return new AsyncResult<>(deleteImageQuietly(filename));
            }
        });
    }

    private static boolean isPublicCameraPreset(final CameraPreset cameraPreset) {
        return cameraPreset != null && cameraPreset.isPublicExternal() && cameraPreset.isPublicInternal();
    }

    private static void updateCameraPreset(final CameraPreset cameraPreset, final Kuva kuva) {
        if (cameraPreset != null) {
            ZonedDateTime pictureTaken = DateHelper.toZonedDateTime(kuva.getAika());
            cameraPreset.setPublicExternal(kuva.isJulkinen());
            cameraPreset.setPictureLastModified(pictureTaken);
        }
    }

    public boolean deleteImageQuietly(final String deleteImageFileName) {
        try (final Session session = sftpSessionFactory.getSession()) {
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

    private void downloadAndUploadImage(final String downloadImageUrl, final String uploadImageFileName) throws IOException {
        try (final Session session = sftpSessionFactory.getSession()) {
            final URL url = new URL(downloadImageUrl);
            URLConnection con = url.openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            final String uploadPath = getImageFullPath(uploadImageFileName);
            log.info("Download image {}", downloadImageUrl);
            byte[] bytes = null;
            try {
                bytes = IOUtils.toByteArray(con.getInputStream());
            } catch (IOException e) {
                log.warn("Download image {} failed", downloadImageUrl);
                throw e;
            }
            log.info("Upload image to sftp server {}", uploadPath);
            try {
                session.write(new ByteArrayInputStream(bytes), uploadPath);
            } catch (IOException e) {
                log.warn("Upload image to sftp server {} failed", uploadPath);
                throw e;
            }
        }
    }

    private static String resolvePresetIdFrom(final CameraPreset cameraPreset, final Kuva kuva) {
        return cameraPreset != null ? cameraPreset.getPresetId() : kuva.getNimi().substring(0, 8);
    }

    private static String getPresetImageName(final String presetId) {
        return  presetId + ".jpg";
    }

    private String getImageFullPath(final String imageFileName) {
        return StringUtils.appendIfMissing(sftpUploadFolder, "/") + imageFileName;
    }
}
