package fi.livi.digitraffic.tie.controller.tms;

import static fi.livi.digitraffic.tie.controller.ApiConstants.API_TMS;
import static fi.livi.digitraffic.tie.controller.ApiConstants.LAST_UPDATED_PARAM;
import static fi.livi.digitraffic.tie.controller.ApiConstants.TMS_TAG_V1;
import static fi.livi.digitraffic.tie.controller.ApiConstants.V1;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_GEO_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_VND_GEO_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.TEXT_CSV_VALUE;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_NOT_FOUND;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_OK;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.common.util.StringUtil;
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
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = TMS_TAG_V1)
@RestController
@Validated
@ConditionalOnWebApplication
public class TmsControllerV1 {
    private static final Logger log = LoggerFactory.getLogger(TmsControllerV1.class);
    private final TmsDataWebServiceV1 tmsDataWebServiceV1;
    private final TmsStationMetadataWebServiceV1 tmsStationMetadataWebServiceV1;
    private final RoadStationSensorServiceV1 roadStationSensorServiceV1;

    /**
     * API paths:
     * <p>
     * Metadata
     * /api/tms/v/stations (simple)
     * /api/tms/v/stations/{id} (detailed)
     * /api/tms/v/sensors/ (sensors metadata)
     * <p>
     * Data
     * /api/tms/v/stations/data (all)
     * /api/tms/v/stations/{id}/data (one station)
     * /api/tms/v/stations/sensor-constants (all)
     * /api/tms/v/stations/{id}/sensor-constants
     */

    // public static final String API_TMS_BETA = API_TMS + BETA;
    public static final String API_TMS_V1 = API_TMS + V1;

    public static final String STATIONS = "/stations";
    public static final String SENSORS = "/sensors";
    public static final String DATA = "/data";
    public static final String SENSOR_CONSTANTS = "/sensor-constants";
    public static final String DATEX2 = "/datex2";

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
                    path = API_TMS_V1 + STATIONS,
                    produces = { APPLICATION_JSON_VALUE,
                                 APPLICATION_GEO_JSON_VALUE,
                                 APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of TMS Station Feature Collections"))
    public TmsStationFeatureCollectionSimpleV1 tmsStations(
        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = LAST_UPDATED_PARAM, required = false, defaultValue = "false")
        final boolean lastUpdated,
        @Parameter(description = "Return TMS stations of given state.")
        @RequestParam(required = false, defaultValue = "ACTIVE")
        final RoadStationState state) {
        return tmsStationMetadataWebServiceV1.findAllPublishableTmsStationsAsSimpleFeatureCollection(lastUpdated, state);
    }

    @Operation(summary = "The static information of one TMS station (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET,
                    path = API_TMS_V1 + STATIONS + "/{id}",
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
                    path =  API_TMS_V1 + SENSORS,
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
                    path = API_TMS_V1 + STATIONS + DATA,
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
                    path = API_TMS_V1 + STATIONS + "/{id}" + DATA,
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
                    path = API_TMS_V1 + STATIONS + SENSOR_CONSTANTS,
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
                    path = API_TMS_V1 + STATIONS + "/{id}" + SENSOR_CONSTANTS,
                    produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of sensor constants and values"))
    public TmsStationSensorConstantDtoV1 tmsSensorConstantsByStationId(
        @Parameter(description = "TMS Station id",
                   required = true)
        @PathVariable
        final long id) {
        return tmsDataWebServiceV1.findPublishableSensorConstantsForStation(id);
    }

    @Operation(summary = "TMS raw history data (Traffic Measurement System / LAM)",
               description = "More documentation at <a href=\"https://www.digitraffic.fi/en/road-traffic/lam/#tms-raw-data\">https://www.digitraffic.fi/en/road-traffic/lam/#tms-raw-data</a>")
    @RequestMapping(method = RequestMethod.GET,
                    path = "/api/tms/v1/history/raw/lamraw_{tmsNumber}_{yearShort}_{dayNumber}.csv",
                    produces = TEXT_CSV_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of TMS raw history data"))
    public TmsRawHistoryCsv[] tmsRawHistory(
            @Parameter(description = "TMS Station tmsNumber (Not station id!)", required = true )
            @PathVariable()
            final int tmsNumber,
            @Parameter(description = "Year in short format, last two digits of the year", required = true)
            @PathVariable
            final int yearShort,
            @Parameter(description = "Day of the year (i.e. ordinal date, value between 1-366, taking into account leap years). e.g. 1.1. = 1", required = true)
            @PathVariable
            final long dayNumber) {
        log.error("method=tmsRawHistory called with parameters tmsNumber={} yearShort={} dayNumber={}. " +
                  "This should never come to TmsControllerV1 but to CloudFront and there to LAM history lambda.",
                tmsNumber, yearShort, dayNumber);
        throw new UnsupportedOperationException(StringUtil.format("method=tmsRawHistory called with parameters tmsNumber={} yearShort={} dayNumber={}.", tmsNumber, yearShort, dayNumber));
    }

    public record TmsRawHistoryCsv(
            @Schema(description = "TMS Station tmsNumber (Not station id!)", requiredMode = Schema.RequiredMode.REQUIRED)
            int tmsNumber,
            @Schema(description =  "Year in short format, last two digits of the year", minimum = "0", maximum = "99", requiredMode = Schema.RequiredMode.REQUIRED)
            int yearShort,
            @Schema(description =  "Day of the year (i.e. ordinal date, taking into account leap years). e.g. 1.1. = 1", minimum = "1", maximum = "366", requiredMode = Schema.RequiredMode.REQUIRED)
            int dayNumber,
            @Schema(description =  "Hour of the day (24h)", minimum = "0", maximum = "23", requiredMode = Schema.RequiredMode.REQUIRED)
            int hour,
            @Schema(description =  "Minute of the hour", minimum = "0", maximum = "59", requiredMode = Schema.RequiredMode.REQUIRED)
            int minute,
            @Schema(description =  "Second of the minute", minimum = "0", maximum = "59", requiredMode = Schema.RequiredMode.REQUIRED)
            int second,
            @Schema(description =  "Hundredth of the second", minimum = "0", maximum = "99", requiredMode = Schema.RequiredMode.REQUIRED)
            int hundredthOfASecond,
            @Schema(description =  "Length of the vehicle in meters", minimum = "1.0", maximum = "39.8", requiredMode = Schema.RequiredMode.REQUIRED)
            double length,
            @Schema(description =  "Lane", requiredMode = Schema.RequiredMode.REQUIRED)
            int lane,
            @Schema(description =  "Measurement direction </br>" +
                    "1 = According to the road register address increasing direction. I.e. on the road 4 to Rovaniemi.</br>" +
                    "2 = According to the road register address decreasing direction. I.e. on the road 4 to Helsinki.", minimum = "1", maximum = "2", requiredMode = Schema.RequiredMode.REQUIRED)
            int direction,
            @Schema(description =  "Vehicle class</br>" +
                    "1 HA-PA (car or delivery van)</br>" +
                    "2 KAIP (truck, no trailer)</br>" +
                    "3 Buses</br>" +
                    "4 KAPP (semi-trailer truck)</br>" +
                    "5 KATP (truck with trailer)</br>" +
                    "6 HA + PK (car or delivery van with trailer)</br>" +
                    "7 HA + AV (car or delivery van with trailer or camper)",
                    minimum = "1",  maximum = "7", requiredMode = Schema.RequiredMode.REQUIRED)
            int vehicleClass,
            @Schema(description =  "Speed in km/h", minimum = "2",  maximum = "188", requiredMode = Schema.RequiredMode.REQUIRED)
            int speed,
            @Schema(description =  "Is record faulty (0=valid record, 1=faulty record)", minimum = "0", maximum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
            int faulty,
            @Schema(description =  "Total time (technical)", requiredMode = Schema.RequiredMode.REQUIRED)
            int totalTime,
            @Schema(description =  "Time interval (technical)", requiredMode = Schema.RequiredMode.REQUIRED)
            int timeTnterval,
            @Schema(description =  "Queue start (technical)", requiredMode = Schema.RequiredMode.REQUIRED)
            int queueStart) {

    }
}

