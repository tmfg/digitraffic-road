package fi.livi.digitraffic.tie.controller.v3;

import static fi.livi.digitraffic.tie.controller.ApiDeprecations.API_NOTE_2023_01_01;
import static fi.livi.digitraffic.tie.controller.ApiDeprecations.API_NOTE_2023_06_01;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_METADATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V3_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.CAMERA_STATIONS_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.FORECAST_SECTIONS_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.LOCATIONS_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.LOCATION_TYPES_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.LOCATION_VERSIONS_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TMS_STATIONS_AVAILABLE_SENSORS_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TMS_STATIONS_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TMS_STATIONS_ROAD_NUMBER_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TMS_STATIONS_ROAD_STATION_ID_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TMS_STATIONS_TMS_NUMBER_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.VARIABLE_SIGNS_CODE_DESCRIPTIONS;
import static fi.livi.digitraffic.tie.controller.ApiPaths.WEATHER_STATIONS_AVAILABLE_SENSORS_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.WEATHER_STATIONS_PATH;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_GEO_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_VND_GEO_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_NOT_FOUND;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_OK;
import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.COORD_FORMAT_WGS84;
import static fi.livi.digitraffic.tie.service.v1.location.LocationService.LATEST;

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
import fi.livi.digitraffic.tie.controller.RoadStationState;
import fi.livi.digitraffic.tie.dto.v1.TmsRoadStationsSensorsMetadata;
import fi.livi.digitraffic.tie.dto.v1.WeatherRoadStationsSensorsMetadata;
import fi.livi.digitraffic.tie.dto.v1.location.LocationFeatureCollection;
import fi.livi.digitraffic.tie.dto.v1.location.LocationTypesMetadata;
import fi.livi.digitraffic.tie.helper.EnumConverter;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionV2FeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationFeatureCollection;
import fi.livi.digitraffic.tie.model.v1.location.LocationVersion;
import fi.livi.digitraffic.tie.model.v3.V3VariableSignDescriptions;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.v1.camera.CameraWebService;
import fi.livi.digitraffic.tie.service.v1.location.LocationService;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationService;
import fi.livi.digitraffic.tie.service.v1.weather.WeatherStationService;
import fi.livi.digitraffic.tie.service.v2.forecastsection.V2ForecastSectionMetadataService;
import fi.livi.digitraffic.tie.service.v3.V3VariableSignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Metadata v3", description = "Metadata for Digitraffic services (Api version 3)")
@RestController
@RequestMapping(API_V3_BASE_PATH + API_METADATA_PART_PATH)
@ConditionalOnWebApplication
public class V3MetadataController {
    private final V2ForecastSectionMetadataService v2ForecastSectionMetadataService;
    private final V3VariableSignService v3VariableSignService;
    private final TmsStationService tmsStationService;
    private final RoadStationSensorService roadStationSensorService;
    private final CameraWebService cameraWebService;
    private final WeatherStationService weatherStationService;
    private final LocationService locationService;

    @Autowired
    public V3MetadataController(final V2ForecastSectionMetadataService v2ForecastSectionMetadataService,
        final V3VariableSignService v3VariableSignService, final TmsStationService tmsStationService,
        final RoadStationSensorService roadStationSensorService, final CameraWebService cameraWebService,
        final WeatherStationService weatherStationService, final LocationService locationService) {
        this.v2ForecastSectionMetadataService = v2ForecastSectionMetadataService;
        this.v3VariableSignService = v3VariableSignService;
        this.tmsStationService = tmsStationService;
        this.roadStationSensorService = roadStationSensorService;
        this.cameraWebService = cameraWebService;
        this.weatherStationService = weatherStationService;
        this.locationService = locationService;
    }

    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTIONS_PATH, produces = { APPLICATION_JSON_VALUE,
                                                                                            APPLICATION_GEO_JSON_VALUE,
                                                                                            APPLICATION_VND_GEO_JSON_VALUE })
    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "The static information of weather forecast sections. " + API_NOTE_2023_06_01)
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Forecast Sections") })
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
    @Operation(summary = "The static information of weather forecast sections by road number. " + API_NOTE_2023_06_01)
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Forecast Sections") })
    public ForecastSectionV2FeatureCollection forecastSections(
        @PathVariable("roadNumber") final int roadNumber) {
        return v2ForecastSectionMetadataService.getForecastSectionV2Metadata(false, roadNumber, null, null,
            null, null, null);
    }

    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTIONS_PATH + "/{minLongitude}/{minLatitude}/{maxLongitude}/{maxLatitude}", produces = { APPLICATION_JSON_VALUE,
                                                                                                                                                           APPLICATION_GEO_JSON_VALUE,
                                                                                                                                                           APPLICATION_VND_GEO_JSON_VALUE })
    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "The static information of weather forecast sections by bounding box. " + API_NOTE_2023_06_01)
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Forecast Sections") })
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
    public V3VariableSignDescriptions listCodeDescriptions() {
        return new V3VariableSignDescriptions(v3VariableSignService.listVariableSignTypes());
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "The static information of TMS stations (Traffic Measurement System / LAM). " + API_NOTE_2023_06_01)
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_PATH, produces = { APPLICATION_JSON_VALUE,
                                                                                       APPLICATION_GEO_JSON_VALUE,
                                                                                       APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of TMS Station Feature Collections") })
    public TmsStationFeatureCollection tmsStations(
        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
        final boolean lastUpdated,
        @Parameter(description = "Return TMS stations of given state.", schema = @Schema(allowableValues = { "active", "removed", "all" }, defaultValue = "active"))
        @RequestParam(value = "state", required = false, defaultValue = "active")
        final String stateString) {

        final RoadStationState state = EnumConverter.parseState(RoadStationState.class, stateString);

        return tmsStationService.findAllPublishableTmsStationsAsFeatureCollection(lastUpdated, state);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "The static information of one TMS station (Traffic Measurement System / LAM). " + API_NOTE_2023_06_01)
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_TMS_NUMBER_PATH + "/{number}", produces = { APPLICATION_JSON_VALUE,
                                                                                                                APPLICATION_GEO_JSON_VALUE,
                                                                                                                APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses({     @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of TMS Station Feature Collections") })
    public TmsStationFeature tmsStationsByTmsNumber(
        @PathVariable("number") final Long tmsNumber) {
        return tmsStationService.getTmsStationByLamId(tmsNumber);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "The static information of TMS stations of given road (Traffic Measurement System / LAM). " + API_NOTE_2023_06_01)
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_ROAD_NUMBER_PATH + "/{number}", produces = { APPLICATION_JSON_VALUE,
                                                                                                                 APPLICATION_GEO_JSON_VALUE,
                                                                                                                 APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of TMS Station Feature Collections"),
    @ApiResponse(responseCode = HTTP_NOT_FOUND, description = "Road number not found", content = @Content) })
    public TmsStationFeatureCollection tmsStationsByRoadNumber(
        @PathVariable("number") final Integer roadNumber,

        @Parameter(description = "Return TMS stations of given state.", schema = @Schema(allowableValues = { "active", "removed", "all" }, defaultValue = "active"))
        @RequestParam(value = "state", required = false, defaultValue = "active")
        final String stateString) {

        final RoadStationState state = EnumConverter.parseState(RoadStationState.class, stateString);


        return tmsStationService.listTmsStationsByRoadNumber(roadNumber, state);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "The static information of one TMS station (Traffic Measurement System / LAM). " + API_NOTE_2023_06_01)
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_ROAD_STATION_ID_PATH + "/{id}", produces = { APPLICATION_JSON_VALUE,
                                                                                                                 APPLICATION_GEO_JSON_VALUE,
                                                                                                                 APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses({     @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of TMS Station Feature Collections"),
    @ApiResponse(responseCode = HTTP_NOT_FOUND, description = "Road Station not found", content = @Content) })
    public TmsStationFeature tmsStationsByRoadStationId(
        @PathVariable("id") final Long id) {
        return tmsStationService.getTmsStationByRoadStationId(id);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "The static information of available sensors of TMS stations (Traffic Measurement System / LAM). " + API_NOTE_2023_06_01)
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_AVAILABLE_SENSORS_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses({     @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of TMS Station Sensors") })
    public TmsRoadStationsSensorsMetadata tmsSensors(
        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
        final boolean lastUpdated) {
        return roadStationSensorService.findTmsRoadStationsSensorsMetadata(lastUpdated);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "The static information of weather camera presets. " + API_NOTE_2023_06_01)
    @RequestMapping(method = RequestMethod.GET, path = CAMERA_STATIONS_PATH, produces = { APPLICATION_JSON_VALUE,
                                                                                          APPLICATION_GEO_JSON_VALUE,
                                                                                          APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Camera Preset Feature Collections") })
    public CameraStationFeatureCollection cameraStations(
        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
        final boolean lastUpdated) {
        return cameraWebService.findAllPublishableCameraStationsAsFeatureCollection(lastUpdated);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "The static information of weather stations. " + API_NOTE_2023_06_01)
    @RequestMapping(method = RequestMethod.GET, path = WEATHER_STATIONS_PATH, produces = { APPLICATION_JSON_VALUE,
                                                                                           APPLICATION_GEO_JSON_VALUE,
                                                                                           APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Weather Feature Collections") })
    public WeatherStationFeatureCollection weatherStations(
        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
        final boolean lastUpdated) {
        return weatherStationService.findAllPublishableWeatherStationAsFeatureCollection(lastUpdated);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "The static information of available sensors of weather stations. " + API_NOTE_2023_06_01)
    @RequestMapping(method = RequestMethod.GET, path = WEATHER_STATIONS_AVAILABLE_SENSORS_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of Weather Station Sensors") })
    public WeatherRoadStationsSensorsMetadata weatherSensors(
        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
        final boolean lastUpdated) {
        return roadStationSensorService.findWeatherRoadStationsSensorsMetadata(lastUpdated);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "List available location versions. " + API_NOTE_2023_06_01)
    @RequestMapping(method = RequestMethod.GET, path = LOCATION_VERSIONS_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of location versions") })
    public List<LocationVersion> locationVersions () {
        return locationService.findLocationVersions();
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "The static information of locations. " + API_NOTE_2023_06_01)
    @RequestMapping(method = RequestMethod.GET, path = LOCATIONS_PATH, produces = { APPLICATION_JSON_VALUE,
                                                                                    APPLICATION_GEO_JSON_VALUE,
                                                                                    APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of locations") })
    public LocationFeatureCollection locations (
        @Parameter(description = "If parameter is given use this version.")
        @RequestParam(value = "version", required = false, defaultValue = LATEST)
        final String version,

        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
        final boolean lastUpdated) {

        return locationService.findLocationsMetadata(lastUpdated, version);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "The static information of location types and locationsubtypes. " + API_NOTE_2023_06_01)
    @RequestMapping(method = RequestMethod.GET, path = LOCATION_TYPES_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of location types and location subtypes") })
    public LocationTypesMetadata locationTypes(
        @Parameter(description = "If parameter is given use this version.")
        @RequestParam(value = "version", required = false, defaultValue = LATEST)
        final String version,

        @Parameter(description = "If parameter is given result will only contain update status.")
        @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
        final boolean lastUpdated) {
        return locationService.findLocationSubtypes(lastUpdated, version);
    }

    @Deprecated(forRemoval = true)
    @Sunset(date = ApiDeprecations.SUNSET_2023_06_01)
    @Operation(summary = "The static information of one location. " + API_NOTE_2023_06_01)
    @RequestMapping(method = RequestMethod.GET, path = LOCATIONS_PATH + "/{id}", produces = { APPLICATION_JSON_VALUE,
                                                                                              APPLICATION_GEO_JSON_VALUE,
                                                                                              APPLICATION_VND_GEO_JSON_VALUE })
    @ApiResponses({ @ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of location") })
    public LocationFeatureCollection locationsById(
        @Parameter(description = "If parameter is given use this version.")
        @RequestParam(value = "version", required = false, defaultValue = LATEST)
        final String version,

        @PathVariable("id") final int id) {
        return locationService.findLocation(id, version);
    }

}
