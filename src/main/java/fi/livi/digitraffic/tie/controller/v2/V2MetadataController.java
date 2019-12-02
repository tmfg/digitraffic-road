package fi.livi.digitraffic.tie.controller.v2;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_METADATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V2_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.FORECAST_SECTIONS_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.VARIABLE_SIGNS_CODE_DESCRIPTIONS;
import static fi.livi.digitraffic.tie.metadata.controller.MediaTypes.MEDIA_TYPE_APPLICATION_GEO_JSON;
import static fi.livi.digitraffic.tie.metadata.controller.MediaTypes.MEDIA_TYPE_APPLICATION_JSON;
import static fi.livi.digitraffic.tie.metadata.controller.MediaTypes.MEDIA_TYPE_APPLICATION_VND_GEO_JSON;
import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.COORD_FORMAT_WGS84;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.data.service.VariableSignService;
import fi.livi.digitraffic.tie.metadata.dto.VariableSignDescriptions;
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
public class V2MetadataController {
    private final ForecastSectionV2MetadataService forecastSectionV2MetadataService;
    private final VariableSignService variableSignService;

    @Autowired
    public V2MetadataController(final ForecastSectionV2MetadataService forecastSectionV2MetadataService,
        final VariableSignService variableSignService) {
        this.forecastSectionV2MetadataService = forecastSectionV2MetadataService;
        this.variableSignService = variableSignService;
    }

    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTIONS_PATH, produces = { MEDIA_TYPE_APPLICATION_JSON,
                                                                                            MEDIA_TYPE_APPLICATION_GEO_JSON,
                                                                                            MEDIA_TYPE_APPLICATION_VND_GEO_JSON })
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

    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTIONS_PATH + "/{roadNumber}", produces = { MEDIA_TYPE_APPLICATION_JSON,
                                                                                                              MEDIA_TYPE_APPLICATION_GEO_JSON,
                                                                                                              MEDIA_TYPE_APPLICATION_VND_GEO_JSON })
    @ApiOperation("The static information of weather forecast sections V2 by road number")
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of Forecast Sections V2") })
    public ForecastSectionV2FeatureCollection forecastSections(
        @PathVariable("roadNumber") final int roadNumber) {
        return forecastSectionV2MetadataService.getForecastSectionV2Metadata(false, roadNumber, null, null,
            null, null, null);
    }

    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTIONS_PATH + "/{minLongitude}/{minLatitude}/{maxLongitude}/{maxLatitude}", produces = {
        MEDIA_TYPE_APPLICATION_JSON,
        MEDIA_TYPE_APPLICATION_GEO_JSON,
        MEDIA_TYPE_APPLICATION_VND_GEO_JSON })
    @ApiOperation("The static information of weather forecast sections V2 by bounding box")
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of Forecast Sections V2") })
    public ForecastSectionV2FeatureCollection forecastSections(
        @ApiParam(value = "Minimum longitude. " + COORD_FORMAT_WGS84)
        @PathVariable("minLongitude") final double minLongitude,
        @ApiParam(value = "Minimum latitude. " + COORD_FORMAT_WGS84)
        @PathVariable("minLatitude") final double minLatitude,
        @ApiParam(value = "Maximum longitude. " + COORD_FORMAT_WGS84)
        @PathVariable("maxLongitude") final double maxLongitude,
        @ApiParam(value = "Minimum latitude. " + COORD_FORMAT_WGS84)
        @PathVariable("maxLatitude") final double maxLatitude) {
        return forecastSectionV2MetadataService.getForecastSectionV2Metadata(false, null, minLongitude, minLatitude,
            maxLongitude, maxLatitude, null);
    }

    @ApiOperation("Return all code descriptions.")
    @GetMapping(path = VARIABLE_SIGNS_CODE_DESCRIPTIONS, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public VariableSignDescriptions listCodeDescriptions() {
        return new VariableSignDescriptions(variableSignService.listVariableSignTypes());
    }
}