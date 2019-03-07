package fi.livi.digitraffic.tie.metadata.controller;

import static fi.livi.digitraffic.tie.data.controller.DataController.FORECAST_SECTION_WEATHER_DATA_PATH;
import static fi.livi.digitraffic.tie.data.controller.DataController.LAST_UPDATED_PARAM;
import static fi.livi.digitraffic.tie.metadata.controller.MetadataController.FORECAST_SECTIONS_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.conf.RoadApplicationConfiguration;
import fi.livi.digitraffic.tie.data.controller.DataController;
import fi.livi.digitraffic.tie.data.dto.forecast.ForecastSectionWeatherRootDto;
import fi.livi.digitraffic.tie.data.dto.tms.TmsSensorConstantRootDto;
import fi.livi.digitraffic.tie.data.service.ForecastSectionDataService;
import fi.livi.digitraffic.tie.data.service.TmsDataDatex2Service;
import fi.livi.digitraffic.tie.data.service.TmsDataService;
import fi.livi.digitraffic.tie.helper.EnumConverter;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TmsDataDatex2Response;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TmsStationDatex2Response;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionV2FeatureCollection;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.ForecastSectionApiVersion;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.ForecastSectionV2MetadataService;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationDatex2Service;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "beta", description = "Beta apis")
@RestController
@Validated
@RequestMapping(RoadApplicationConfiguration.API_BETA_BASE_PATH)
@ConditionalOnWebApplication
public class BetaController {
    public static final String TMS_STATIONS_DATEX2_PATH = "/tms-stations-datex2";
    public static final String TMS_DATA_DATEX2_PATH = "/tms-data-datex2";
    public static final String TMS_SENSOR_CONSTANTS = "/tms-sensor-constants";

    private final TmsStationDatex2Service tmsStationDatex2Service;
    private final TmsDataDatex2Service tmsDataDatex2Service;
    private final TmsDataService tmsDataService;
    private final ForecastSectionDataService forecastSectionDataService;
    private final ForecastSectionV2MetadataService forecastSectionV2MetadataService;

    @Autowired
    public BetaController(final TmsStationDatex2Service tmsStationDatex2Service, final TmsDataDatex2Service tmsDataDatex2Service,
                          final TmsDataService tmsDataService, final ForecastSectionDataService forecastSectionDataService,
                          final ForecastSectionV2MetadataService forecastSectionV2MetadataService) {
        this.tmsStationDatex2Service = tmsStationDatex2Service;
        this.tmsDataDatex2Service = tmsDataDatex2Service;
        this.tmsDataService = tmsDataService;
        this.forecastSectionDataService = forecastSectionDataService;
        this.forecastSectionV2MetadataService = forecastSectionV2MetadataService;
    }

    @ApiOperation("The static information of TMS stations in Datex2 format (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_UTF8_VALUE})
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of TMS Station Feature Collections"))
    public TmsStationDatex2Response tmsStationsDatex2(
        @ApiParam(value = "Return TMS stations of given state.", allowableValues = "active,removed,all")
        @RequestParam(value = "state", required = false, defaultValue = "active")
        final String stateString) {

        final TmsState state = EnumConverter.parseState(TmsState.class, stateString);

        return tmsStationDatex2Service.findAllPublishableTmsStationsAsDatex2(state);
    }

    @ApiOperation("Current data of TMS Stations in Datex2 format (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_DATA_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_UTF8_VALUE })
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of TMS Station data"))
    public TmsDataDatex2Response tmsDataDatex2() {
        return tmsDataDatex2Service.findPublishableTmsDataDatex2();
    }

    @ApiOperation("Current sensor constants and values of TMS station (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_SENSOR_CONSTANTS, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of sensor constants and values"))
    public TmsSensorConstantRootDto tmsSensorConstants(
        @ApiParam("If parameter is given result will only contain update status")
        @RequestParam(value=DataController.LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
        boolean lastUpdated) {
        return tmsDataService.findPublishableSensorConstants(lastUpdated);
    }

    @ApiOperation("Current data of Weather Forecast Sections V2")
    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTION_WEATHER_DATA_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of Weather Forecast Section V2 data"))
    public ForecastSectionWeatherRootDto roadConditions(
        @ApiParam("If parameter is given result will only contain update status")
        @RequestParam(value=LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
        boolean lastUpdated,
        @ApiParam(value = "List of forecast section indices")
        @RequestParam(value = "naturalIds", required = false)
        final List<String> naturalIds) {
        return forecastSectionDataService.getForecastSectionWeatherData(ForecastSectionApiVersion.V2, lastUpdated, null,
                                                                        null, null, null, null,
                                                                        naturalIds);
    }

    @ApiOperation("Current data of Weather Forecast Sections V2 by road number")
    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTION_WEATHER_DATA_PATH + "/{roadNumber}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of Weather Forecast Section V2 data"))
    public ForecastSectionWeatherRootDto roadConditions(
        @PathVariable("roadNumber") final int roadNumber) {
        return forecastSectionDataService.getForecastSectionWeatherData(ForecastSectionApiVersion.V2, false, roadNumber,
                                                                        null, null, null, null,
                                                                        null);
    }

    @ApiOperation("Current data of Weather Forecast Sections V2 by bounding box")
    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTION_WEATHER_DATA_PATH + "/{minLongitude}/{minLatitude}/{maxLongitude}/{maxLatitude}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of Weather Forecast Section V2 data"))
    public ForecastSectionWeatherRootDto roadConditions(
        @PathVariable("minLongitude") final double minLongitude,
        @PathVariable("minLatitude") final double minLatitude,
        @PathVariable("maxLongitude") final double maxLongitude,
        @PathVariable("maxLatitude") final double maxLatitude) {
        return forecastSectionDataService.getForecastSectionWeatherData(ForecastSectionApiVersion.V2, false, null,
                                                                        minLongitude, minLatitude, maxLongitude, maxLatitude, null);
    }

    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTIONS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation("The static information of weather forecast sections V2")
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of Forecast Sections V2") })
    public ForecastSectionV2FeatureCollection forecastSections(
        @ApiParam("If parameter is given result will only contain update status.")
        @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
        final boolean lastUpdated,
        @ApiParam(value = "List of forecast section indices")
        @RequestParam(value = "naturalIds", required = false)
        final List<String> naturalIds) {
        return forecastSectionV2MetadataService.getForecastSectionV2Metadata(lastUpdated, null, null, null,
                                                                             null,null, naturalIds);
    }

    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTIONS_PATH + "/{roadNumber}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation("The static information of weather forecast sections V2 by road number")
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of Forecast Sections V2") })
    public ForecastSectionV2FeatureCollection forecastSections(
        @PathVariable("roadNumber") final int roadNumber) {
        return forecastSectionV2MetadataService.getForecastSectionV2Metadata(false, roadNumber, null, null,
                                                                             null, null, null);
    }

    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTIONS_PATH + "/{minLongitude}/{minLatitude}/{maxLongitude}/{maxLatitude}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation("The static information of weather forecast sections V2 by bounding box")
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of Forecast Sections V2") })
    public ForecastSectionV2FeatureCollection forecastSections(
        @PathVariable("minLongitude") final double minLongitude,
        @PathVariable("minLatitude") final double minLatitude,
        @PathVariable("maxLongitude") final double maxLongitude,
        @PathVariable("maxLatitude") final double maxLatitude) {
        return forecastSectionV2MetadataService.getForecastSectionV2Metadata(false, null, minLongitude, minLatitude,
                                                                             maxLongitude, maxLatitude, null);
    }
}