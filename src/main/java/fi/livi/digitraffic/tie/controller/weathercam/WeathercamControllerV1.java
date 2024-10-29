package fi.livi.digitraffic.tie.controller.weathercam;

import static fi.livi.digitraffic.tie.controller.ApiConstants.API_WEATHERCAM;
import static fi.livi.digitraffic.tie.controller.ApiConstants.LAST_UPDATED_PARAM;
import static fi.livi.digitraffic.tie.controller.ApiConstants.V1;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_GEO_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_VND_GEO_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_OK;
import static java.time.temporal.ChronoUnit.HOURS;

import java.time.Instant;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.format.annotation.DateTimeFormat;
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
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamStationsDataV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamStationsPresetsPublicityHistoryV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.history.WeathercamPresetsHistoryDtoV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.history.WeathercamsHistoryDtoV1;
import fi.livi.digitraffic.tie.service.weathercam.v1.WeathercamDataWebServiceV1;
import fi.livi.digitraffic.tie.service.weathercam.v1.WeathercamMetadataWebServiceV1;
import fi.livi.digitraffic.tie.service.weathercam.v1.WeathercamPresetHistoryDataWebServiceV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = ApiConstants.WEATHERCAM_TAG_V1)
@RestController
@Validated
@ConditionalOnWebApplication
public class WeathercamControllerV1 {

    /**
     * API paths:
     * <p>
     * Metadata
     * /api/weathercam/v/stations (simple)
     * /api/weathercam/v/stations/{id} (detailed)
     * <p>
     * Data
     * /api/weathercam/v/stations/data (all)
     * /api/weathercam/v/stations/{id}/data (one station)
     * <p>
     * Histories
     * /api/weathercam/v/stations/histories
     * /api/weathercam/v/stations/histories/changes
     *
     */

    private static final String API_WEATHERCAM_V1 = API_WEATHERCAM + V1;
    // private static final String API_WEATHERCAM_BETA = API_WEATHERCAM + BETA;

    private static final String STATIONS = "/stations";
    private static final String PUBLICITIES = "/publicities";
    public static final String DATA = "/data";
    public static final String HISTORY = "/history";

    public static final String API_WEATHERCAM_V1_STATIONS = API_WEATHERCAM_V1 + STATIONS;
    public static final String API_WEATHERCAM_V1_PUBLICITIES = API_WEATHERCAM_V1 + PUBLICITIES;

    private final WeathercamMetadataWebServiceV1 weathercamMetadataWebServiceV1;
    private final WeathercamDataWebServiceV1 weathercamDataWebServiceV1;
    private final WeathercamPresetHistoryDataWebServiceV1 weathercamPresetHistoryDataWebServiceV1;

    @Autowired
    public WeathercamControllerV1(final WeathercamMetadataWebServiceV1 weathercamMetadataWebServiceV1,
                                  final WeathercamDataWebServiceV1 weathercamDataWebServiceV1,
                                  final WeathercamPresetHistoryDataWebServiceV1 weathercamPresetHistoryDataWebServiceV1) {
        this.weathercamMetadataWebServiceV1 = weathercamMetadataWebServiceV1;
        this.weathercamDataWebServiceV1 = weathercamDataWebServiceV1;
        this.weathercamPresetHistoryDataWebServiceV1 = weathercamPresetHistoryDataWebServiceV1;
    }

    @Operation(summary = "The static information of weather camera stations")
    @RequestMapping(method = RequestMethod.GET, path = API_WEATHERCAM_V1_STATIONS,
                    produces = { APPLICATION_JSON_VALUE, APPLICATION_GEO_JSON_VALUE, APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Camera Preset Feature Collections") })
    public WeathercamStationFeatureCollectionSimpleV1 weathercamStations(
        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = LAST_UPDATED_PARAM, required = false, defaultValue = "false")
        final boolean lastUpdated) {
        return weathercamMetadataWebServiceV1.findAllPublishableCameraStationsAsSimpleFeatureCollection(lastUpdated);
    }

    @Operation(summary = "The static information of weather camera station")
    @RequestMapping(method = RequestMethod.GET, path = API_WEATHERCAM_V1_STATIONS + "/{id}",
                    produces = { APPLICATION_JSON_VALUE, APPLICATION_GEO_JSON_VALUE, APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Success") })
    public WeathercamStationFeatureV1Detailed weathercamStation(
        @Parameter(description = "Weathercam station id", required = true)
        @PathVariable
        final String id) {
        return weathercamMetadataWebServiceV1.findPublishableCameraStationAsDetailedFeature(id);
    }


    @Operation(summary = "Current data of weathercams")
    @RequestMapping(method = RequestMethod.GET, path = API_WEATHERCAM_V1_STATIONS + DATA, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of camera station data"))
    public WeathercamStationsDataV1 weathercamsDatas(
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
        final String id) {
        return weathercamDataWebServiceV1.findPublishableWeathercamStationData(id);
    }

    /* History APIs */

    @Operation(summary = "Weathercam presets publicity changes after given time. Result is in ascending order by presetId and lastModified -fields. ")
    @RequestMapping(method = RequestMethod.GET, path = API_WEATHERCAM_V1_PUBLICITIES + "/changes", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of camera history changes"))
    public WeathercamStationsPresetsPublicityHistoryV1 weathercamPresetPublicityChangesAfter(

        @Parameter(description = "Return changes int the history after given time. Given time must be within 24 hours. Default is 24h in past")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @RequestParam(required = false)
        final Instant after) {

        if (after != null && after.plus(24, HOURS).isBefore(Instant.now())) {
            throw new IllegalArgumentException("Given time must be within 24 hours.");
        }

        return weathercamPresetHistoryDataWebServiceV1.findWeathercamPresetPublicityChangesAfter(
            ObjectUtils.defaultIfNull(after, Instant.now().minus(24, HOURS)));
    }

    @Operation(summary = "Weathercam presets history for given camera")
    @RequestMapping(method = RequestMethod.GET, path = API_WEATHERCAM_V1_STATIONS + "/{id}" + HISTORY , produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of weathercam image history"))
    public WeathercamPresetsHistoryDtoV1 getWeathercamPresetsHistoryById(

        @Parameter(description = "Camera id")
        @PathVariable(value = "id")
        final String cameraOrPresetId) {

        return weathercamPresetHistoryDataWebServiceV1.findCameraOrPresetPublicHistory(cameraOrPresetId);
    }

    @Operation(summary = "Weathercams presets history for all cameras")
    @RequestMapping(method = RequestMethod.GET, path = API_WEATHERCAM_V1_STATIONS + HISTORY, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of weathercams image history"))
    // TODO presetId-query parameter?
    public WeathercamsHistoryDtoV1 getWeathercamsPresetsHistory() {
        return weathercamPresetHistoryDataWebServiceV1.getWeathercamsHistory();
    }
}