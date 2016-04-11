package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_V1_BASE_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.data.model.LamDataObject;
import fi.livi.digitraffic.tie.data.service.LamDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="Lam data", description="Api to read latest lam measurements")
@RestController
@RequestMapping(API_V1_BASE_PATH + API_DATA_PART_PATH)
public class LamDataController {
    public static final String PATH = "/lam-data";

    private final LamDataService lamDataService;

    @Autowired
    public LamDataController(final LamDataService lamDataService) {
        this.lamDataService = lamDataService;
    }

    @ApiOperation(value = "List all lam measurements", notes = "List all lam measurements")
    @RequestMapping(method = RequestMethod.GET, path = PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    public LamDataObject listAllLamData() {
        return lamDataService.listAllLamDataFromNonObsoleteStations();
    }

}
