package fi.livi.digitraffic.tie.controller.beta;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_BETA_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_JSON_VALUE;
import static fi.livi.digitraffic.tie.controller.DtMediaType.APPLICATION_XML_VALUE;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_BAD_REQUEST;
import static fi.livi.digitraffic.tie.controller.HttpCodeConstants.HTTP_OK;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.controller.RoadStationState;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.dto.WeatherSensorValueHistoryDto;
import fi.livi.digitraffic.tie.service.v1.TmsDataDatex2Service;
import fi.livi.digitraffic.tie.service.v1.WeatherService;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationDatex2Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Beta")
@RestController
@Validated
@RequestMapping(API_BETA_BASE_PATH)
@ConditionalOnWebApplication
public class BetaController {
    public static final String TMS_STATIONS_DATEX2_PATH = "/tms-stations-datex2";
    public static final String TMS_DATA_DATEX2_PATH = "/tms-data-datex2";
    public static final String WEATHER_HISTORY_DATA_PATH = "/weather-history-data";

    private final TmsStationDatex2Service tmsStationDatex2Service;
    private final TmsDataDatex2Service tmsDataDatex2Service;
    private final WeatherService weatherService;

    @Autowired
    public BetaController(final TmsStationDatex2Service tmsStationDatex2Service,
                          final TmsDataDatex2Service tmsDataDatex2Service,
                          final WeatherService weatherService) {
        this.tmsStationDatex2Service = tmsStationDatex2Service;
        this.tmsDataDatex2Service = tmsDataDatex2Service;
        this.weatherService = weatherService;
    }

    @Operation(summary = "The static information of TMS stations in Datex2 format (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE })
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of TMS Stations Datex2 metadata"))
    public D2LogicalModel tmsStationsDatex2(
        @Parameter(description = "Return TMS stations of given state.")
        @RequestParam(value = "state", required = false, defaultValue = "active")
        final RoadStationState roadStationState) {

        return tmsStationDatex2Service.findAllPublishableTmsStationsAsDatex2(roadStationState);
    }

    @Operation(summary = "Current data of TMS Stations in Datex2 format (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_DATA_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE })
    @ApiResponses(@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of TMS Stations Datex2 data"))
    public D2LogicalModel tmsDataDatex2() {
        return tmsDataDatex2Service.findPublishableTmsDataDatex2();
    }

    @Operation(summary = "List the history of sensor values from the weather road station")
    @RequestMapping(method = RequestMethod.GET, path = WEATHER_HISTORY_DATA_PATH + "/{stationId}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses({@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of weather station data"),
                   @ApiResponse(responseCode = HTTP_BAD_REQUEST, description = "Invalid parameter(s)", content = @Content)})
    public List<WeatherSensorValueHistoryDto> weatherDataHistory(
        @Parameter(description = "Weather station id", required = true)
        @PathVariable
        final long stationId,

        @Parameter(description = "Fetch history after given date time")
        @RequestParam(value="from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime from,

        @Parameter(description = "Limit history to given date time")
        @RequestParam(value="to", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime to) {

        return weatherService.findWeatherHistoryData(stationId, from, to);
    }

    @Operation(summary = "List the history of sensor value from the weather road station")
    @RequestMapping(method = RequestMethod.GET, path = WEATHER_HISTORY_DATA_PATH + "/{stationId}/{sensorId}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses({@ApiResponse(responseCode = HTTP_OK, description = "Successful retrieval of weather station data"),
                  @ApiResponse(responseCode = HTTP_BAD_REQUEST, description = "Invalid parameter", content = @Content)})
    public List<WeatherSensorValueHistoryDto> weatherDataHistory(
        @Parameter(description = "Weather Station id", required = true)
        @PathVariable final long stationId,

        @Parameter(description = "Sensor id", required = true)
        @PathVariable final long sensorId,

        @Parameter(description = "Fetch history after given time")
        @RequestParam(value="from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        final ZonedDateTime from) {

        return weatherService.findWeatherHistoryData(stationId, sensorId, from);
    }
}
