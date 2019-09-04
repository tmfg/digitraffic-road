package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.conf.RoadWebApplicationConfiguration.API_V1_BASE_PATH;
import static fi.livi.digitraffic.tie.conf.RoadWebApplicationConfiguration.API_TRAFFIC_SIGNS_PART_PATH;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.data.dto.trafficsigns.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.livi.digitraffic.tie.data.service.TrafficSignsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "variable speed limits")
@RestController
@Validated
@RequestMapping(API_V1_BASE_PATH + API_TRAFFIC_SIGNS_PART_PATH)
@ConditionalOnWebApplication
public class TrafficSignsController {
    private static final Logger log = LoggerFactory.getLogger(TrafficSignsController.class);

    public static final String METADATA_PATH = "/metadata";
    public static final String DATA_PATH = "/data";

    private final TrafficSignsService trafficSignsService;

    public TrafficSignsController(final TrafficSignsService trafficSignsService) {
        this.trafficSignsService = trafficSignsService;
    }

    @ApiOperation("Posting variable speed limits from HARJA")
    @RequestMapping(method = RequestMethod.POST, path = METADATA_PATH, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful post of traffic signs metadata from TLOIK"))
    public ResponseEntity<Void> postTrafficSignsMetadata(@RequestBody MetadataSchema metadata) throws JsonProcessingException {
        trafficSignsService.saveMetadata(metadata);

        return ResponseEntity.ok().build();
    }

    @ApiOperation("Posting variable speed limits from HARJA")
    @RequestMapping(method = RequestMethod.POST, path = DATA_PATH, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful post of traffic signs data from TLOIK"))
    public ResponseEntity<Void> postTrafficSignsData(@RequestBody DataSchema data) throws JsonProcessingException {
        trafficSignsService.saveData(data);

        return ResponseEntity.ok().build();
    }

}
