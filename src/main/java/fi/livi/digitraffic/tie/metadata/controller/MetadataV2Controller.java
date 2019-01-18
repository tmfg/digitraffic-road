package fi.livi.digitraffic.tie.metadata.controller;

import static fi.livi.digitraffic.tie.conf.RoadApplicationConfiguration.API_METADATA_PART_PATH;
import static fi.livi.digitraffic.tie.conf.RoadApplicationConfiguration.API_V2_BASE_PATH;
import static fi.livi.digitraffic.tie.metadata.controller.MetadataController.FORECAST_SECTIONS_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
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

@Api(tags = "metadata", description = "Metadata for Digitraffic services")
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
        final boolean lastUpdated) {
        return forecastSectionV2MetadataService.getForecastSectionV2Metadata(lastUpdated);
    }

}
