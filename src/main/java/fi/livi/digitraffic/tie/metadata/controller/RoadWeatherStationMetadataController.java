package fi.livi.digitraffic.tie.metadata.controller;

import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_METADATA_PART_PATH;
import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_V1_BASE_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.metadata.geojson.roadweather.RoadWeatherStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.service.roadweather.RoadWeatherStationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="Lam metadata", description="Api to read lam metadata")
@RestController
@RequestMapping(API_V1_BASE_PATH + API_METADATA_PART_PATH)
public class RoadWeatherStationMetadataController {

    private final RoadWeatherStationService roadWeatherStationService;

    @Autowired
    public RoadWeatherStationMetadataController(final RoadWeatherStationService roadWeatherStationService) {
        this.roadWeatherStationService = roadWeatherStationService;
    }

    @ApiOperation(value = "List all road weather stations", notes = "List all road weather stations")
    @RequestMapping(method = RequestMethod.GET, path = "/road-weather-stations", produces = APPLICATION_JSON_UTF8_VALUE)
    public RoadWeatherStationFeatureCollection listNonObsoleteRoadWeatherStations() {
        return roadWeatherStationService.findAllNonObsoleteRoadWeatherStationAsFeatureCollection();
    }
}
