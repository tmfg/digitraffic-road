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
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value="Roadstation sensor metadata", description="Api to read roadstation sensor metadata")
@RestController
@RequestMapping(API_V1_BASE_PATH + API_METADATA_PART_PATH)
public class RoadStationSensorMetadataController {
    public static final String PATH = "/road-station-sensors";

    private final RoadStationSensorService roadStationSensorService;

    @Autowired
    public RoadStationSensorMetadataController(final RoadStationSensorService roadStationSensorService) {
        this.roadStationSensorService = roadStationSensorService;
    }

    @ApiOperation("List all roadstation sensors")
    @RequestMapping(method = RequestMethod.GET, path = PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful retrieval of Road Station Sensors"),
                            @ApiResponse(code = 500, message = "Internal server error") })
    public List<RoadStationSensor> listNonObsoleteRoadStationSensors() {
        return roadStationSensorService.findAllNonObsoleteRoadStationSensors();
    }
}
