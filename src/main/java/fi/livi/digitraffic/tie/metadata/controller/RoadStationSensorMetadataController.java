package fi.livi.digitraffic.tie.metadata.controller;

import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_METADATA_PART_PATH;
import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_V1_BASE_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="Roadstation sensor metadata", description="Api to read roadstation sensor metadata")
@RestController
@RequestMapping(API_V1_BASE_PATH + API_METADATA_PART_PATH)
public class RoadStationSensorMetadataController {
    private final RoadStationSensorService roadStationSensorService;

    @Autowired
    public RoadStationSensorMetadataController(final RoadStationSensorService roadStationSensorService) {
        this.roadStationSensorService = roadStationSensorService;
    }

    @ApiOperation("List all roadstation sensors.")
    @RequestMapping(method = RequestMethod.GET, path = "/road-station-sensors", produces = APPLICATION_JSON_UTF8_VALUE)
    public List<RoadStationSensor> listNonObsoleteRoadStationSensors() {
        return roadStationSensorService.findAllNonObsoleteRoadStationSensors();
    }
}
