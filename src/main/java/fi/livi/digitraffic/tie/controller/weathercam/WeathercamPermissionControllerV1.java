package fi.livi.digitraffic.tie.controller.weathercam;

import fi.livi.digitraffic.tie.conf.amazon.WeathercamS3Properties;
import fi.livi.digitraffic.tie.service.aws.S3Service;
import fi.livi.digitraffic.tie.service.weathercam.CameraImageThumbnailService;
import fi.livi.digitraffic.tie.service.weathercam.CameraPresetHistoryDataService;
import fi.livi.digitraffic.tie.service.weathercam.CameraPresetHistoryDataService.HistoryStatus;
import fi.livi.digitraffic.tie.service.weathercam.ThumbnailGenerationError;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.RejectedExecutionException;

import static fi.livi.digitraffic.tie.controller.weathercam.WeathercamPermissionControllerV1.WEATHERCAM_PATH;
import static fi.livi.digitraffic.tie.service.weathercam.CameraPresetHistoryDataService.HistoryStatus.PUBLIC;
import static java.util.concurrent.CompletableFuture.completedFuture;

@RestController
@Validated
@RequestMapping(WEATHERCAM_PATH)
@ConditionalOnWebApplication
public class WeathercamPermissionControllerV1 {

    private static final Logger log = LoggerFactory.getLogger(WeathercamPermissionControllerV1.class);

    public static final String WEATHERCAM_PATH = "/weathercam";
    private static final String VERSION_ID_PARAM = "versionId";
    private static final String THUMBNAIL_PARAM = "thumbnail";
    private static final String IMAGE_NOT_AVAILABLE_IMG = "img/image_not_available.jpg";

    private final CameraPresetHistoryDataService cameraPresetHistoryDataService;
    private final WeathercamS3Properties weathercamS3Properties;
    private final CameraImageThumbnailService cameraImageThumbnailService;
    private final byte[] imageNotAvailable;

    @Autowired
    public WeathercamPermissionControllerV1(final CameraPresetHistoryDataService cameraPresetHistoryDataService,
                                            final WeathercamS3Properties weathercamS3Properties,
                                            final CameraImageThumbnailService cameraImageThumbnailService,
                                            final ResourceLoader resourceLoader) throws IOException {
        this.cameraPresetHistoryDataService = cameraPresetHistoryDataService;
        this.weathercamS3Properties = weathercamS3Properties;
        this.cameraImageThumbnailService = cameraImageThumbnailService;
        this.imageNotAvailable = readImageNotAvailableFromResource(resourceLoader);
    }

    private static byte[] readImageNotAvailableFromResource(final ResourceLoader resourceLoader) throws IOException {
        log.info("Read image from {}", IMAGE_NOT_AVAILABLE_IMG);
        final Resource resource = resourceLoader.getResource("classpath:" + IMAGE_NOT_AVAILABLE_IMG);
        try (final InputStream is = resource.getInputStream()) {
            return IOUtils.toByteArray(is);
        }
    }

    /**
     * This method handles requests for specific weathercam image versions as well as thumbnails of image versions AND thumbnails of current weathercam images.
     * If versionId has a value, the publicity of the requested image version is checked first.
     * If the current image is not public (deleted from S3), an "image not available" placeholder is returned.
     *
     * @param imageName The name of the weathercam image eg C1234501.jpg
     * @param versionId The S3 version id of the requested image version (optional if thumbnail=true)
     * @param thumbnail If true, a thumbnail of the current image, or its version is generated
     * @return Redirect to the image version at S3, a thumbnail, or an "image not available" placeholder.
     */
    @RequestMapping(method = RequestMethod.GET,
            path = "{imageName}")
    public CompletableFuture<ResponseEntity<?>> imageVersion(
            @PathVariable final String imageName,
            @RequestParam(value = VERSION_ID_PARAM,
                    required = false) final String versionId,
            @RequestParam(value = THUMBNAIL_PARAM,
                    required = false,
                    defaultValue = "false") final boolean thumbnail) {

        if (StringUtils.isNotBlank(versionId)) {
            final HistoryStatus historyStatus =
                    cameraPresetHistoryDataService.resolveHistoryStatusForVersion(imageName, versionId);
            log.debug("method=imageVersion history of s3Key={} historyStatus={}", imageName, historyStatus);

            if (historyStatus != PUBLIC) {
                log.info("method=imageVersion Returning image-not-available placeholder for image={} versionId={} historyStatus={}",
                        imageName, versionId, historyStatus);
                return completedFuture(imageNotAvailableResponse());
            }
        } else if (!thumbnail) {
            // If no versionId and not thumbnail, bad request
            return completedFuture(badRequestResponse());
        }

        if (thumbnail) {
            final StopWatch stopWatch = StopWatch.createStarted();

            return cameraImageThumbnailService
                    .generateCameraImageThumbnailAsync(imageName, versionId)
                    .handle((thumbnailBytes, ex) -> {

                        if (ex == null) {
                            log.info(
                                    "method=imageVersion thumbnail generated for image={} tookMs={}",
                                    imageName,
                                    stopWatch.getDuration().toMillis()
                            );
                            return ResponseEntity.ok()
                                    .contentType(MediaType.IMAGE_JPEG)
                                    .body(thumbnailBytes);
                        }

                        final Throwable cause =
                                ex instanceof CompletionException ? ex.getCause() : ex;
                        if (cause instanceof final Error err) {
                            log.error(
                                    "method=imageVersion Unexpected error when generating thumbnail for image={} versionId={}",
                                    imageName, versionId, err
                            );
                            return internalServerErrorResponse();
                        } else if (cause instanceof NoSuchKeyException) {
                            log.info("method=imageVersion Image not found in S3, returning image-not-available placeholder image={} versionId={}", imageName, versionId);
                            return imageNotAvailableResponse();
                        } else if (cause instanceof final ThumbnailGenerationError e) {
                            log.error(
                                    "method=imageVersion Thumbnail generation failed for imageName={} versionId={} lastModified={} size={} hash={}",
                                    e.getImageName(), e.getVersionId(),
                                    e.getLastModified(), e.getOriginalImageSize(),
                                    e.getOriginalImageHash(), e
                            );
                            return internalServerErrorResponse();
                        } else if (cause instanceof S3Service.S3RateLimitExceededException) {
                            log.error("method=imageVersion S3 requests overloaded image={} versionId={}", imageName, versionId, cause);
                            return tooManyRequestsErrorResponse();
                        } else if (cause instanceof RejectedExecutionException ) {
                            log.error("method=imageVersion Thumbnail executor overloaded image={} versionId={}", imageName, versionId, cause);
                            return tooManyRequestsErrorResponse();
                        }

                        log.error(
                                "method=imageVersion Unexpected error when generating thumbnail for image={} versionId={}",
                                imageName, versionId, cause
                        );
                        return internalServerErrorResponse();
                    });
        }

        return completedFuture(ResponseEntity.status(HttpStatus.FOUND)
                .location(weathercamS3Properties.getS3UriForVersion(imageName, versionId))
                .<Void>build());
    }


    private static ResponseEntity<?> badRequestResponse() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    private static ResponseEntity<?> internalServerErrorResponse() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    private ResponseEntity<?> imageNotAvailableResponse() {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .cacheControl(CacheControl.noCache())
                .body(imageNotAvailable);
    }

    private ResponseEntity<?> tooManyRequestsErrorResponse() {
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .build();
    }
}
