package fi.livi.digitraffic.tie.service.weathercam;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import fi.ely.lotju.kamera.proto.KuvaProtos;
import fi.livi.digitraffic.common.util.TimeUtil;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.roadstation.RoadStation;
import fi.livi.digitraffic.tie.model.weathercam.CameraPreset;
import fi.livi.digitraffic.tie.service.ImageManipulationService;

@ConditionalOnNotWebApplication
@Component
public class CameraImageUpdateHandler {
    private static final Logger log = LoggerFactory.getLogger(CameraImageUpdateHandler.class);

    private final int retryDelayMs;
    private final CameraPresetService cameraPresetService;
    private final CameraImageReader imageReader;
    private final CameraImageS3Writer cameraImageS3Writer;
    private final byte[] noiseImage;
    private final ResourceLoader resourceLoader;

    public static final int RETRY_COUNT = 3;
    private static final String NOISE_IMG = "img/noise.jpg";

    private static final Map<Class<? extends Throwable>, Boolean> retryableExceptions = Map.of(
        CameraImageReadFailureException.class, true,
        CameraImageWriteFailureException.class, true);

    @Autowired
    CameraImageUpdateHandler(
        @Value("${camera-image-uploader.retry.delay.ms}")
        final int retryDelayMs,
        final CameraPresetService cameraPresetService,
        final CameraImageReader imageReader,
        final CameraImageS3Writer cameraImageS3Writer,
        final ResourceLoader resourceLoader) throws IOException {
        this.retryDelayMs = retryDelayMs;
        this.cameraPresetService = cameraPresetService;
        this.imageReader = imageReader;
        this.cameraImageS3Writer = cameraImageS3Writer;
        this.resourceLoader = resourceLoader;
        this.noiseImage = readEmptyImageFromResource();
    }

    private byte[] readEmptyImageFromResource() throws IOException {
        log.info("Read image from {}", CameraImageUpdateHandler.NOISE_IMG);
        final Resource resource = resourceLoader.getResource("classpath:" + CameraImageUpdateHandler.NOISE_IMG);
        final InputStream imageIs = resource.getInputStream();
        return IOUtils.toByteArray(imageIs);
    }

    public boolean handleKuva(final KuvaProtos.Kuva kuva) {
        if (log.isDebugEnabled()) {
            log.debug("method=handleKuva Handling {}", ToStringHelper.toString(kuva));
        }

        final CameraPreset cameraPreset = cameraPresetService.findCameraPresetByLotjuId(kuva.getEsiasentoId());

        final String presetId = resolvePresetIdFrom(cameraPreset, kuva);
        final String imageKey = getPresetImageKey(presetId);

        // If preset exists in db, update image
        if (cameraPreset != null) {
            final boolean roadStationPublic = cameraPreset.getRoadStation().isPublicNow();
            final boolean isResultPublic = kuva.getJulkinen() && roadStationPublic;
            final ImageUpdateInfo transferInfo = transferKuva(kuva, presetId, imageKey, isResultPublic);

            cameraPresetService.updateCameraPresetAndHistoryWithLotjuId(kuva.getEsiasentoId(), isResultPublic,
                kuva.getJulkinen(), transferInfo);

            if (transferInfo.isSuccess()) {
                if (log.isDebugEnabled()) {
                    log.debug("method=handleKuva presetId=\"{}\" s3Key=\"{}\" readImageStatus={} writeImageStatus={} " +
                            "readTookMs={} writeTooksMs={} tookMs={} " +
                            "downloadImageUrl={} sizeBytes={} " +
                            "s3VersionId=\"{}\" imageTimestamp={} imageTimeInPastSeconds={}",
                        presetId, imageKey, transferInfo.getReadStatus(), transferInfo.getWriteStatus(),
                        transferInfo.getReadDurationMs(), transferInfo.getWriteDurationMs(),
                        transferInfo.getDurationMs(), transferInfo.getDownloadUrl(), transferInfo.getSizeBytes(),
                        transferInfo.getVersionId(),
                        transferInfo.getLastUpdated(), transferInfo.getImageTimeInPastSeconds());
                }
            } else {
                log.error("method=handleKuva presetId=\"{}\" s3Key=\"{}\" readImageStatus={} writeImageStatus={} " +
                        "readTookMs={} readTotalTookMs={} writeTooksMs={} writeTotalTookMs={} tookMs={} " +
                        "downloadImageUrl={} sizeBytes={} error={}",
                    presetId, imageKey, transferInfo.getReadStatus(), transferInfo.getWriteStatus(),
                    transferInfo.getReadDurationMs(), transferInfo.getReadTotalDurationMs(),
                    transferInfo.getWriteDurationMs(), transferInfo.getWriteTotalDurationMs(),
                    transferInfo.getDurationMs(),
                    transferInfo.getDownloadUrl(), transferInfo.getSizeBytes(),
                    getErrorMessage(transferInfo),
                    transferInfo.getReadError() != null ? transferInfo.getReadError() : transferInfo.getWriteError());
            }
            return transferInfo.isSuccess();
        } else {
            // Preset doesn't exist, so we delete image
            final CameraImageS3Writer.DeleteInfo deleteInfoS3 = cameraImageS3Writer.deleteImage(imageKey);
            log.info(
                "method=handleKuva preset does not exists presetId=\"{}\" deleteFileS3Key=\"{}\" fileExists={} deleteSuccess={} tookMs={}",
                presetId, deleteInfoS3.getKey(), deleteInfoS3.isFileExists(), deleteInfoS3.isDeleteSuccess(),
                deleteInfoS3.getDurationMs());
            return deleteInfoS3.isSuccess();
        }
    }

    private String getErrorMessage(final ImageUpdateInfo transferInfo) {
        return transferInfo.getReadError() != null ? transferInfo.getReadError().getMessage() :
               transferInfo.getWriteError() != null ? transferInfo.getWriteError().getMessage() : "Unknown error";
    }

    private ImageUpdateInfo transferKuva(final KuvaProtos.Kuva kuva, final String presetId, final String filename,
                                         final boolean isPublic) {
        final ImageUpdateInfo info =
            new ImageUpdateInfo(presetId, TimeUtil.toInstant(kuva.getAikaleima()));
        try {
            byte[] image = readKuva(kuva.getKuvaId(), info);
            try {
                image = ImageManipulationService.removeJpgExifMetadata(image);
            } catch (final Exception e) {
                // Let's use original
                log.warn(
                    "Failed to remove Exif metadata from image with presetId={}, using original image. Error message: {}",
                    presetId, e.getMessage());
            }
            writeKuva(image, kuva.getAikaleima(), filename, info, isPublic);
        } catch (final CameraImageReadFailureException | CameraImageWriteFailureException e) {
            // read/write attempts exhausted
        }
        return info;
    }

    private byte[] readKuva(final long kuvaId, final ImageUpdateInfo info) {
        final RetryTemplate retryTemplate = getRetryTemplate();
        return retryTemplate.execute(retryContext -> {
            final StopWatch start = StopWatch.createStarted();
            // Read the image
            byte[] image;
            try {
                image = imageReader.readImage(kuvaId, info);
            } catch (final Exception e) {
                info.updateReadStatusFailed(e);
                throw new CameraImageReadFailureException(e);
            } finally {
                info.updateReadTotalDurationMs(start.getDuration().toMillis());
            }
            if (image.length <= 0) {
                final CameraImageReadFailureException e =
                    new CameraImageReadFailureException(String.format("Image was %d bytes", image.length));
                info.updateReadStatusFailed(e);
                throw e;
            }
            info.updateReadStatusSuccess();
            return image;
        });
    }

    private void writeKuva(final byte[] realImage, final long timestampEpochMillis, final String filename,
                           final ImageUpdateInfo info, final boolean isPublic) {
        final RetryTemplate retryTemplate = getRetryTemplate();
        retryTemplate.execute(retryContext -> {
            final StopWatch writeStart = StopWatch.createStarted();
            try {
                final byte[] currentImageToWrite = isPublic ? realImage : noiseImage;

                final String versionId = cameraImageS3Writer.writeImage(currentImageToWrite, realImage,
                    filename, timestampEpochMillis);
                info.setVersionId(versionId);
                info.updateWriteStatusSuccess();
                info.setWriteDurationMs(writeStart.getDuration().toMillis());

            } catch (final Exception e) {
                info.updateWriteStatusFailed(e);
                throw new CameraImageWriteFailureException(e);
            } finally {
                info.updateWriteTotalDurationMs(writeStart.getDuration().toMillis());
            }
            return null;
        });
    }

    public void hideCurrentImagesForCamera(final RoadStation rs) {
        final Map<Long, CameraPreset> presets =
            cameraPresetService.findAllCameraPresetsByCameraLotjuIdMappedByPresetLotjuId(rs.getLotjuId());
        presets.values().forEach(this::hideCurrentImageForPreset);
    }

    public void hideCurrentImageForPreset(final CameraPreset preset) {
        final String imageKey = getPresetImageKey(preset.getPresetId());
        cameraImageS3Writer.writeCurrentImage(noiseImage, imageKey, Instant.now().toEpochMilli());
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

    public static String resolvePresetIdFrom(final KuvaProtos.Kuva kuva) {
        return resolvePresetIdFrom(null, kuva);
    }

    private static String resolvePresetIdFrom(final CameraPreset cameraPreset, final KuvaProtos.Kuva kuva) {
        return cameraPreset != null ? cameraPreset.getPresetId() : kuva.getNimi().substring(0, 8);
    }

    private static String getPresetImageKey(final String presetId) {
        return presetId + ".jpg";
    }

    static class CameraImageReadFailureException extends RuntimeException {

        CameraImageReadFailureException(final String message) {
            super(message);
        }

        CameraImageReadFailureException(final Throwable cause) {
            super(cause);
        }
    }

    static class CameraImageWriteFailureException extends RuntimeException {

        CameraImageWriteFailureException(final Throwable cause) {
            super(cause);
        }

    }
}
