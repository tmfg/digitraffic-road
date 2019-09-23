package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.conf.RoadWebApplicationConfiguration.WEATHERCAM_PATH;

import java.net.URI;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.data.service.CameraImageS3Writer;
import fi.livi.digitraffic.tie.metadata.model.CameraPresetHistory;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetHistoryService;

@RestController
@Validated
@RequestMapping(WEATHERCAM_PATH)
@ConditionalOnWebApplication
public class WeathercamController {

    private static final Logger log = LoggerFactory.getLogger(WeathercamController.class);

    private static final String VERSION_ID_PARAM = "versionId";
    private final String s3WeathercamBucketUrl;
    private final String s3WeathercamKeyRegexp;
    private final int historyMaxAgeHours;

    private CameraPresetHistoryService cameraPresetHistoryService;

    private enum HistoryStatus {
        PUBLIC,
        SECRET,
        NOT_FOUND,
        TOO_OLD,
        ILLEGAL_KEY
    }

    @Autowired
    public WeathercamController(final CameraPresetHistoryService cameraPresetHistoryService,
                                @Value("${dt.amazon.s3.weathercam.bucketName}") final String s3WeathercamBucketName,
                                @Value("${dt.amazon.s3.weathercam.region}") final String s3WeathercamRegion,
                                @Value("${dt.amazon.s3.weathercam.key.regexp}") final String s3WeathercamKeyRegexp,
                                @Value("${dt.amazon.s3.weathercam.history.maxAgeHours}") final int historyMaxAgeHours) {
        this.cameraPresetHistoryService = cameraPresetHistoryService;
        this.s3WeathercamKeyRegexp = s3WeathercamKeyRegexp;
        this.historyMaxAgeHours = historyMaxAgeHours;
        this.s3WeathercamBucketUrl = String.format("http://%s.s3-%s.amazonaws.com", s3WeathercamBucketName, s3WeathercamRegion);
    }

    @RequestMapping(method = RequestMethod.GET, path = "{imageName}")
    public ResponseEntity<Void>  imageVersion(
        @PathVariable final String imageName,
        @RequestParam(value=VERSION_ID_PARAM) final String versionId) {

        final HistoryStatus historyStatus = resolveHistoryStatus(imageName, versionId);
        log.info("method=imageVersion history of s3Key={} historyStatus={}", imageName, historyStatus);

        if ( !historyStatus.equals(HistoryStatus.PUBLIC) ) {
            return createNotFoundResponse();
        }

        final ResponseEntity<Void> response = ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(String.format("%s/%s?%s=%s", s3WeathercamBucketUrl, createImageVersionKey(getPresetId(imageName)), VERSION_ID_PARAM, versionId)))
            .build();

        log.info("method=imageVersion response={}", response);

        return response;
    }

    private HistoryStatus resolveHistoryStatus(final String imageName, final String versionId) {

        if (!imageName.matches(s3WeathercamKeyRegexp)) {
            return HistoryStatus.ILLEGAL_KEY;
        }
        // C1234567.jpg -> C1234567
        final CameraPresetHistory history = cameraPresetHistoryService.findHistory(getPresetId(imageName), versionId);
        final ZonedDateTime oldestLimit = ZonedDateTime.now().minusHours(historyMaxAgeHours);

        if (history == null) {
            return HistoryStatus.NOT_FOUND;
        } else if (!history.getPublishable()) {
            return HistoryStatus.SECRET;
        } else if (history.getLastModified().isBefore(oldestLimit)) {
            return HistoryStatus.TOO_OLD;
        }
        return HistoryStatus.PUBLIC;
    }

    private String getPresetId(final String imageName) {
        return imageName.substring(0,8);
    }

    private String createImageVersionKey(String presetId) {
        return presetId + CameraImageS3Writer.IMAGE_VERSION_KEY_SUFFIX;
    }

    private ResponseEntity<Void> createNotFoundResponse() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
