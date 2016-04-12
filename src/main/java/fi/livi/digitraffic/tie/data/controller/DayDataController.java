package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_V1_BASE_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.data.model.daydata.HistoryData;
import fi.livi.digitraffic.tie.data.service.DayDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="History data for previous day", description="History data for previous day")
@RestController
@RequestMapping(API_V1_BASE_PATH + API_DATA_PART_PATH)
public class DayDataController {
    public static final String PATH = "/day-data";

    private final DayDataService dayDataService;

    @Autowired
    public DayDataController(DayDataService dayDataService) {
        this.dayDataService = dayDataService;
    }

    @ApiOperation("List history data for previous day")
    @RequestMapping(method = RequestMethod.GET, path = PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    public HistoryData listPreviousDayHistoryData() {
        return dayDataService.listPreviousDayHistoryData();
    }
}
