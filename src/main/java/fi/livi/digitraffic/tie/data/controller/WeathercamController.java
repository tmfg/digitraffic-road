package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.conf.RoadWebApplicationConfiguration.WEATHERCAM_PATH;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetHistoryService;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetHistoryService.HistoryStatus;

// TODO DPO-949 restore
//@RestController
@Validated
@RequestMapping(WEATHERCAM_PATH)
@ConditionalOnWebApplication
public class WeathercamController {

    private static final Logger log = LoggerFactory.getLogger(WeathercamController.class);

    private static final String VERSION_ID_PARAM = "versionId";

    private CameraPresetHistoryService cameraPresetHistoryService;

    @Autowired
    public WeathercamController(final CameraPresetHistoryService cameraPresetHistoryService) {
        this.cameraPresetHistoryService = cameraPresetHistoryService;
    }

    @RequestMapping(method = RequestMethod.GET, path = "{imageName}")
    public ResponseEntity<Void>  imageVersion(
        @PathVariable final String imageName,
        @RequestParam(value=VERSION_ID_PARAM) final String versionId) {

        final HistoryStatus historyStatus = cameraPresetHistoryService.resolveHistoryStatusForVersion(imageName, versionId);
        log.info("method=imageVersion history of s3Key={} historyStatus={}", imageName, historyStatus);

        if ( !historyStatus.equals(HistoryStatus.PUBLIC) ) {
            return createNotFoundResponse();
        }

        final ResponseEntity<Void> response = ResponseEntity.status(HttpStatus.FOUND)
            .location(cameraPresetHistoryService.createS3UriForVersion(imageName, versionId))
            .build();

        log.info("method=imageVersion response={}", response);

        return response;
    }

    private ResponseEntity<Void> createNotFoundResponse() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
