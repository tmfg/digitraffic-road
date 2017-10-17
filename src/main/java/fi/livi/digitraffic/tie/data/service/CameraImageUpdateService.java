package fi.livi.digitraffic.tie.data.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.time.ZonedDateTime;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.lotju.xsd.kamera.Kuva;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.quartz.CameraMetadataUpdateJob;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;

@Service
public class CameraImageUpdateService {
    private static final Logger log = LoggerFactory.getLogger(CameraImageUpdateService.class);

    private final String sftpUploadFolder;
    private final int connectTimeout;
    private final int readTimeout;
    private final CameraPresetService cameraPresetService;
    private final SessionFactory sftpSessionFactory;
    private int retryDelayMs;

    @Autowired
    CameraImageUpdateService(@Value("${camera-image-uploader.sftp.uploadFolder}")
                             final String sftpUploadFolder,
                             @Value("${camera-image-uploader.http.connectTimeout}")
                             final int connectTimeout,
                             @Value("${camera-image-uploader.http.readTimeout}")
                             final int readTimeout,
                             final CameraPresetService cameraPresetService,
                             @Qualifier("sftpSessionFactory")
                             final SessionFactory sftpSessionFactory,
                             @Value("${camera-image-uploader.retry.delay.ms}")
                             final int retryDelayMs) {
        this.sftpUploadFolder = sftpUploadFolder;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.cameraPresetService = cameraPresetService;
        this.sftpSessionFactory = sftpSessionFactory;
        this.retryDelayMs = retryDelayMs;
    }

    @Transactional(readOnly = true)
    public long deleteAllImagesForNonPublishablePresets() {
        List<String> presetIdsToDelete = cameraPresetService.findAllNotPublishableCameraPresetsPresetIds();
        return presetIdsToDelete.stream().filter(presetId -> deleteImage(getPresetImageName(presetId))).count();
    }

    @Transactional
    public boolean handleKuva(final Kuva kuva) {
        boolean rval;

        log.info("Handling {}", ToStringHelper.toString(kuva));

        final CameraPreset cameraPreset = cameraPresetService.findPublishableCameraPresetByLotjuId(kuva.getEsiasentoId());

        final String presetId = resolvePresetIdFrom(cameraPreset, kuva);
        final String filename = getPresetImageName(presetId);

        if (cameraPreset != null) {
            rval = saveKuva(kuva, presetId, filename);
        }
        else {
            rval = deleteKuva(kuva, presetId, filename);
        }

        updateCameraPreset(cameraPreset, kuva);

        return rval;
    }

    private boolean deleteKuva(Kuva kuva, String presetId, String filename) {

        log.info("Deleting preset's {} remote image {}. The image is not publishable or preset was not included in previous run of {}. Kuva from incoming JMS: {}", presetId, getImageFullPath(filename),
            CameraMetadataUpdateJob.class.getName(), ToStringHelper.toString(kuva));

        return deleteImage(filename);
    }

    private boolean saveKuva(Kuva kuva, String presetId, String filename) {
        // Read the image
        byte[] image = null;
        for (int readTries = 3; readTries > 0; readTries--) {
            try {
                image = readImage(kuva.getUrl(), filename);
                if (image.length > 0) {
                    break;
                } else {
                    log.warn("Reading image for presetId {} from {} to sftp server path {} returned 0 bytes. {} tries left.",
                        presetId, kuva.getUrl(), getImageFullPath(filename), readTries - 1);
                }
            } catch (final Exception e) {
                log.warn("Reading image for presetId {} from {} to sftp server path {} failed. {} tries left. Exception message: {}.",
                    presetId, kuva.getUrl(), getImageFullPath(filename), readTries - 1, e.getMessage());
            }
            try {
                Thread.sleep(retryDelayMs);
            } catch (InterruptedException e) {
                throw new Error(e);
            }
        }
        if (image == null) {
            log.error("Reading image failed for " + ToStringHelper.toString(kuva) + " no retries remaining, transfer aborted.");
            return false;
        }

        // Write the image
        boolean writtenSuccessfully = false;
        for (int writeTries = 3; writeTries > 0; writeTries--) {
            try {
                writeImage(image, filename);
                writtenSuccessfully = true;
                break;
            } catch (final Exception e) {
                log.warn("Writing image for presetId {} from {} to sftp server path {} failed. {} tries left. Exception message: {}.",
                    presetId, kuva.getUrl(), getImageFullPath(filename), writeTries - 1, e.getMessage());
            }
            try {
                Thread.sleep(retryDelayMs);
            } catch (InterruptedException e) {
                throw new Error(e);
            }
        }
        if (!writtenSuccessfully) {
            log.error("Writing image failed for " + ToStringHelper.toString(kuva) + " no retries remaining, transfer aborted.");
            return false;
        }
        return true;
    }

    private static boolean isPublicCameraPreset(final CameraPreset cameraPreset) {
        return cameraPreset != null && cameraPreset.isPublicExternal() && cameraPreset.isPublicInternal();
    }

    private byte[] readImage(final String downloadImageUrl, final String uploadImageFileName) throws IOException {
        log.info("Read image {} ({})", downloadImageUrl, uploadImageFileName);
        byte[] result;
        try {
            final URL url = new URL(downloadImageUrl);
            URLConnection con = url.openConnection();
            con.setConnectTimeout(connectTimeout);
            con.setReadTimeout(readTimeout);
            result = IOUtils.toByteArray(con.getInputStream());
        } catch (Exception e) {
            throw e;
        }
        final byte[] data = result;
        log.info("Image read successfully. Size {} bytes", data.length);
        return data;
    }

    private void writeImage(byte[] data, String filename) throws IOException {
        final String uploadPath = getImageFullPath(filename);
        try (final Session session = sftpSessionFactory.getSession()) {
            log.info("Writing image to sftp server path {} started", uploadPath);
            session.write(new ByteArrayInputStream(data), uploadPath);
            log.info("Writing image to sftp server path {} ended successfully", uploadPath);
        } catch (Exception e) {
            log.warn("Failed to write image to sftp server path {}. Most specific cause message {}. Stack trace: {}", uploadPath, NestedExceptionUtils.getMostSpecificCause(e).getMessage(), ExceptionUtils.getStackTrace(e));
            throw e;
        }
    }

    private static void updateCameraPreset(final CameraPreset cameraPreset, final Kuva kuva) {
        if (cameraPreset != null) {
            ZonedDateTime pictureTaken = DateHelper.toZonedDateTime(kuva.getAika());
            cameraPreset.setPublicExternal(kuva.isJulkinen());
            cameraPreset.setPictureLastModified(pictureTaken);
        }
    }

    private boolean deleteImage(final String deleteImageFileName) {
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

    public static String resolvePresetIdFrom(final CameraPreset cameraPreset, final Kuva kuva) {
        return cameraPreset != null ? cameraPreset.getPresetId() : kuva.getNimi().substring(0, 8);
    }

    private static String getPresetImageName(final String presetId) {
        return  presetId + ".jpg";
    }

    private String getImageFullPath(final String imageFileName) {
        return StringUtils.appendIfMissing(sftpUploadFolder, "/") + imageFileName;
    }
}
