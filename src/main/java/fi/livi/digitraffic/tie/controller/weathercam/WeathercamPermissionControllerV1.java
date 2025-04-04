package fi.livi.digitraffic.tie.controller.weathercam;

import static fi.livi.digitraffic.tie.controller.weathercam.WeathercamPermissionControllerV1.WEATHERCAM_PATH;
import static fi.livi.digitraffic.tie.service.weathercam.CameraPresetHistoryDataService.HistoryStatus.PUBLIC;

import java.io.IOException;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.conf.amazon.WeathercamS3Properties;
import fi.livi.digitraffic.tie.service.weathercam.CameraImageThumbnailService;
import fi.livi.digitraffic.tie.service.weathercam.CameraPresetHistoryDataService;
import fi.livi.digitraffic.tie.service.weathercam.CameraPresetHistoryDataService.HistoryStatus;

@RestController
@Validated
@RequestMapping(WEATHERCAM_PATH)
@ConditionalOnWebApplication
public class WeathercamPermissionControllerV1 {

    private static final Logger log = LoggerFactory.getLogger(WeathercamPermissionControllerV1.class);

    public static final String WEATHERCAM_PATH = "/weathercam";
    private static final String VERSION_ID_PARAM = "versionId";
    private static final String THUMBNAIL_PARAM = "thumbnail";

    private final CameraPresetHistoryDataService cameraPresetHistoryDataService;
    private final WeathercamS3Properties weathercamS3Properties;
    private final CameraImageThumbnailService cameraImageThumbnailService;

    @Autowired
    public WeathercamPermissionControllerV1(final CameraPresetHistoryDataService cameraPresetHistoryDataService,
                                            final WeathercamS3Properties weathercamS3Properties,
                                            final CameraImageThumbnailService cameraImageThumbnailService) {
        this.cameraPresetHistoryDataService = cameraPresetHistoryDataService;
        this.weathercamS3Properties = weathercamS3Properties;
        this.cameraImageThumbnailService = cameraImageThumbnailService;
    }

    /**
     * This method handles requests for specific weathercam image versions as well as thumbnails of image versions AND thumbnails of current weathercam images.
     * If versionId has a value, the publicity of the requested image version is checked first.
     * If a current image is not public, the image file in the S3 bucket will be obscured and so will the resulting thumbnail.
     */
    @RequestMapping(method = RequestMethod.GET, path = "{imageName}")
    public ResponseEntity<?>  imageVersion(
        @PathVariable final String imageName,
        @RequestParam(value = VERSION_ID_PARAM, required = false) final String versionId,
        @RequestParam(value = THUMBNAIL_PARAM, required = false, defaultValue = "false") final boolean thumbnail) {

        if (versionId != null && versionId != "") {
            final HistoryStatus historyStatus =
                    cameraPresetHistoryDataService.resolveHistoryStatusForVersion(imageName, versionId);
            log.debug("method=imageVersion history of s3Key={} historyStatus={}", imageName, historyStatus);

            if (historyStatus != PUBLIC) {
                return createNotFoundResponse();
            }
        }

        if (thumbnail) {
            final StopWatch stopWatch = StopWatch.createStarted();
            try {
                final byte[] thumbnailBytes = cameraImageThumbnailService.generateCameraImageThumbnail(imageName, versionId);
                final HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_JPEG);
                log.info(
                        "method=imageVersion thumbnail generated for image {} tookMs={}", imageName,
                        stopWatch.getDuration().toMillis());
                return new ResponseEntity<>(thumbnailBytes, headers, HttpStatus.OK);
            } catch (final IOException e) {
                log.error("Error generating thumbnail for image {}", imageName, e);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

        }

        final ResponseEntity<Void> response = ResponseEntity.status(HttpStatus.FOUND)
            .location(weathercamS3Properties.getS3UriForVersion(imageName, versionId))
            .build();

        log.info("method=imageVersion response={}", response);

        return response;
    }

    private ResponseEntity<Void> createNotFoundResponse() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
