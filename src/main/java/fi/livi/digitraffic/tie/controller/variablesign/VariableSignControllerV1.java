package fi.livi.digitraffic.tie.controller.variablesign;

import static fi.livi.digitraffic.tie.controller.ApiConstants.API_SIGNS;
import static fi.livi.digitraffic.tie.controller.ApiConstants.API_SIGNS_CODE_DESCRIPTIONS;
import static fi.livi.digitraffic.tie.controller.ApiConstants.API_SIGNS_HISTORY;
import static fi.livi.digitraffic.tie.controller.ApiConstants.API_VS_V1;
import static fi.livi.digitraffic.tie.controller.ApiConstants.VARIABLE_SIGN_TAG_V1;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_OK;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.controller.ResponseEntityWithLastModifiedHeader;
import fi.livi.digitraffic.tie.dto.v1.VariableSignDescriptions;
import fi.livi.digitraffic.tie.dto.variablesigns.v1.TrafficSignHistoryV1;
import fi.livi.digitraffic.tie.dto.variablesigns.v1.VariableSignFeatureCollectionV1;
import fi.livi.digitraffic.tie.service.variablesign.v1.VariableSignDataServiceV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = VARIABLE_SIGN_TAG_V1)
@RestController
@Validated
@ConditionalOnWebApplication
public class VariableSignControllerV1 {
    private final VariableSignDataServiceV1 variableSignDataServiceV1;

    public VariableSignControllerV1(final VariableSignDataServiceV1 variableSignDataServiceV1) {
        this.variableSignDataServiceV1 = variableSignDataServiceV1;
    }

    @Operation(summary = "Return the latest data of variable signs. If deviceId is given, latest data for that sign will be returned, otherwise return the latest data for each sign from the last 7 days.")
    @RequestMapping(method = RequestMethod.GET, path = API_VS_V1 + API_SIGNS, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of variable sign data"))
    public VariableSignFeatureCollectionV1 variableSigns(
        @Parameter(description = "If parameter is given list only latest value of given sign")
        @RequestParam(value = "deviceId", required = false)
        final String deviceId) {
        if(deviceId != null) {
            return variableSignDataServiceV1.listLatestValue(deviceId);
        } else {
            return variableSignDataServiceV1.listLatestValues();
        }
    }

    @Operation(summary = "Return the latest value of a variable sign")
    @RequestMapping(method = RequestMethod.GET, path = API_VS_V1 + API_SIGNS + "/{deviceId}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of variable sign data"))
    public VariableSignFeatureCollectionV1 variableSignByPath(@PathVariable("deviceId") final String deviceId) {
        return variableSignDataServiceV1.listLatestValue(deviceId);
    }

    @Operation(summary = "Return the history of variable sign data")
    @RequestMapping(method = RequestMethod.GET, path = API_VS_V1 + API_SIGNS_HISTORY, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of variable sign history"))
    public ResponseEntityWithLastModifiedHeader<List<TrafficSignHistoryV1>> variableSignHistory(
        @Parameter(description = "Id of sign.")
        @RequestParam(value = "deviceId")
        final String deviceId,
        @Parameter(description = "When a date is given, return only history for that day.  This is date of UTC-0 time.")
        @RequestParam(value = "date", required = false)
        @DateTimeFormat(iso = DATE)
        final Date date) {
        final List<TrafficSignHistoryV1> history = variableSignDataServiceV1.listVariableSignHistory(date, deviceId);
        final Instant lastModified = history.stream().map(TrafficSignHistoryV1::getCreated).max(Comparator.naturalOrder()).orElse(Instant.EPOCH);
        return ResponseEntityWithLastModifiedHeader.of(history, lastModified, API_VS_V1 + API_SIGNS_HISTORY + "?deviceId=" + deviceId);
    }

    @Operation(summary = "Return all code descriptions.")
    @GetMapping(path = API_VS_V1 + API_SIGNS_CODE_DESCRIPTIONS, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public VariableSignDescriptions getCodeDescriptions() {
        return variableSignDataServiceV1.getCodeDescriptions();
    }
}
