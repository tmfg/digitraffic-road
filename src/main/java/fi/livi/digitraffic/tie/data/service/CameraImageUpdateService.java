package fi.livi.digitraffic.tie.data.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.time.ZonedDateTime;

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

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

import fi.ely.lotju.kamera.proto.KuvaProtos;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;

@ConditionalOnNotWebApplication
@Service
public class CameraImageUpdateService {
    private static final Logger log = LoggerFactory.getLogger(CameraImageUpdateService.class);

    private final static int MAX_IMG_READ_ATTEMPTS = 3;
    private final static int MAX_IMG_WRITE_ATTEMPTS = 3;
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
            .filter(presetId -> deleteImage(getImageFullPath(getPresetImageName(presetId))).isFileExistsAndDeleteSuccess())
            .count();
    }

    @Transactional
    public boolean handleKuva(final KuvaProtos.Kuva kuva) {
        final StopWatch start = StopWatch.createStarted();
        log.info("method=handleKuva Handling {}", ToStringHelper.toString(kuva));

        final CameraPreset cameraPreset = cameraPresetService.findPublishableCameraPresetByLotjuId(kuva.getEsiasentoId());

        final String presetId = resolvePresetIdFrom(cameraPreset, kuva);
        final String filename = getPresetImageName(presetId);
        final String imageFullPath = getImageFullPath(filename);

        final boolean success;
        if (cameraPreset != null) {
            success = transferKuva(kuva, presetId, imageFullPath);
            updateCameraPreset(cameraPreset, kuva, success);
        } else {
            final DeleteInfo result = deleteImage(imageFullPath);
            success = !result.isFileExists() || result.isDeleteSuccess();
        }

        log.info("method=handleKuva {} for {} presetId={} tookMs={} {}",
            success ? "success" : "failed",
            cameraPreset != null ? "transferKuva":"deleteKuva",
            presetId,
            start.getTime(),
            ToStringHelper.toString(kuva));
        return success;
    }

    private boolean transferKuva(final KuvaProtos.Kuva kuva, final String presetId, final String imageFullPath) {
        final StopWatch start = StopWatch.createStarted();
        // Read the image
        byte[] image = null;

        Exception lastReadException = null;
        final String imageDownloadUrl = getCameraDownloadUrl(kuva);

        for (int readAttempts = MAX_IMG_READ_ATTEMPTS; readAttempts > 0; readAttempts--) {
            try {
                image = readImage(imageDownloadUrl);
                if (image.length > 0) {
                    break;
                }
            } catch (final Exception e) {
                lastReadException = e;
            }
            try {
                Thread.sleep(retryDelayMs);
            } catch (InterruptedException e) {
                throw new Error(e);
            }
        }

        if (image != null) {
            log.info("method=transferKuva phase=readImage presetId={} readTookMs={} readImage url={} uploadFileName={} imageSizeBytes={}",
                     presetId, start.getTime(), imageDownloadUrl, imageFullPath, image.length);
        } else {
            log.error(String.format("method=transferKuva phase=readImage presetId=%s readTookMs=%d url=%s uploadFileName=%s retried %d times but reading image failed for %s, transfer aborted.",
                                    presetId, start.getTime(), imageDownloadUrl, imageFullPath, MAX_IMG_READ_ATTEMPTS, ToStringHelper.toString(kuva)),
                      lastReadException);
            return false;
        }

        // Write the image
        final StopWatch writeStart = StopWatch.createStarted();
        boolean writtenSuccessfully = false;
        Exception lastWriteException = null;
        final int imageTimestampEpochSecond = (int) (kuva.getAikaleima() / 1000);

        for (int writeAttempts = MAX_IMG_WRITE_ATTEMPTS; writeAttempts > 0; writeAttempts--) {
            try {
                writeImage(image, imageFullPath, imageTimestampEpochSecond);
                writtenSuccessfully = true;
                break;
            } catch (final Exception e) {
                lastWriteException = e;
            }
            try {
                Thread.sleep(retryDelayMs);
            } catch (InterruptedException e) {
                throw new Error(e);
            }
        }

        if (writtenSuccessfully) {
            log.info("method=transferKuva phase=writeImage presetId={} writerTookMs={} uploadFileName={} imageTimestamp={}", presetId, writeStart.getTime(), imageFullPath, Instant.ofEpochSecond(imageTimestampEpochSecond));
        } else {
            log.error(String.format("method=transferKuva phase=writeImage presetId=%s writerTookMs=%d retried %d times but writing image failed for %s, transfer aborted.",
                                    presetId, writeStart.getTime(), MAX_IMG_WRITE_ATTEMPTS, ToStringHelper.toString(kuva)),
                      lastWriteException);
            return false;
        }
        log.info("method=transferKuva presetId={} tookMs={}", presetId, start.getTime());
        return true;
    }

    private byte[] readImage(final String imageDownloadUrl) throws IOException {
        final URL url = new URL(imageDownloadUrl);
        final URLConnection con = url.openConnection();
        con.setConnectTimeout(connectTimeout);
        con.setReadTimeout(readTimeout);
        try (final InputStream is = con.getInputStream()) {
            return IOUtils.toByteArray(is);
        }
    }

    private void writeImage(final byte[] data, final String imageFullPath, final int timestampEpochSecond) throws IOException, SftpException {

        try (final Session session = sftpSessionFactory.getSession()) {
            session.write(new ByteArrayInputStream(data), imageFullPath);
            ((ChannelSftp)session.getClientInstance()).setMtime(imageFullPath, timestampEpochSecond);
        } catch (Exception e) {
            log.warn("method=writeImage Failed to write image to sftpServerPath={} . mostSpecificCauseMessage={} . stackTrace={}", imageFullPath, NestedExceptionUtils.getMostSpecificCause(e).getMessage(), ExceptionUtils.getStackTrace(e));
            throw e;
        }
    }

    private static void updateCameraPreset(final CameraPreset cameraPreset, final KuvaProtos.Kuva kuva, final boolean success) {
        final ZonedDateTime lastModified = DateHelper.toZonedDateTimeAtUtc(Instant.ofEpochMilli(kuva.getAikaleima()));
        if (cameraPreset.isPublicExternal() != kuva.getJulkinen()) {
            cameraPreset.setPublicExternal(kuva.getJulkinen());
            cameraPreset.setPictureLastModified(lastModified);
            log.info("method=updateCameraPreset cameraPresetId={} isPublicExternal from {} to {} lastModified={}", cameraPreset.getPresetId(), !kuva.getJulkinen(), kuva.getJulkinen(), lastModified);
        } else if (success) {
            cameraPreset.setPictureLastModified(lastModified);
        }
    }

    /**
     * @param imageFullPath file path to delete
     * @return Info if the file exists and delete success. For non existing images success is false.
     */
    private DeleteInfo deleteImage(final String imageFullPath) {
        try (final Session session = sftpSessionFactory.getSession()) {
            if (session.exists(imageFullPath) ) {
                log.info("method=deleteImage presetId={} imagePath={}", resolvePresetIdFromImageFullPath(imageFullPath), imageFullPath);
                session.remove(imageFullPath);
                return new DeleteInfo(true, true);
            }
            return new DeleteInfo(false, false);
        } catch (IOException e) {
            log.error(String.format("Failed to remove remote file deleteImageFileName=%s", imageFullPath), e);
            return new DeleteInfo(true, false);
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

    private static String resolvePresetIdFromImageFullPath(final String imageFullPath) {

        return StringUtils.substringBeforeLast(StringUtils.substringAfterLast(imageFullPath,"/"), ".");
    }

    private String getCameraDownloadUrl(final KuvaProtos.Kuva kuva) {
        return StringUtils.appendIfMissing(camera_url, "/") + kuva.getKuvaId();
    }

    private static class DeleteInfo {
        private final boolean fileExists;
        private final boolean deleteSuccess;

        private DeleteInfo(boolean fileExists, boolean deleteSuccess) {
            this.fileExists = fileExists;
            this.deleteSuccess = deleteSuccess;
        }

        public boolean isFileExists() {
            return fileExists;
        }

        public boolean isDeleteSuccess() {
            return deleteSuccess;
        }
        public boolean isFileExistsAndDeleteSuccess() {
            return fileExists && deleteSuccess;
        }
    }
}













