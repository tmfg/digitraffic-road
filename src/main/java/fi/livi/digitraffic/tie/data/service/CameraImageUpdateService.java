package fi.livi.digitraffic.tie.data.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.ely.lotju.kamera.proto.KuvaProtos;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.quartz.CameraMetadataUpdateJob;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;

@ConditionalOnNotWebApplication
@Service
public class CameraImageUpdateService {
    private static final Logger log = LoggerFactory.getLogger(CameraImageUpdateService.class);

    private final String sftpUploadFolder;
    private final int connectTimeout;
    private final int readTimeout;
    private final CameraPresetService cameraPresetService;
    private final SessionFactory sftpSessionFactory;
    private int retryDelayMs;

    @Value("${camera-image-download.url}")
    private String camera_url;

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

    public long deleteAllImagesForNonPublishablePresets() {
        // return count of succesful deletes
        return cameraPresetService.findAllNotPublishableCameraPresetsPresetIds().stream()
            .filter(presetId -> deleteImage(getPresetImageName(presetId)))
            .count();
    }

    @Transactional
    public boolean handleKuva(final KuvaProtos.Kuva kuva) {
        final StopWatch start = StopWatch.createStarted();
        log.info("method=handleKuva Handling {}", ToStringHelper.toString(kuva));

        final CameraPreset cameraPreset = cameraPresetService.findPublishableCameraPresetByLotjuId(kuva.getEsiasentoId());

        final String presetId = resolvePresetIdFrom(cameraPreset, kuva);
        final String filename = getPresetImageName(presetId);

        final boolean success;
        if (cameraPreset != null) {
            success = transferKuva(kuva, presetId, filename);
            updateCameraPreset(cameraPreset, kuva, success);
        } else {
            success = deleteKuva(kuva, presetId, filename);
        }

        if (!success) {
            log.error("method=handleKuva failed to {} for presetId={} {}", cameraPreset != null ? "transferKuva":"deleteKuva", presetId,
                                                                           ToStringHelper.toString(kuva));
        }
        log.info("method=handleKuva {} for presetId={} tookMs={} {}", success ? "success" : "failed", start.getTime(), presetId,
                                                                      success ? "" : ToStringHelper.toString(kuva));
        return success;
    }

    private boolean deleteKuva(KuvaProtos.Kuva kuva, String presetId, String filename) {
        log.info("method=deleteKuva Deleting presetId={} remote imagePath={}. The image is not publishable or preset was not included in previous run of" +
                "clazz={}. Kuva from incoming JMS: {}", presetId, getImageFullPath(filename),
            CameraMetadataUpdateJob.class.getName(), ToStringHelper.toString(kuva));

        return deleteImage(filename);
    }

    private boolean transferKuva(KuvaProtos.Kuva kuva, String presetId, String filename) {
        final StopWatch start = StopWatch.createStarted();
        // Read the image
        byte[] image = null;
        for (int readTries = 3; readTries > 0; readTries--) {
            try {
                image = readImage(getCameraDownloadUrl(kuva), filename);
                if (image.length > 0) {
                    break;
                } else {
                    log.warn("method=transferKuva Reading image for presetId={} from srcUri={} to sftpServerPath={} returned 0 bytes. triesLeft={} .",
                        presetId, getCameraDownloadUrl(kuva), getImageFullPath(filename), readTries - 1);
                }
            } catch (final Exception e) {
                log.warn("method=transferKuva Reading image for presetId={} from srcUri={} to sftpServerPath={} failed. triesLeft={} . exceptionMessage={} .",
                    presetId, getCameraDownloadUrl(kuva), getImageFullPath(filename), readTries - 1, e.getMessage());
            }
            try {
                Thread.sleep(retryDelayMs);
            } catch (InterruptedException e) {
                throw new Error(e);
            }
        }
        log.info("method=transferKuva readTookMs={}", start.getTime());
        if (image == null) {
            log.error("method=transferKuva Reading image failed for {} no retries remaining, transfer aborted.", ToStringHelper.toString(kuva));
            return false;
        }

        // Write the image
        final StopWatch writeStart = StopWatch.createStarted();
        boolean writtenSuccessfully = false;
        for (int writeTries = 3; writeTries > 0; writeTries--) {
            try {
                writeImage(image, filename);
                writtenSuccessfully = true;
                break;
            } catch (final Exception e) {
                log.warn("method=transferKuva Writing image for presetId={} from srcUri={} to sftpServerPath={} failed. triesLeft={}. exceptionMessage={}.",
                    presetId, getCameraDownloadUrl(kuva), getImageFullPath(filename), writeTries - 1, e.getMessage());
            }
            try {
                Thread.sleep(retryDelayMs);
            } catch (InterruptedException e) {
                throw new Error(e);
            }
        }
        log.info("method=transferKuva writerTookMs={}", writeStart.getTime());
        if (!writtenSuccessfully) {
            log.error("method=transferKuva Writing image failed for {} no retries remaining, transfer aborted.", ToStringHelper.toString(kuva));
            return false;
        }
        log.info("method=transferKuva tookMs={}", start.getTime());
        return true;
    }

    private byte[] readImage(final String downloadImageUrl, final String uploadImageFileName) throws IOException {
        log.info("method=readImage Read image url={} ( uploadFileName={} )", downloadImageUrl, uploadImageFileName);

        final URL url = new URL(downloadImageUrl);
        final URLConnection con = url.openConnection();
        con.setConnectTimeout(connectTimeout);
        con.setReadTimeout(readTimeout);
        try (final InputStream is = con.getInputStream()) {
            final byte[] result = IOUtils.toByteArray(is);
            log.info("method=readImage Image read successfully. imageSizeBytes={} bytes", result.length);
            return result;
        }
    }

    private void writeImage(byte[] data, String filename) throws IOException {
        final String uploadPath = getImageFullPath(filename);
        try (final Session session = sftpSessionFactory.getSession()) {
            log.info("method=writeImage Writing image to sftpServerPath={} started", uploadPath);
            session.write(new ByteArrayInputStream(data), uploadPath);
            log.info("method=writeImage Writing image to sftpServerPath={} ended successfully", uploadPath);
        } catch (Exception e) {
            log.warn("method=writeImage Failed to write image to sftpServerPath={} . mostSpecificCauseMessage={} . stackTrace={}", uploadPath, NestedExceptionUtils.getMostSpecificCause(e).getMessage(), ExceptionUtils.getStackTrace(e));
            throw e;
        }
    }

    private static void updateCameraPreset(final CameraPreset cameraPreset, final KuvaProtos.Kuva kuva, final boolean success) {
        if (cameraPreset.isPublicExternal() != kuva.getJulkinen()) {
            cameraPreset.setPublicExternal(kuva.getJulkinen());
            cameraPreset.setPictureLastModified(DateHelper.toZonedDateTimeAtUtc(Instant.ofEpochMilli(kuva.getAikaleima())));
            log.info("method=updateCameraPreset cameraPresetId={} isPublicExternal from {} to {} ", cameraPreset.getPresetId(), !kuva.getJulkinen(), kuva.getJulkinen());
        } else if (success) {
            cameraPreset.setPictureLastModified(DateHelper.toZonedDateTimeAtUtc(Instant.ofEpochMilli(kuva.getAikaleima())));
        }
    }

    private boolean deleteImage(final String deleteImageFileName) {
        try (final Session session = sftpSessionFactory.getSession()) {
            final String imageRemotePath = getImageFullPath(deleteImageFileName);
            if (session.exists(imageRemotePath) ) {
                log.info("Delete imagePath={}", imageRemotePath);
                session.remove(imageRemotePath);
                return true;
            }
            return false;
        } catch (IOException e) {
            log.error("Failed to remove remote file deleteImageFileName={}", getImageFullPath(deleteImageFileName));
            return false;
        }
    }

    static String resolvePresetIdFrom(final CameraPreset cameraPreset, final KuvaProtos.Kuva kuva) {
        return cameraPreset != null ? cameraPreset.getPresetId() : kuva.getNimi().substring(0, 8);
    }

    private static String getPresetImageName(final String presetId) {
        return  presetId + ".jpg";
    }

    private String getImageFullPath(final String imageFileName) {
        return StringUtils.appendIfMissing(sftpUploadFolder, "/") + imageFileName;
    }

    private String getCameraDownloadUrl(final KuvaProtos.Kuva kuva) {
        return StringUtils.appendIfMissing(camera_url, "/") + kuva.getKuvaId();
    }
}
