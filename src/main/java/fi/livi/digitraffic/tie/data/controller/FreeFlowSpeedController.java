package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_V1_BASE_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.data.model.FreeFlowSpeedObject;
import fi.livi.digitraffic.tie.data.service.FreeFlowSpeedService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="Free flow speeds", description="Api to read free flow speeds")
@RestController
@RequestMapping(API_V1_BASE_PATH + API_DATA_PART_PATH)
public class FreeFlowSpeedController {
    private final FreeFlowSpeedService freeFlowSpeedService;

    @Autowired
    public FreeFlowSpeedController(final FreeFlowSpeedService freeFlowSpeedService) {
        this.freeFlowSpeedService = freeFlowSpeedService;
    }

    @ApiOperation("List all free flow speeds")
    @RequestMapping(method = RequestMethod.GET, path = "/freeFlowSpeeds", produces = APPLICATION_JSON_UTF8_VALUE)
    public FreeFlowSpeedObject listFreeFlowSpeeds() {
        return freeFlowSpeedService.listAllFreeFlowSpeeds();
    }

}
