package fi.livi.digitraffic.tie.controller;

import fi.livi.digitraffic.tie.service.LamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.geojson.FeatureCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(value="Lam metadata", description="Api to read lam metadata")
@RestController
public class LamMetadataController extends AbstractMetadataController {

    @Autowired
    private LamService lamService;

    @ApiOperation(value = "List all lam stations.")
    @RequestMapping(method = RequestMethod.GET, path = API_V1_PATH + "/lam-stations", produces = MediaType.APPLICATION_JSON_VALUE)
    public FeatureCollection listNonObsoleteLamStations() {
        return lamService.findAllNonObsoleteLamStationsAsFeatureCollection();
    }
}
