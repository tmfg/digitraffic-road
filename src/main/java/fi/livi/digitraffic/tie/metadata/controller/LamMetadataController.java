package fi.livi.digitraffic.tie.metadata.controller;

import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_METADATA_PART_PATH;
import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_V1_BASE_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import fi.livi.digitraffic.tie.geojson.lamstation.LamStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.service.lam.LamStationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(value="Lam metadata", description="Api to read lam metadata")
@RestController
@RequestMapping(API_V1_BASE_PATH + API_METADATA_PART_PATH)
public class LamMetadataController {
    private final LamStationService lamStationService;

    @Autowired
    public LamMetadataController(final LamStationService lamStationService) {
        this.lamStationService = lamStationService;
    }

    @ApiOperation(value = "List all lam stations", notes = "List all lam stations")
    @RequestMapping(method = RequestMethod.GET, path = "/lam-stations", produces = APPLICATION_JSON_UTF8_VALUE)
    public LamStationFeatureCollection listNonObsoleteLamStations() {
        return lamStationService.findAllNonObsoleteLamStationsAsFeatureCollection();
    }
}
