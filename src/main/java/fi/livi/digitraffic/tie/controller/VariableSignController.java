package fi.livi.digitraffic.tie.controller;

import fi.livi.digitraffic.tie.dto.v1.VariableSignDescriptions;
import fi.livi.digitraffic.tie.dto.v1.trafficsigns.TrafficSignHistory;
import fi.livi.digitraffic.tie.metadata.geojson.variablesigns.VariableSignFeatureCollection;
import fi.livi.digitraffic.tie.service.v2.V2VariableSignDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static fi.livi.digitraffic.tie.controller.ApiConstants.*;
import static fi.livi.digitraffic.tie.controller.ApiPaths.VARIABLE_SIGNS_CODE_DESCRIPTIONS;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_OK;

@Tag(name = VARIABLE_SIGN_V1_TAG)
@RestController
@Validated
@ConditionalOnWebApplication
public class VariableSignController {
    private final V2VariableSignDataService v2VariableSignDataService;

    public VariableSignController(final V2VariableSignDataService v2VariableSignDataService) {
        this.v2VariableSignDataService = v2VariableSignDataService;
    }

    @Operation(summary = "Return the latest data of variable signs. If deviceId is given, latest data for that sign will be returned, otherwise return the latest data for each sign from the last 7 days.")
    @RequestMapping(method = RequestMethod.GET, path = API_VS_V1 + API_SIGNS, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of variable sign data"))
    public VariableSignFeatureCollection variableSigns(
        @Parameter(description = "If parameter is given list only latest value of given sign")
        @RequestParam(value = "deviceId", required = false)
        final String deviceId) {
        if(deviceId != null) {
            return v2VariableSignDataService.listLatestValue(deviceId);
        } else {
            return v2VariableSignDataService.listLatestValues();
        }
    }

    @Operation(summary = "Return the latest value of a variable sign")
    @RequestMapping(method = RequestMethod.GET, path = API_VS_V1 + API_SIGNS + "/{deviceId}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of variable sign data"))
    public VariableSignFeatureCollection variableSignByPath(@PathVariable("deviceId") final String deviceId) {
        return v2VariableSignDataService.listLatestValue(deviceId);
    }

    @Operation(summary = "Return the history of variable sign data")
    @RequestMapping(method = RequestMethod.GET, path = API_VS_V1 + API_SIGNS_HISTORY, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of variable sign history"))
    public List<TrafficSignHistory> variableSignHistory(
        @Parameter(description = "List history data of given sign")
        @RequestParam(value = "deviceId")
        final String deviceId) {
        return v2VariableSignDataService.listVariableSignHistory(deviceId);
    }

    @Operation(summary = "Return all code descriptions.")
    @GetMapping(path = API_VS_V1 + API_SIGNS_CODE_DESCRIPTIONS, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public VariableSignDescriptions listCodeDescriptions() {
        return new VariableSignDescriptions(v2VariableSignDataService.listVariableSignTypes());
    }
}
