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

    static final int RETRY_COUNT = 6;

    private static final Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>() {{
        put(Error.class, true);
    }};

    @Autowired
    CameraImageUpdateService(
        @Value("${camera-image-uploader.retry.delay.ms}")
        final int retryDelayMs,
        final CameraPresetService cameraPresetService,
        final CameraImageReader imageReader,
        final CameraImageWriter imageWriter) {
        this.retryDelayMs = retryDelayMs;
        this.cameraPresetService = cameraPresetService;
        this.imageReader = imageReader;
        this.imageWriter = imageWriter;
    }

    public long deleteAllImagesForNonPublishablePresets() {
        // return count of succesful deletes
        return cameraPresetService.findAllNotPublishableCameraPresetsPresetIds().stream()
            .map(presetId -> imageWriter.deleteImage(getPresetImageName(presetId)))
            .filter(CameraImageWriter.DeleteInfo::isFileExistsAndDeleteSuccess)
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
                        "readTookMs={} writeTooksMs={} tookMs={} " +
                        "downloadImageUrl={} imageSizeBytes={} " +
                        "readErro={} writeError={}",
                    presetId, transferInfo.getFullPath(), transferInfo.getReadStatus(), transferInfo.getWriteStatus(),
                    transferInfo.getReadDurationMs(), transferInfo.getWriteDurationMs(), transferInfo.getDurationMs(),
                    transferInfo.getDownloadUrl(), transferInfo.getSizeBytes(),
                    transferInfo.getReadError(), transferInfo.getWriteError());
            }
            return transferInfo.isSuccess();
        } else {
            final CameraImageWriter.DeleteInfo deleteInfo = imageWriter.deleteImage(filename);
            log.info("method=handleKuva presetId={} deleteFileName={} fileExists={} deleteSuccess={} tookMs={}",
                presetId, deleteInfo.getFullPath(), deleteInfo.isFileExists(), deleteInfo.isDeleteSuccess(), deleteInfo.getDurationMs());
            return deleteInfo.isSuccess();
        }
    }

    private ImageUpdateInfo transferKuva(final KuvaProtos.Kuva kuva, final String presetId, final String filename) {
        final RetryTemplate retryTemplate = new RetryTemplate();
        final FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(retryDelayMs);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        final SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(RETRY_COUNT, retryableExceptions);
        retryTemplate.setRetryPolicy(retryPolicy);

        final ImageUpdateInfo info = new ImageUpdateInfo();
        info.setPresetId(presetId);
        info.setFullPath(imageWriter.getImageFullPath(filename));

        return retryTemplate.execute(args -> {
            final StopWatch start = StopWatch.createStarted();
            // Read the image
            byte[] image;
            try {
                image = imageReader.readImage(kuva, info);
                info.setSizeBytes(image.length);
                info.setReadStatusSuccess();
                // TODO count here all attempts to read including errors
                info.setReadDurationMs(start.getTime());
            } catch (final Exception e) {
                info.setReadStatusFailed(e);
                throw new Error(e);
            }
            if (image.length <= 0) {
                final Error e = new Error("Image was 0 bytes");
                info.setReadStatusFailed(e);
                throw e;
            }

            // Write the image
            final StopWatch writeStart = StopWatch.createStarted();
            try {
                imageWriter.writeImage(image, filename, (int) (kuva.getAikaleima() / 1000));
                info.setWriteStatusSuccess();
                // TODO count here all attempts to write including errors
                info.setWriteDurationMs(writeStart.getTime());
            } catch (final Exception e) {
                info.setWriteStatusFailed(e);
                throw new Error(e);
            }
            return info;
        }, args -> {
            // recover
            return info;
        });
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
}