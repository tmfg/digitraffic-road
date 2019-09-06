package fi.livi.digitraffic.tie.data.service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.ely.lotju.kamera.proto.KuvaProtos;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;

@ConditionalOnNotWebApplication
@Service
public class CameraImageUpdateService {
    private static final Logger log = LoggerFactory.getLogger(CameraImageUpdateService.class);

    private final int retryDelayMs;
    private final CameraPresetService cameraPresetService;
    private final CameraImageReader imageReader;
    private final CameraImageWriter imageWriter;
    private final CameraImageS3Writer cameraImageS3Writer;

    static final int RETRY_COUNT = 3;

    private static final Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>() {{
        put(CameraImageReadFailureException.class, true);
        put(CameraImageWriteFailureException.class, true);
    }};

    @Autowired
    CameraImageUpdateService(
        @Value("${camera-image-uploader.retry.delay.ms}")
        final int retryDelayMs,
        final CameraPresetService cameraPresetService,
        final CameraImageReader imageReader,
        final CameraImageWriter imageWriter,
        final CameraImageS3Writer cameraImageS3Writer) {
        this.retryDelayMs = retryDelayMs;
        this.cameraPresetService = cameraPresetService;
        this.imageReader = imageReader;
        this.imageWriter = imageWriter;
        this.cameraImageS3Writer = cameraImageS3Writer;
    }

    public long deleteAllImagesForNonPublishablePresets() {
        // return count of succesful deletes
        cameraPresetService.findAllNotPublishableCameraPresetsPresetIds().stream()
            .map(presetId -> imageWriter.deleteImage(getPresetImageName(presetId)))
            .filter(CameraImageWriter.DeleteInfo::isFileExistsAndDeleteSuccess)
            .count();

        return cameraPresetService.findAllNotPublishableCameraPresetsPresetIds().stream()
            .map(presetId -> cameraImageS3Writer.deleteImage(getPresetImageName(presetId)))
            .filter(CameraImageS3Writer.DeleteInfo::isFileExistsAndDeleteSuccess)
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

        if (cameraPreset != null) {
            final ImageUpdateInfo transferInfo = transferKuva(kuva, presetId, filename);
            updateCameraPreset(cameraPreset, kuva, transferInfo.isSuccess());

            if (transferInfo.isSuccess()) {
                log.info("method=handleKuva presetId={} uploadFileName={} readImageStatus={} writeImageStatus={} " +
                        "readTookMs={} writeTooksMs={} tookMs={} " +
                        "downloadImageUrl={} imageSizeBytes={}",
                    presetId, transferInfo.getFullPath(), transferInfo.getReadStatus(), transferInfo.getWriteStatus(),
                    transferInfo.getReadDurationMs(), transferInfo.getWriteDurationMs(), transferInfo.getDurationMs(),
                    transferInfo.getDownloadUrl(), transferInfo.getSizeBytes());
            } else {
                log.error("method=handleKuva presetId={} uploadFileName={} readImageStatus={} writeImageStatus={} " +
                        "readTookMs={} readTotalTookMs={} " +
                        "writeTooksMs={} writeTotalTookMs={} tookMs={} " +
                        "downloadImageUrl={} imageSizeBytes={} " +
                        "readErro={} writeError={}",
                    presetId, transferInfo.getFullPath(), transferInfo.getReadStatus(), transferInfo.getWriteStatus(),
                    transferInfo.getReadDurationMs(), transferInfo.getReadTotalDurationMs(),
                    transferInfo.getWriteDurationMs(), transferInfo.getWriteTotalDurationMs(),
                    transferInfo.getDurationMs(),
                    transferInfo.getDownloadUrl(), transferInfo.getSizeBytes(),
                    transferInfo.getReadError(), transferInfo.getWriteError());
            }
            return transferInfo.isSuccess();
        } else {
            final CameraImageWriter.DeleteInfo deleteInfo = imageWriter.deleteImage(filename);
            final CameraImageS3Writer.DeleteInfo deleteInfoS3 = cameraImageS3Writer.deleteImage(filename);
            log.info("method=handleKuva presetId={} deleteFileName={} fileExists={} deleteSuccess={} tookMs={}",
                presetId, deleteInfo.getFullPath(), deleteInfo.isFileExists(), deleteInfo.isDeleteSuccess(), deleteInfo.getDurationMs());
            log.info("method=handleKuva presetId={} deleteFileName={} fileExists={} deleteSuccess={} tookMs={}",
                presetId, deleteInfo.getFullPath(), deleteInfoS3.isFileExists(), deleteInfoS3.isDeleteSuccess(), deleteInfoS3.getDurationMs());
            return deleteInfo.isSuccess();
        }
    }

    private ImageUpdateInfo transferKuva(final KuvaProtos.Kuva kuva, final String presetId, final String filename) {
        final ImageUpdateInfo info = new ImageUpdateInfo(presetId, imageWriter.getImageFullPath(filename));
        try {
            byte[] image = readKuva(kuva, info);
            writeKuva(image, kuva, filename, info);
        } catch (CameraImageReadFailureException e) {
            // read attempts exhausted
        } catch (CameraImageWriteFailureException e) {
            // write attempts exhausted
        }
        return info;
    }

    private byte[] readKuva(KuvaProtos.Kuva kuva, final ImageUpdateInfo info) {
        final RetryTemplate retryTemplate = getRetryTemplate();
        return retryTemplate.execute(retryContext -> {
            final StopWatch start = StopWatch.createStarted();
            // Read the image
            byte[] image;
            try {
                image = imageReader.readImage(kuva, info);
                info.setSizeBytes(image.length);
                info.updateReadStatusSuccess();
                final long readEnd = start.getTime();
                info.updateReadTotalDurationMs(readEnd);
                info.setReadDurationMs(readEnd);
            } catch (final Exception e) {
                info.updateReadStatusFailed(e);
                throw new CameraImageReadFailureException(e);
            }
            if (image.length <= 0) {
                final CameraImageReadFailureException e = new CameraImageReadFailureException("Image was 0 bytes");
                info.updateReadStatusFailed(e);
                throw e;
            }
            return image;
        });
    }

    private void writeKuva(byte[] image, KuvaProtos.Kuva kuva, String filename, ImageUpdateInfo info) {
        final RetryTemplate retryTemplate = getRetryTemplate();
        retryTemplate.execute(retryContext -> {
            final StopWatch writeStart = StopWatch.createStarted();
            try {
                imageWriter.writeImage(image, filename, (int) (kuva.getAikaleima() / 1000));
                cameraImageS3Writer.writeImage(image, filename, (int) (kuva.getAikaleima() / 1000));
                info.updateWriteStatusSuccess();
                final long writeEnd = writeStart.getTime();
                info.updateWriteTotalDurationMs(writeEnd);
                info.setWriteDurationMs(writeEnd);
            } catch (final Exception e) {
                info.updateWriteStatusFailed(e);
                throw new CameraImageWriteFailureException(e);
            }
            return null;
        });
    }

    private RetryTemplate getRetryTemplate() {
        final RetryTemplate retryTemplate = new RetryTemplate();
        final FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(retryDelayMs);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        final SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(RETRY_COUNT, retryableExceptions);
        retryTemplate.setRetryPolicy(retryPolicy);
        return retryTemplate;
    }

    private static void updateCameraPreset(final CameraPreset cameraPreset, final KuvaProtos.Kuva kuva, final boolean success) {
        final ZonedDateTime lastModified = DateHelper.toZonedDateTimeAtUtc(Instant.ofEpochMilli(kuva.getAikaleima()));
        if (cameraPreset.isPublicExternal() != kuva.getJulkinen()) {
            cameraPreset.setPublicExternal(kuva.getJulkinen());
            cameraPreset.setPictureLastModified(lastModified);
            log.info("method=updateCameraPreset cameraPresetId={} isPublicExternal from {} to {} lastModified={}",
                cameraPreset.getPresetId(), !kuva.getJulkinen(), kuva.getJulkinen(), lastModified);
        } else if (success) {
            cameraPreset.setPictureLastModified(lastModified);
        }
    }

    static String resolvePresetIdFrom(final CameraPreset cameraPreset, final KuvaProtos.Kuva kuva) {
        return cameraPreset != null ? cameraPreset.getPresetId() : kuva.getNimi().substring(0, 8);
    }

    private static String getPresetImageName(final String presetId) {
        return presetId + ".jpg";
    }

    static class CameraImageReadFailureException extends RuntimeException {

        CameraImageReadFailureException(String message) {
            super(message);
        }

        CameraImageReadFailureException(Throwable cause) {
            super(cause);
        }
    }

    static class CameraImageWriteFailureException extends RuntimeException {

        CameraImageWriteFailureException(Throwable cause) {
            super(cause);
        }

    }
}