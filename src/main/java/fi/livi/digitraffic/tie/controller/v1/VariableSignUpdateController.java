package fi.livi.digitraffic.tie.controller.v1;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V1_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_VARIABLE_SIGN_UPDATE_PART_PATH;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_OK;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.external.tloik.variablesigns.Metatiedot;
import fi.livi.digitraffic.tie.external.tloik.variablesigns.Tilatiedot;
import fi.livi.digitraffic.tie.service.v1.VariableSignUpdateService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Hidden
@Tag(name = "Variable speed limits")
@RestController
@Validated
@RequestMapping(API_V1_BASE_PATH + API_VARIABLE_SIGN_UPDATE_PART_PATH)
@ConditionalOnWebApplication
public class VariableSignUpdateController {
    public static final String METADATA_PATH = "/metadata";
    public static final String DATA_PATH = "/data";

    private final VariableSignUpdateService variableSignUpdateService;

    public VariableSignUpdateController(final VariableSignUpdateService variableSignUpdateService) {
        this.variableSignUpdateService = variableSignUpdateService;
    }

    @Operation(summary = "Posting variable sign metadata from TLOIK")
    @RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT}, path = METADATA_PATH, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful post of variable sign metadata from TLOIK"))
    public ResponseEntity<Void> postVariableSignMetadata(@RequestBody Metatiedot metadata) {
        variableSignUpdateService.saveMetadata(metadata);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Posting variable sign data from TLOIK")
    @RequestMapping(method = { RequestMethod.POST, RequestMethod.PUT}, path = DATA_PATH, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful post of variable sign data from TLOIK"))
    public ResponseEntity<Void> postTrafficSignData(@RequestBody Tilatiedot data) {
        variableSignUpdateService.saveData(data);

        return ResponseEntity.ok().build();
    }

}
