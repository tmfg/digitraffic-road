package fi.livi.digitraffic.tie.controller.weathercam;

import static fi.livi.digitraffic.tie.controller.weathercam.WeathercamPermissionControllerV1.WEATHERCAM_PATH;
import static fi.livi.digitraffic.tie.service.v1.camera.CameraPresetHistoryDataService.HistoryStatus.PUBLIC;

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

import fi.livi.digitraffic.tie.conf.amazon.WeathercamS3Properties;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetHistoryDataService;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetHistoryDataService.HistoryStatus;

@RestController
@Validated
@RequestMapping(WEATHERCAM_PATH)
@ConditionalOnWebApplication
public class WeathercamPermissionControllerV1 {

    private static final Logger log = LoggerFactory.getLogger(WeathercamPermissionControllerV1.class);

    public static final String WEATHERCAM_PATH = "/weathercam";
    private static final String VERSION_ID_PARAM = "versionId";

    private final CameraPresetHistoryDataService cameraPresetHistoryDataService;
    private final WeathercamS3Properties weathercamS3Properties;

    @Autowired
    public WeathercamPermissionControllerV1(final CameraPresetHistoryDataService cameraPresetHistoryDataService,
                                            final WeathercamS3Properties weathercamS3Properties) {
        this.cameraPresetHistoryDataService = cameraPresetHistoryDataService;
        this.weathercamS3Properties = weathercamS3Properties;
    }

    @RequestMapping(method = RequestMethod.GET, path = "{imageName}")
    public ResponseEntity<Void>  imageVersion(
        @PathVariable final String imageName,
        @RequestParam(value = VERSION_ID_PARAM) final String versionId) {

        final HistoryStatus historyStatus = cameraPresetHistoryDataService.resolveHistoryStatusForVersion(imageName, versionId);
        log.info("method=imageVersion history of s3Key={} historyStatus={}", imageName, historyStatus);

        if ( historyStatus != PUBLIC ) {
            return createNotFoundResponse();
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
