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

    private enum Status { SUCCESS, FAILED, NONE;

        public boolean isSuccess() {
            return this.equals(SUCCESS);
        }
    }

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
        if (log.isDebugEnabled()) {
            log.debug("method=handleKuva Handling {}", ToStringHelper.toString(kuva));
        }

        final CameraPreset cameraPreset = cameraPresetService.findPublishableCameraPresetByLotjuId(kuva.getEsiasentoId());

        final String presetId = resolvePresetIdFrom(cameraPreset, kuva);
        final String filename = getPresetImageName(presetId);
        final String imageFullPath = getImageFullPath(filename);

        if (cameraPreset != null) {
            final ImageUpdateInfo transferInfo = transferKuva(kuva, presetId, imageFullPath);
            updateCameraPreset(cameraPreset, kuva, transferInfo.isSuccess());

            log.info("method=handleKuva presetId={} uploadFileName={} readImageStatus={} writeImageStatus={} " +
                    "readTookMs={} writeTooksMs={} tookMs={} " +
                    "downloadImageUrl={} imageSizeBytes={}",
                presetId, transferInfo.getFullPath(), transferInfo.getReadStatus(), transferInfo.getWriteStatus(),
                transferInfo.getReadDurationMs(), transferInfo.getWriteDurationMs(), transferInfo.getDurationMs(),
                transferInfo.getDownloadUrl(), transferInfo.getSizeBytes());
            return transferInfo.isSuccess();
        } else {
            final DeleteInfo deleteInfo = deleteImage(imageFullPath);
            log.info("method=handleKuva presetId={} deleteFileName={} fileExists={} deleteSuccess={} tookMs={}",
                presetId, imageFullPath, deleteInfo.isFileExists(), deleteInfo.isDeleteSuccess(), deleteInfo.getDurationMs());
            return deleteInfo.isSuccess();
        }
    }

    private ImageUpdateInfo transferKuva(final KuvaProtos.Kuva kuva, final String presetId, final String imageFullPath) {
        final StopWatch start = StopWatch.createStarted();

        // Read the image
        byte[] image = null;

        Exception lastReadException = null;
        final String imageDownloadUrl = getCameraDownloadUrl(kuva);

        final ImageUpdateInfo info = new ImageUpdateInfo();
        info.setDownloadUrl(imageDownloadUrl);
        info.setPresetId(presetId);
        info.setFullPath(imageFullPath);

        for (int readAttempts = MAX_IMG_READ_ATTEMPTS; readAttempts > 0; readAttempts--) {
            try {
                image = readImage(imageDownloadUrl);
                if (image.length > 0) {
                    lastReadException = null;
                    info.setSizeBytes(image.length);
                    info.setReadStatus(Status.SUCCESS);
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

        info.setReadDurationMs(start.getTime());

        if (info.getReadStatus().isSuccess()) {
            log.debug("method=transferKuva phase=readImage readStatus={} presetId={} readTookMs={} readImage url={} uploadFileName={} imageSizeBytes={}",
                      Status.SUCCESS, presetId, start.getTime(), imageDownloadUrl, imageFullPath, image.length);
        } else {
            log.error(String.format("method=transferKuva phase=readImage readStatus={} presetId=%s readTookMs=%d url=%s uploadFileName=%s retried %d times but reading image failed for %s, transfer aborted.",
                                    Status.FAILED, presetId, start.getTime(), imageDownloadUrl, imageFullPath, MAX_IMG_READ_ATTEMPTS, ToStringHelper.toString(kuva)),
                      lastReadException);
            return info;
        }

        // Write the image
        final StopWatch writeStart = StopWatch.createStarted();
        Exception lastWriteException = null;
        final int imageTimestampEpochSecond = (int) (kuva.getAikaleima() / 1000);
        info.setImageTimestampEpochSecond(imageTimestampEpochSecond);

        for (int writeAttempts = MAX_IMG_WRITE_ATTEMPTS; writeAttempts > 0; writeAttempts--) {
            try {
                writeImage(image, imageFullPath, imageTimestampEpochSecond);
                lastWriteException = null;
                info.setWriteStatus(Status.SUCCESS);
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

        info.setWriteDurationMs(writeStart.getTime());

        if (info.getWriteStatus().isSuccess()) {
            log.debug("method=transferKuva phase=writeImage writeStatus={} presetId={} writerTookMs={} uploadFileName={} imageTimestamp={}",
                     Status.SUCCESS, presetId, writeStart.getTime(), imageFullPath, Instant.ofEpochSecond(imageTimestampEpochSecond));
        } else {
            log.error(String.format("method=transferKuva phase=writeImage writeStatus={} presetId=%s writerTookMs=%d retried %d times for %s, transfer aborted.",
                                    Status.FAILED, presetId, writeStart.getTime(), MAX_IMG_WRITE_ATTEMPTS, ToStringHelper.toString(kuva)),
                      lastWriteException);
        }
        log.debug("method=transferKuva presetId={} tookMs={}", presetId, start.getTime());

        return info;
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
        final StopWatch start = StopWatch.createStarted();
        try (final Session session = sftpSessionFactory.getSession()) {
            if (session.exists(imageFullPath) ) {
                log.info("method=deleteImage presetId={} imagePath={}", resolvePresetIdFromImageFullPath(imageFullPath), imageFullPath);
                session.remove(imageFullPath);
                return new DeleteInfo(true, true, start.getTime());
            }
            return new DeleteInfo(false, false, start.getTime());
        } catch (IOException e) {
            log.error(String.format("Failed to remove remote file deleteImageFileName=%s", imageFullPath), e);
            return new DeleteInfo(true, false, start.getTime());
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
        private final long durationMs;

        private DeleteInfo(final boolean fileExists, final boolean deleteSuccess, final long durationMs) {
            this.fileExists = fileExists;
            this.deleteSuccess = deleteSuccess;
            this.durationMs = durationMs;
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

        public boolean isSuccess() {
            return !isFileExists() || isDeleteSuccess();
        }

        public long getDurationMs() {
            return durationMs;
        }
    }

    private class ImageUpdateInfo {

        private long readDurationMs = 0;
        private long writeDurationMs = 0;
        private String downloadUrl;
        private String presetId;
        private String fullPath;
        private int sizeBytes = -1;
        private Status readStatus;
        private Status writeStatus = Status.NONE;
        private int imageTimestampEpochSecond;

        public void setReadDurationMs(final long readDurationMs) {
            this.readDurationMs = readDurationMs;
        }

        public long getReadDurationMs() {
            return readDurationMs;
        }

        public void setDownloadUrl(final String downloadUrl) {
            this.downloadUrl = downloadUrl;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public void setPresetId(final String presetId) {
            this.presetId = presetId;
        }

        public String getPresetId() {
            return presetId;
        }

        public void setFullPath(final String fullPath) {
            this.fullPath = fullPath;
        }

        public String getFullPath() {
            return fullPath;
        }

        public void setSizeBytes(final int sizeBytes) {
            this.sizeBytes = sizeBytes;
        }

        public int getSizeBytes() {
            return sizeBytes;
        }

        public void setWriteDurationMs(final long writeDurationMs) {
            this.writeDurationMs = writeDurationMs;
        }

        public long getWriteDurationMs() {
            return writeDurationMs;
        }

        public void setReadStatus(final Status readStatus) {
            this.readStatus = readStatus;
        }

        public Status getReadStatus() {
            return readStatus;
        }

        public void setWriteStatus(final Status writeStatus) {
            this.writeStatus = writeStatus;
        }

        public Status getWriteStatus() {
            return writeStatus;
        }

        public boolean isSuccess() {
            return getReadStatus().isSuccess() && getWriteStatus().isSuccess();
        }

        public long getDurationMs() {
            return getReadDurationMs() + getWriteDurationMs();
        }

        public void setImageTimestampEpochSecond(final int imageTimestampEpochSecond) {
            this.imageTimestampEpochSecond = imageTimestampEpochSecond;
        }

        public int getImageTimestampEpochSecond() {
            return imageTimestampEpochSecond;
        }
    }
}













