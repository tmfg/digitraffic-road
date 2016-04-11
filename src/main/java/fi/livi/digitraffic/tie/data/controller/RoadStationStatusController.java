package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_V1_BASE_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.data.service.RoadStationStatusService;
import fi.livi.digitraffic.tie.metadata.model.RoadStationStatuses;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value="Roadstation status metadata", description="Api to read roadstation status metadata")
@RestController
@RequestMapping(API_V1_BASE_PATH + API_DATA_PART_PATH)
public class RoadStationStatusController {
    public static final String PATH = "/road-station-statuses";

    private final RoadStationStatusService roadStationStatusService;

    @Autowired
    public RoadStationStatusController(RoadStationStatusService roadStationStatusService) {
        this.roadStationStatusService = roadStationStatusService;
    }

    @ApiOperation("List all roadstation statuses.")
    @RequestMapping(method = RequestMethod.GET, path = PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful retrieval of Road Station Statuses"),
                            @ApiResponse(code = 500, message = "Internal server error") })
    public RoadStationStatuses listNonObsoleteRoadStationSensors() {
        return roadStationStatusService.findAllRoadStationStatuses();
    }
}
