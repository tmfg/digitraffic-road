package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.conf.RoadWebApplicationConfiguration.WEATHERCAM_PATH;

import java.net.URI;

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
    private String s3WeathercamKeyRegexp;

    private CameraPresetHistoryService cameraPresetHistoryService;

    @Autowired
    public WeathercamController(final CameraPresetHistoryService cameraPresetHistoryService,
                                @Value("${dt.amazon.s3.weathercamBucketName}") final String s3WeathercamBucketName,
                                @Value("${dt.amazon.s3.weathercamRegion}") final String s3WeathercamRegion,
                                @Value("${dt.amazon.s3.weathercamKey.regexp}") final String s3WeathercamKeyRegexp) {
        this.cameraPresetHistoryService = cameraPresetHistoryService;
        this.s3WeathercamKeyRegexp = s3WeathercamKeyRegexp;
        this.s3WeathercamBucketUrl = String.format("http://%s.s3-%s.amazonaws.com", s3WeathercamBucketName, s3WeathercamRegion);
    }

    @RequestMapping(method = RequestMethod.GET, path = "{imageName}")
    public ResponseEntity<Void>  imageVersion(
        @PathVariable final String imageName,
        @RequestParam(value=VERSION_ID_PARAM) final String versionId) {

        log.info("method=imageVersion imageName={} versionId={}", imageName, versionId);
        if (!imageName.matches(s3WeathercamKeyRegexp)) {
            log.warn("metdhod=imageVersion S3 key should match regexp format \"{}}\" ie. \"C1234567.jpg\" but was \"{}\"", s3WeathercamKeyRegexp, imageName);
            createNotFoundResponse();
        }
        // C1234567.jpg -> C1234567
        final String presetId  = imageName.substring(0,8);
        final CameraPresetHistory history = cameraPresetHistoryService.findHistory(presetId, versionId);
        if (history == null || !history.getPublishable()) {
            log.info("method=imageVersion history of s3Key={} notFoundReason={}", imageName, history != null ? "SECRET" : "NOT_FOUND");
            return createNotFoundResponse();
        }

        final ResponseEntity<Void> response = ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(String.format("%s/%s?%s=%s", s3WeathercamBucketUrl, createImageVersionKey(presetId), VERSION_ID_PARAM, versionId)))
            .build();

        log.info("method=imageVersion response={}", response);

        return response;
    }

    private String createImageVersionKey(String presetId) {
        return presetId + CameraImageS3Writer.IMAGE_VERSION_KEY_SUFFIX;
    }

    private ResponseEntity<Void> createNotFoundResponse() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
