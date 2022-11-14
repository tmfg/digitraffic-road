package fi.livi.digitraffic.tie.controller.v2;

import static fi.livi.digitraffic.tie.controller.ApiDeprecations.API_NOTE_2023_01_01;
import static fi.livi.digitraffic.tie.controller.ApiDeprecations.API_NOTE_2023_06_01;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_METADATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V2_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.FORECAST_SECTIONS_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.VARIABLE_SIGNS_CODE_DESCRIPTIONS;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_GEO_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_VND_GEO_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_OK;
import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.COORD_FORMAT_WGS84;

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

import fi.livi.digitraffic.tie.annotation.Sunset;
import fi.livi.digitraffic.tie.controller.ApiDeprecations;
import fi.livi.digitraffic.tie.dto.v1.VariableSignDescriptions;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionV2FeatureCollection;
import fi.livi.digitraffic.tie.service.v2.V2VariableSignDataService;
import fi.livi.digitraffic.tie.service.v2.forecastsection.V2ForecastSectionMetadataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Metadata v2", description = "Metadata for Digitraffic services (Api version 2)")
@RestController
@RequestMapping(API_V2_BASE_PATH + API_METADATA_PART_PATH)
@ConditionalOnWebApplication
public class V2MetadataController {
    private final V2ForecastSectionMetadataService v2ForecastSectionMetadataService;
    private final V2VariableSignDataService v2VariableSignDataService;

    @Autowired
    public V2MetadataController(final V2ForecastSectionMetadataService v2ForecastSectionMetadataService,
        final V2VariableSignDataService v2VariableSignDataService) {
        this.v2ForecastSectionMetadataService = v2ForecastSectionMetadataService;
        this.v2VariableSignDataService = v2VariableSignDataService;
    }

    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTIONS_PATH, produces = { APPLICATION_JSON_VALUE,
                                                                                            APPLICATION_GEO_JSON_VALUE,
                                                                                            APPLICATION_VND_GEO_JSON_VALUE })
    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "The static information of weather forecast sections V2. " + API_NOTE_2023_06_01)
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Forecast Sections V2") })
    public ForecastSectionV2FeatureCollection forecastSections(
        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
        final boolean lastUpdated,
        @Parameter(description = "List of forecast section indices")
        @RequestParam(value = "naturalIds", required = false)
        final List<String> naturalIds) {
        return v2ForecastSectionMetadataService.getForecastSectionV2Metadata(lastUpdated, null, null, null,
            null,null, naturalIds);
    }

    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTIONS_PATH + "/{roadNumber}", produces = { APPLICATION_JSON_VALUE,
                                                                                                              APPLICATION_GEO_JSON_VALUE,
                                                                                                              APPLICATION_VND_GEO_JSON_VALUE })
    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "The static information of weather forecast sections V2 by road number. " + API_NOTE_2023_06_01)
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Forecast Sections V2") })
    public ForecastSectionV2FeatureCollection forecastSections(
        @PathVariable("roadNumber") final int roadNumber) {
        return v2ForecastSectionMetadataService.getForecastSectionV2Metadata(false, roadNumber, null, null,
            null, null, null);
    }

    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTIONS_PATH + "/{minLongitude}/{minLatitude}/{maxLongitude}/{maxLatitude}", produces = {
        APPLICATION_JSON_VALUE,
        APPLICATION_GEO_JSON_VALUE,
        APPLICATION_VND_GEO_JSON_VALUE })
    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "The static information of weather forecast sections V2 by bounding box. " + API_NOTE_2023_06_01)
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Forecast Sections V2") })
    public ForecastSectionV2FeatureCollection forecastSections(
        @Parameter(description = "Minimum longitude. " + COORD_FORMAT_WGS84)
        @PathVariable("minLongitude") final double minLongitude,
        @Parameter(description = "Minimum latitude. " + COORD_FORMAT_WGS84)
        @PathVariable("minLatitude") final double minLatitude,
        @Parameter(description = "Maximum longitude. " + COORD_FORMAT_WGS84)
        @PathVariable("maxLongitude") final double maxLongitude,
        @Parameter(description = "Minimum latitude. " + COORD_FORMAT_WGS84)
        @PathVariable("maxLatitude") final double maxLatitude) {
        return v2ForecastSectionMetadataService.getForecastSectionV2Metadata(false, null, minLongitude, minLatitude,
            maxLongitude, maxLatitude, null);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_01_01)
    @Operation(summary = "Return all code descriptions. " + API_NOTE_2023_01_01)
    @GetMapping(path = VARIABLE_SIGNS_CODE_DESCRIPTIONS, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public VariableSignDescriptions listCodeDescriptions() {
        return new VariableSignDescriptions(v2VariableSignDataService.listVariableSignTypes());
    }
}