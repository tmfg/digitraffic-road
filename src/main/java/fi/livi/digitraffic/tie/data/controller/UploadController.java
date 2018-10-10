package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.conf.RoadApplicationConfiguration.API_UPLOAD_PART_PATH;
import static fi.livi.digitraffic.tie.conf.RoadApplicationConfiguration.API_V1_BASE_PATH;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.harja.TyokoneenseurannanKirjausRequestSchema;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "upload", description = "Upload data to Digitraffic service")
@RestController
@Validated
@RequestMapping(API_V1_BASE_PATH + API_UPLOAD_PART_PATH)
@ConditionalOnWebApplication
public class UploadController {

    private static final Logger log = LoggerFactory.getLogger(UploadController.class);

    public static final String HARJA_PATH = "/harja";
    public static final String HARJA_SEURANTA_TYOKONE_PATH = HARJA_PATH + "/seuranta/tyokone";

    private final ObjectMapper objectMapper;

    @Autowired
    public UploadController(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }



    @ApiOperation("Posting real-time tracking information for a work machine from HARJA")
    @RequestMapping(method = RequestMethod.POST, path = HARJA_SEURANTA_TYOKONE_PATH)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of real-time tracking information for a work machine from HARJA"))
    public ResponseEntity<Void> postHarjaSeuranta(@RequestBody TyokoneenseurannanKirjausRequestSchema tyokoneenseurannanKirjaus, HttpServletResponse response)
        throws JsonProcessingException {

        log.info("Received JSON:\n{}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tyokoneenseurannanKirjaus));

        return ResponseEntity.ok().build();
    }


}
