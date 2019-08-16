package fi.livi.digitraffic.tie.data.service;

import java.time.Instant;
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
            success = imageWriter.deleteKuva(kuva, presetId, filename);
        }

        log.info("method=handleKuva {} for {} presetId={} tookMs={} {}",
            success ? "success" : "failed",
            cameraPreset != null ? "transferKuva" : "deleteKuva",
            presetId,
            start.getTime(),
            ToStringHelper.toString(kuva));
        return success;
    }

    private boolean transferKuva(KuvaProtos.Kuva kuva, String presetId, String filename) {
        final RetryTemplate retryTemplate = new RetryTemplate();
        final FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(retryDelayMs);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        final SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(RETRY_COUNT, retryableExceptions);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate.execute(args -> {
            final StopWatch start = StopWatch.createStarted();
            // Read the image
            byte[] image;
            try {
                image = imageReader.readImage(kuva, filename);
            } catch (final Exception e) {
                log.warn(
                    "method=transferKuva Reading image for presetId={} from srcUri={} to sftpServerPath={} failed. exceptionMessage={} .",
                    presetId, kuva.getKuvaId(), filename, e.getMessage());
                throw new Error(e);
            }
            if (image.length == 0) {
                log.warn("method=transferKuva Reading image for presetId={} from srcUri={} to sftpServerPath={} returned 0 bytes.",
                    presetId, kuva.getKuvaId(), filename);
                throw new Error("image was 0 bytes");
            }
            log.info("method=transferKuva readTookMs={}", start.getTime());

            // Write the image
            final StopWatch writeStart = StopWatch.createStarted();
            try {
                imageWriter.writeImage(image, filename, (int) (kuva.getAikaleima() / 1000));
            } catch (final Exception e) {
                log.warn(
                    "method=transferKuva Writing image for presetId={} from srcUri={} to sftpServerPath={} failed. exceptionMessage={}.",
                    presetId, kuva.getKuvaId(), filename, e.getMessage());
                throw new Error(e);
            }
            log.info("method=transferKuva presetId={} writerTookMs={}", presetId, writeStart.getTime());
            log.info("method=transferKuva presetId={} tookMs={}", presetId, start.getTime());
            return true;
        }, args -> {
            // recover
            return false;
        });
    }

    private static void updateCameraPreset(final CameraPreset cameraPreset, final KuvaProtos.Kuva kuva, final boolean success) {
        if (cameraPreset.isPublicExternal() != kuva.getJulkinen()) {
            cameraPreset.setPublicExternal(kuva.getJulkinen());
            cameraPreset.setPictureLastModified(DateHelper.toZonedDateTimeAtUtc(Instant.ofEpochMilli(kuva.getAikaleima())));
            log.info("method=updateCameraPreset cameraPresetId={} isPublicExternal from {} to {} ", cameraPreset.getPresetId(), !kuva.getJulkinen(),
                kuva.getJulkinen());
        } else if (success) {
            cameraPreset.setPictureLastModified(DateHelper.toZonedDateTimeAtUtc(Instant.ofEpochMilli(kuva.getAikaleima())));
        }
    }

    static String resolvePresetIdFrom(final CameraPreset cameraPreset, final KuvaProtos.Kuva kuva) {
        return cameraPreset != null ? cameraPreset.getPresetId() : kuva.getNimi().substring(0, 8);
    }

    private static String getPresetImageName(final String presetId) {
        return presetId + ".jpg";
    }

}













