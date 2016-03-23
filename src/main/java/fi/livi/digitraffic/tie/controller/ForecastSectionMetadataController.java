package fi.livi.digitraffic.tie.controller;

import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_METADATA_PART_PATH;
import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_V1_BASE_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.model.ForecastSection;
import fi.livi.digitraffic.tie.service.forecastsection.ForecastSectionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="Forecast section metadata", description="Api to read forecast section metadata")
@RestController
@RequestMapping(API_V1_BASE_PATH + API_METADATA_PART_PATH)
public class ForecastSectionMetadataController {
    private final ForecastSectionService forecastSectionService;

    @Autowired
    public ForecastSectionMetadataController(final ForecastSectionService forecastSectionService) {
        this.forecastSectionService = forecastSectionService;
    }

    @ApiOperation("List all roadstation sensors.")
    @RequestMapping(method = RequestMethod.GET, path = "/forecast-sections", produces = APPLICATION_JSON_UTF8_VALUE)
    public List<ForecastSection> listaForecastSections() {
        return forecastSectionService.findAllForecastSections();
    }

}
