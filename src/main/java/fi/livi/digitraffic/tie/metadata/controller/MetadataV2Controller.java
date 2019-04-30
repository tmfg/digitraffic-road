package fi.livi.digitraffic.tie.metadata.controller;

import static fi.livi.digitraffic.tie.conf.RoadApplicationConfiguration.API_METADATA_PART_PATH;
import static fi.livi.digitraffic.tie.conf.RoadApplicationConfiguration.API_V2_BASE_PATH;
import static fi.livi.digitraffic.tie.metadata.controller.MetadataController.FORECAST_SECTIONS_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionV2FeatureCollection;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.ForecastSectionV2MetadataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "Metadata v2", description = "Metadata for Digitraffic services (Api version 2)")
@RestController
@RequestMapping(API_V2_BASE_PATH + API_METADATA_PART_PATH)
@ConditionalOnWebApplication
public class MetadataV2Controller {

    private final ForecastSectionV2MetadataService forecastSectionV2MetadataService;

    @Autowired
    public MetadataV2Controller(final ForecastSectionV2MetadataService forecastSectionV2MetadataService) {
        this.forecastSectionV2MetadataService = forecastSectionV2MetadataService;
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