package fi.livi.digitraffic.tie.controller.tms;

import static fi.livi.digitraffic.tie.controller.ApiConstants.API_TMS;
import static fi.livi.digitraffic.tie.controller.ApiConstants.BETA;
import static fi.livi.digitraffic.tie.controller.ApiConstants.LAST_UPDATED_PARAM;
import static fi.livi.digitraffic.tie.controller.ApiConstants.TMS_BETA_TAG;
import static fi.livi.digitraffic.tie.controller.ApiConstants.V1;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_GEO_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_VND_GEO_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_NOT_FOUND;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_OK;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.controller.RoadStationState;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationDataDtoV1;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationFeatureCollectionSimpleV1;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationFeatureDetailedV1;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationSensorConstantDtoV1;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationSensorsDtoV1;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationsDataDtoV1;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationsSensorConstantsDataDtoV1;
import fi.livi.digitraffic.tie.service.roadstation.v1.RoadStationSensorServiceV1;
import fi.livi.digitraffic.tie.service.tms.v1.TmsDataWebServiceV1;
import fi.livi.digitraffic.tie.service.tms.v1.TmsStationMetadataWebServiceV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = TMS_BETA_TAG, description = "TMS Controller")
@RestController
@Validated
@ConditionalOnWebApplication
public class TmsControllerV1 {

    private final TmsDataWebServiceV1 tmsDataWebServiceV1;
    private final TmsStationMetadataWebServiceV1 tmsStationMetadataWebServiceV1;
    private final RoadStationSensorServiceV1 roadStationSensorServiceV1;

    /**
     * API paths:
     *
     * Metadata
     * /api/tms/v/stations (simple)
     * /api/tms/v/stations/{id} (detailed)
     * /api/tms/v/sensors/ (sensors metadata)
     *
     * Data
     * /api/tms/v/stations/data (all)
     * /api/tms/v/stations/{id}/data (one station)
     * /api/tms/v/stations/sensor-constants (all)
     * /api/tms/v/stations/{id}/sensor-constants
     */

    public static final String API_TMS_BETA = API_TMS + BETA;
    public static final String API_TMS_V1 = API_TMS + V1;

    public static final String STATIONS = "/stations";
    public static final String SENSORS = "/sensors";
    public static final String DATA = "/data";
    public static final String SENSOR_CONSTANTS = "/sensor-constants";

    public TmsControllerV1(final TmsDataWebServiceV1 tmsDataWebServiceV1,
                           final TmsStationMetadataWebServiceV1 tmsStationMetadataWebServiceV1,
                           final RoadStationSensorServiceV1 roadStationSensorServiceV1) {
        this.tmsDataWebServiceV1 = tmsDataWebServiceV1;
        this.tmsStationMetadataWebServiceV1 = tmsStationMetadataWebServiceV1;
        this.roadStationSensorServiceV1 = roadStationSensorServiceV1;
    }

    /* METADATA */

    @Operation(summary = "The static information of TMS stations (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET,
                    path = API_TMS_BETA + STATIONS,
                    produces = { APPLICATION_JSON_VALUE,
                                 APPLICATION_GEO_JSON_VALUE,
                                 APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of TMS Station Feature Collections"))
    public TmsStationFeatureCollectionSimpleV1 tmsStations(
        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = LAST_UPDATED_PARAM, required = false, defaultValue = "false")
        final boolean lastUpdated,
        @Parameter(description = "Return TMS stations of given state.", required = true)
        @RequestParam(value = "roadStationState",
                      required = false,
                      defaultValue = "ACTIVE")
        final RoadStationState roadStationState) {

        return tmsStationMetadataWebServiceV1.findAllPublishableTmsStationsAsSimpleFeatureCollection(lastUpdated, roadStationState);
    }

    @Operation(summary = "The static information of one TMS station (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET,
                    path = API_TMS_BETA + STATIONS + "/{id}",
                    produces = { APPLICATION_JSON_VALUE,
                                 APPLICATION_GEO_JSON_VALUE,
                                 APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of TMS Station Feature"),
                    @ApiResponse(responseCode = HTTP_NOT_FOUND,
                                 description = "Road Station not found",
                                 content = @Content) })
    public TmsStationFeatureDetailedV1 tmsStationByRoadStationId(
        @PathVariable("id")
        final Long id) {
        return tmsStationMetadataWebServiceV1.getTmsStationById(id);
    }

    @Operation(summary = "The static information of available sensors of TMS stations (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET,
                    path =  API_TMS_BETA + SENSORS,
                    produces = APPLICATION_JSON_VALUE)
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK,
                                 description = "Successful retrieval of TMS station sensors") })
    public TmsStationSensorsDtoV1 tmsSensors(
        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
        final boolean lastUpdated) {
        return roadStationSensorServiceV1.findTmsRoadStationsSensorsMetadata(lastUpdated);
    }

    /* DATA */

    @Operation(summary = "Current data of TMS stations (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET,
                    path = API_TMS_BETA + STATIONS + DATA,
                    produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of TMS station data"))
    public TmsStationsDataDtoV1 tmsData(
        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = LAST_UPDATED_PARAM,
                      required = false,
                      defaultValue = "false")
        final boolean lastUpdated) {
        return tmsDataWebServiceV1.findPublishableTmsData(lastUpdated);
    }

    @Operation(summary = "Current data of one TMS station (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET,
                    path = API_TMS_BETA + STATIONS + "/{id}" + DATA,
                    produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of TMS station data"))
    public TmsStationDataDtoV1 tmsDataById(
        @Parameter(description = "TMS Station id", required = true)
        @PathVariable
        final long id) {
        return tmsDataWebServiceV1.findPublishableTmsData(id);
    }

    @Operation(summary = "Current sensor constants and values of TMS stations (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET,
                    path = API_TMS_BETA + STATIONS + SENSOR_CONSTANTS,
                    produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of sensor constants and values"))
    public TmsStationsSensorConstantsDataDtoV1 tmsSensorConstants(
        @Parameter(description = "If parameter is given result will only contain update status")
        @RequestParam(value = LAST_UPDATED_PARAM,
                      required = false,
                      defaultValue = "false")
        final boolean lastUpdated) {
        return tmsDataWebServiceV1.findPublishableSensorConstants(lastUpdated);
    }

    @Operation(summary = "Current sensor constants and values of one TMS station (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET,
                    path = API_TMS_BETA + STATIONS + "/{id}" + SENSOR_CONSTANTS,
                    produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of sensor constants and values"))
    public TmsStationSensorConstantDtoV1 tmsSensorConstantsByStationId(
        @Parameter(description = "TMS Station id",
                   required = true)
        @PathVariable
        final long id) {
        return tmsDataWebServiceV1.findPublishableSensorConstantsForStation(id);
    }
}

