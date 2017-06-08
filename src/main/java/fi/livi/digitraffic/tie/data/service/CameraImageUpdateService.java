package fi.livi.digitraffic.tie.data.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.time.ZonedDateTime;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.lotju.xsd.kamera.Kuva;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;

@Service
public class CameraImageUpdateService {
    private static final Logger log = LoggerFactory.getLogger(CameraImageUpdateService.class);

    private final String sftpUploadFolder;
    private final int connectTimeout;
    private final int readTimeout;
    private final CameraPresetService cameraPresetService;
    private final SessionFactory sftpSessionFactory;

    @Autowired
    CameraImageUpdateService(@Value("${camera-image-uploader.sftp.uploadFolder}")
                             final String sftpUploadFolder,
                             @Value("${camera-image-uploader.http.connectTimeout}")
                             final int connectTimeout,
                             @Value("${camera-image-uploader.http.readTimeout}")
                             final int readTimeout,
                             final CameraPresetService cameraPresetService,
                             final SessionFactory sftpSessionFactory) {
        this.sftpUploadFolder = sftpUploadFolder;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.cameraPresetService = cameraPresetService;
        this.sftpSessionFactory = sftpSessionFactory;
    }

    @Transactional(readOnly = true)
    public long deleteAllImagesForNonPublishablePresets() {
        List<String> presetIdsToDelete = cameraPresetService.findAllNotPublishableCameraPresetsPresetIds();
        return presetIdsToDelete.stream().filter(presetId -> deleteImageQuietly(getPresetImageName(presetId))).count();
    }

    @Transactional
    @Retryable(maxAttempts = 5)
    public boolean handleKuva(final Kuva kuva) {
        log.info("Handling {}", ToStringHelper.toString(kuva));
        final CameraPreset cameraPreset = cameraPresetService.findPublishableCameraPresetByLotjuId(kuva.getEsiasentoId());
        // Update preset attributes
        updateCameraPreset(cameraPreset, kuva);
        // Download image from http-server and upload it to sftp-server
        return updateImage(cameraPreset, kuva);
    }

    private boolean updateImage(CameraPreset cameraPreset, Kuva kuva) {
        final String presetId = resolvePresetIdFrom(cameraPreset, kuva);
        final String filename = getPresetImageName(presetId);

        // Load and save image or delete non public image
        if (isPublicCameraPreset(cameraPreset)) {
            try {
                downloadAndUploadImage(kuva.getUrl(), filename);
                return true;
            } catch (IOException e) {
                log.warn("Reading or writing picture for presetId {} from {} to sftp server path {} failed",
                          presetId, kuva.getUrl(), getImageFullPath(filename));
                return false;
            }
        } else {
            if (cameraPreset == null) {
                log.info("Could not update non existing camera preset for kuva {}", ToStringHelper.toString(kuva));
            }
            log.info("Delete {} preset's {} remote image {}",
                     cameraPreset != null ? "hidden" : "missing", presetId, getImageFullPath(filename));
            return deleteImageQuietly(filename);
        }
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

    private boolean deleteImageQuietly(final String deleteImageFileName) {
        try (final Session session = sftpSessionFactory.getSession()) {
            final String imageRemotePath = getImageFullPath(deleteImageFileName);
            if (session.exists(imageRemotePath) ) {
                log.info("Delete image {}", imageRemotePath);
                session.remove(imageRemotePath);
                return true;
            }
            return false;
        } catch (IOException e) {
            log.error("Failed to remove remote file {}", getImageFullPath(deleteImageFileName));
            return false;
        }
    }

    private void downloadAndUploadImage(final String downloadImageUrl, final String uploadImageFileName) throws IOException {
        log.info("Download image {} ({})", downloadImageUrl, uploadImageFileName);
        final byte[] data = downloadImage(downloadImageUrl);
        log.info("Image downloaded of size {} bytes", data.length);
        try (final Session session = sftpSessionFactory.getSession()) {
            final String uploadPath = getImageFullPath(uploadImageFileName);
            log.info("Upload image to sftp server path {}", uploadPath);
            session.write(new ByteArrayInputStream(data), uploadPath);
        } catch (Exception e) {
            log.error("Error while trying to upload image to sftp server path {}", getImageFullPath(uploadImageFileName));
            throw new RuntimeException(e);
        }
    }

    private byte[] downloadImage(final String downloadImageUrl) {
        try {
            final URL url = new URL(downloadImageUrl);
            URLConnection con = url.openConnection();
            con.setConnectTimeout(connectTimeout);
            con.setReadTimeout(readTimeout);
            return IOUtils.toByteArray(con.getInputStream());
        } catch (Exception e) {
            log.error("Error while trying to download image from {}", downloadImageUrl);
            throw new RuntimeException(e);
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
