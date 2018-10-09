package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.conf.RoadApplicationConfiguration.API_UPLOAD_PART_PATH;
import static fi.livi.digitraffic.tie.conf.RoadApplicationConfiguration.API_V1_BASE_PATH;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.data.service.CameraDataService;
import fi.livi.digitraffic.tie.data.service.Datex2DataService;
import fi.livi.digitraffic.tie.data.service.ForecastSectionDataService;
import fi.livi.digitraffic.tie.data.service.FreeFlowSpeedService;
import fi.livi.digitraffic.tie.data.service.TmsDataService;
import fi.livi.digitraffic.tie.data.service.WeatherService;
import fi.livi.digitraffic.tie.harja.TyokoneenseurannanKirjausRequestSchema;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/*
 * REST/JSON replacement api for Digitraffic SOAP-api
 *
 */
@Api(tags = "upload", description = "Upload data to Digitraffic service")
@RestController
@Validated
@RequestMapping(API_V1_BASE_PATH + API_UPLOAD_PART_PATH + "/harja")
@ConditionalOnWebApplication
public class UploadController {

    public static final String HARJA_PATH = "/harja";
    public static final String HARJA_SEURANTA_TYOKONE_PATH = HARJA_PATH + "/seuranta/tyokone";

    private final TmsDataService tmsDataService;
    private final FreeFlowSpeedService freeFlowSpeedService;
    private final WeatherService weatherService;
    private final CameraDataService cameraDataService;
    private final ForecastSectionDataService forecastSectionDataService;
    private final Datex2DataService datex2DataService;

    @Autowired
    public UploadController(final TmsDataService tmsDataService,
                            final FreeFlowSpeedService freeFlowSpeedService,
                            final WeatherService weatherService,
                            final CameraDataService cameraDataService,
                            final ForecastSectionDataService forecastSectionDataService,
                            final Datex2DataService datex2DataService) {
        this.tmsDataService = tmsDataService;
        this.freeFlowSpeedService = freeFlowSpeedService;
        this.weatherService = weatherService;
        this.cameraDataService = cameraDataService;
        this.forecastSectionDataService = forecastSectionDataService;
        this.datex2DataService = datex2DataService;
    }



    @ApiOperation("Posting real-time tracking information for a work machine from HARJA")
    @RequestMapping(method = RequestMethod.POST, path = HARJA_SEURANTA_TYOKONE_PATH)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of real-time tracking information for a work machine from HARJA"))
    public ResponseEntity<Void> postHarjaSeuranta(@RequestBody TyokoneenseurannanKirjausRequestSchema tyokoneenseurannanKirjaus, HttpServletResponse response) {

        //System.out.println(ToStringHelper.toStringFull(tyokoneenseurannanKirjaus));
        System.out.println(tyokoneenseurannanKirjaus);

        return ResponseEntity.ok().build();
    }


}
