package fi.livi.digitraffic.tie.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.dto.WazeFeedAnnouncementDto;
import fi.livi.digitraffic.tie.service.WazeFeedService;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@Validated
@RequestMapping(ApiPaths.API_INTEGRATIONS_BASE_PATH)
@ApiIgnore
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix="dt.waze", name="enabled", havingValue="true")
public class WazeFeedController {
    private final WazeFeedService wazeFeedService;

    public WazeFeedController(final WazeFeedService wazeFeedService) {
        this.wazeFeedService = wazeFeedService;
    }

    @ApiOperation(value = "Traffic incident announcements for Waze")
    @RequestMapping(method = RequestMethod.GET, path = ApiPaths.WAZE_INCIDENT_PATH, produces = { APPLICATION_JSON_VALUE })
    public WazeFeedAnnouncementDto wazeFeedAnnouncement() {
        return wazeFeedService.findActive();
    }
}