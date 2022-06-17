package fi.livi.digitraffic.tie.controller.weathercam;

import static fi.livi.digitraffic.tie.controller.ApiConstants.API_WEATHERCAM;
import static fi.livi.digitraffic.tie.controller.ApiConstants.BETA;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_GEO_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_VND_GEO_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_OK;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.controller.ApiConstants;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamFeatureCollectionV1;
import fi.livi.digitraffic.tie.service.v1.camera.CameraDataService;
import fi.livi.digitraffic.tie.service.weathercam.v1.WeathercamWebServiceV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = ApiConstants.WEATHERCAM_TAG, description = "Weathercam Controller (BETA)")
@RestController
@Validated
@ConditionalOnWebApplication
public class WeathercamController {

    /**
     * API paths:
     *
     * Metadata
     * /api/weathercam/v/stations
     * /api/weathercam/v/stations/{id}
     *
     * Data
     * /api/weathercam/v/stations/data
     * /api/weathercam/v/stations/{id}/data
     *
     * /api/weathercam/v/stations/histories
     * /api/weathercam/v/stations/histories/changes
     * /api/weathercam/v/stations/presences
     *
     * These was from old
     * /api/weathercam/v/stations (= metadata) – OK
     * /api/weathercam/v/stations/{cameraId} (= metadata)
     * /api/weathercam/v/stations/measurements (= data) – OK
     * /api/weathercam/v/stations/measurements/{presetId} – OK
     * /api/weathercam/v/stations/{cameraId}/measurements (= aseman data)
     * /api/weathercam/v/stations/{cameraId}/measurements/{presetId}
     *
     *
     */

//    private static final String API_WEATHERCAM_V1 = API_WEATHERCAM + V1;
    private static final String API_WEATHERCAM_BETA = API_WEATHERCAM + BETA;

    private static final String STATIONS = "/stations";
    private static final String DATA = "/data";

    private static final String API_WEATHERCAM_V1_STATIONS = API_WEATHERCAM_BETA + STATIONS;

//    public static final String API_WEATHERCAM_V1_STATIONS_MEASUREMENTS = API_WEATHERCAM_V1_STATIONS + "/measurements";
//    public static final String API_WEATHERCAM_V1_STATIONS_HISTORIES = API_WEATHERCAM_V1_STATIONS + "/histories";
//    public static final String API_WEATHERCAM_V1_STATIONS_HISTORIES_CHANGES = API_WEATHERCAM_V1_STATIONS_HISTORIES + "/changes";
//    public static final String API_WEATHERCAM_V1_STATIONS_PRESENCES = API_WEATHERCAM_V1_STATIONS + "/presences";


    private final WeathercamWebServiceV1 weathercamWebServiceV1;
    private CameraDataService cameraDataService;

    @Autowired
    public WeathercamController(final WeathercamWebServiceV1 weathercamWebServiceV1,
                                final CameraDataService cameraDataService) {
        this.weathercamWebServiceV1 = weathercamWebServiceV1;
        this.cameraDataService = cameraDataService;
    }

    @Operation(summary = "The static information of weather camera stations")
    @RequestMapping(method = RequestMethod.GET, path = API_WEATHERCAM_V1_STATIONS,
                    produces = { APPLICATION_JSON_VALUE, APPLICATION_GEO_JSON_VALUE, APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Camera Preset Feature Collections") })
    public WeathercamFeatureCollectionV1 weathercamStations(
        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
        final boolean lastUpdated) {
        return weathercamWebServiceV1.findAllPublishableCameraStationsAsFeatureCollection(lastUpdated);
    }
//
//    @Operation(summary = "The static information of weather camera station")
//    @RequestMapping(method = RequestMethod.GET, path = API_WEATHERCAM_V1_STATIONS + "/{stationId}",
//                    produces = { APPLICATION_JSON_VALUE, APPLICATION_GEO_JSON_VALUE, APPLICATION_VND_GEO_JSON_VALUE })
//    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Camera Preset Feature Collections") })
//    public CameraStationFeature weathercamStationsByStationId(
//        @Parameter(description = "Camera station id", required = true)
//        @PathVariable
//        final String stationId) {
//        return cameraWebService.findPublishableCameraStationAsFeature(stationId);
//    }
//
//    @Operation(summary = "Current data of cameras")
//    @RequestMapping(method = RequestMethod.GET, path = API_WEATHERCAM_V1_STATIONS_MEASUREMENTS, produces = APPLICATION_JSON_VALUE)
//    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of camera station data"))
//    public CameraRootDataObjectDto weathercamData(
//        @Parameter(description = "If parameter is given result will only contain update status.")
//        @RequestParam(value=LAST_UPDATED_PARAM, required = false, defaultValue = "false")
//        final boolean lastUpdated) {
//        return cameraDataService.findPublishableCameraStationsData(lastUpdated);
//    }
//
//    @Operation(summary = "Current data of camera preset")
//    @RequestMapping(method = RequestMethod.GET, path = API_WEATHERCAM_V1_STATIONS + "/{stationId}" + DATA, produces = APPLICATION_JSON_VALUE)
//    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of camera station data"))
//    public CameraRootDataObjectDto weathercamDataByStationId(
//        @Parameter(description = "Camera station id", required = true)
//        @PathVariable
//        final String stationId) {
//        return cameraDataService.findPublishableCameraStationsData(stationId);
//    }


    // TODO
//    @Operation(summary = "Current data of camera preset")
//    @RequestMapping(method = RequestMethod.GET, path = API_WEATHERCAM_V1_STATIONS_MEASUREMENTS + "/{presetId}", produces = APPLICATION_JSON_VALUE)
//    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of camera station data"))
//    public CameraRootDataObjectDto cameraDataById(
//        @Parameter(description = "Camera id", required = true)
//        @PathVariable
//        final String presetId) {
//        return cameraDataService.findPublishableCameraStationsDataByPresetId(presetId);
//    }
}