package fi.livi.digitraffic.tie.controller.weathercam;

import static fi.livi.digitraffic.tie.controller.ApiConstants.API_WEATHERCAM;
import static fi.livi.digitraffic.tie.controller.ApiConstants.BETA;
import static fi.livi.digitraffic.tie.controller.ApiConstants.LAST_UPDATED_PARAM;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_GEO_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_VND_GEO_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_OK;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.controller.ApiConstants;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamStationDataV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamStationFeatureCollectionSimpleV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamStationFeatureV1Detailed;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamStationsDatasV1;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetHistoryDataService;
import fi.livi.digitraffic.tie.service.weathercam.v1.WeathercamDataWebServiceV1;
import fi.livi.digitraffic.tie.service.weathercam.v1.WeathercamMetadataWebServiceV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = ApiConstants.WEATHERCAM_BETA_TAG, description = "Weathercam Controller (BETA)")
@RestController
@Validated
@ConditionalOnWebApplication
public class WeathercamControllerV1 {

    /**
     * API paths:
     *
     * Metadata
     * /api/weathercam/v/stations (simple)
     * /api/weathercam/v/stations/{id} (detailed)
     *
     * Data
     * /api/weathercam/v/stations/data (all)
     * /api/weathercam/v/stations/{id}/data (one station)
     *
     * Histories TODO
     * /api/weathercam/v/stations/histories
     * /api/weathercam/v/stations/histories/changes
     * /api/weathercam/v/stations/presences
     *
     */

//    private static final String API_WEATHERCAM_V1 = API_WEATHERCAM + V1;
    private static final String API_WEATHERCAM_BETA = API_WEATHERCAM + BETA;

    private static final String STATIONS = "/stations";
    private static final String HISTORIES = "/history";
    private static final String DATA = "/data";
    private static final String PRESENCE = "/presence";

    /** TODO change beta when going to production */
    private static final String API_WEATHERCAM_V1_STATIONS = API_WEATHERCAM_BETA + STATIONS;
    private static final String API_WEATHERCAM_V1_HISTORIES = API_WEATHERCAM_BETA + HISTORIES;

    private final WeathercamMetadataWebServiceV1 weathercamMetadataWebServiceV1;
    private WeathercamDataWebServiceV1 weathercamDataWebServiceV1;
    private final CameraPresetHistoryDataService cameraPresetHistoryDataService;

    @Autowired
    public WeathercamControllerV1(final WeathercamMetadataWebServiceV1 weathercamMetadataWebServiceV1,
                                  final WeathercamDataWebServiceV1 weathercamDataWebServiceV1,
                                  final CameraPresetHistoryDataService cameraPresetHistoryDataService) {
        this.weathercamMetadataWebServiceV1 = weathercamMetadataWebServiceV1;
        this.weathercamDataWebServiceV1 = weathercamDataWebServiceV1;
        this.cameraPresetHistoryDataService = cameraPresetHistoryDataService;
    }

    @Operation(summary = "The static information of weather camera stations")
    @RequestMapping(method = RequestMethod.GET, path = API_WEATHERCAM_V1_STATIONS,
                    produces = { APPLICATION_JSON_VALUE, APPLICATION_GEO_JSON_VALUE, APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Camera Preset Feature Collections") })
    public WeathercamStationFeatureCollectionSimpleV1 weathercamStations(
        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
        final boolean lastUpdated) {
        return weathercamMetadataWebServiceV1.findAllPublishableCameraStationsAsSimpleFeatureCollection(lastUpdated);
    }

    @Operation(summary = "The static information of weather camera stations")
    @RequestMapping(method = RequestMethod.GET, path = API_WEATHERCAM_V1_STATIONS + "/{id}",
                    produces = { APPLICATION_JSON_VALUE, APPLICATION_GEO_JSON_VALUE, APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Success") })
    public WeathercamStationFeatureV1Detailed weathercamStation(
// TODO: Should this be implemented too?
//        @Parameter(description = "If parameter is given result will only contain update status.")
//        @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
//        final boolean lastUpdated,
        @Parameter(description = "Weathercam station id", required = true)
        @PathVariable
        final String id) {
        return weathercamMetadataWebServiceV1.findPublishableCameraStationAsDetailedFeature(id);
    }


    @Operation(summary = "Current data of weathercams")
    @RequestMapping(method = RequestMethod.GET, path = API_WEATHERCAM_V1_STATIONS + DATA, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of camera station data"))
    public WeathercamStationsDatasV1 weathercamsDatas(
        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = LAST_UPDATED_PARAM, required = false, defaultValue = "false")
        final boolean lastUpdated) {
        return weathercamDataWebServiceV1.findPublishableWeathercamStationsData(lastUpdated);
    }

    @Operation(summary = "Current data of weathercam")
    @RequestMapping(method = RequestMethod.GET, path = API_WEATHERCAM_V1_STATIONS + "/{id}" + DATA, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of camera station data"))
    public WeathercamStationDataV1 weathercamDatasByStationId(
        @Parameter(description = "Camera station id", required = true)
        @PathVariable
        final String id,
        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = LAST_UPDATED_PARAM, required = false, defaultValue = "false")
        final boolean lastUpdated) {
        return weathercamDataWebServiceV1.findPublishableWeathercamStationData(id, lastUpdated);
    }

    /* History APIs */

    // TODO CameraHistoryDto: peri StationDataV1
    // TODO: mieti vielä rajapinnat kuntoon mm. polut ja histories/{id} varmaan riittävä ei tarvitse tukea listaa asemien historiasta
    // TODO sama changes-rajapinnan kanssa
/*
    @Operation(summary = "Weathercam history for given camera or preset")
    @RequestMapping(method = RequestMethod.GET, path = API_WEATHERCAM_V1_HISTORIES, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of camera images history"))
    public List<CameraHistoryDto> getWeathercamStationOrPresetHistory(

        @Parameter(description = "Camera or preset id(s)", required = true)
        @RequestParam(value = "id")
        final List<String> cameraOrPresetIds,
        @Parameter(description = "Return the latest url for the image from the history at the given date time. " +
                                 "If the time is not given then the history of last 24h is returned.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @RequestParam(value = "at", required = false)
        final Instant at) {

        return cameraPresetHistoryDataService.findCameraOrPresetPublicHistory(cameraOrPresetIds, at);
    }

    @Operation(summary = "Weather camera history changes after given time. Result is in ascending order by presetId and lastModified -fields.")
    @RequestMapping(method = RequestMethod.GET, path = API_WEATHERCAM_V1_HISTORIES + "/changes", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of camera history changes"))
    public CameraHistoryChangesDto getWeathercamStationOrPresetHistoryChanges(

        @Parameter(description = "Camera or preset id(s)")
        @RequestParam(value = "id", required = false)
        final List<String> cameraOrPresetIds,

        @Parameter(description = "Return changes int the history after given time. Given time must be within 24 hours.", required = true)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @RequestParam
        final ZonedDateTime after) {

        if (after.plus(24, HOURS).isBefore(ZonedDateTime.now())) {
            throw new IllegalArgumentException("Given time must be within 24 hours.");
        }

        return cameraPresetHistoryDataService.findCameraOrPresetHistoryChangesAfter(after, cameraOrPresetIds == null ? Collections.emptyList() : cameraOrPresetIds);
    }
     */
}