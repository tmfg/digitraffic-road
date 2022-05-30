package fi.livi.digitraffic.tie.controller;

import static fi.livi.digitraffic.tie.controller.ApiConstants.API_WAZEFEED;
import static fi.livi.digitraffic.tie.controller.ApiConstants.V1;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedAnnouncementDto;
import fi.livi.digitraffic.tie.service.WazeFeedService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@Validated
@Hidden
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix="dt.waze", name="enabled", havingValue="true")
public class WazeFeedController {
    private static final String API_WAZE_V1 = API_WAZEFEED + V1;
    private static final String FEED = "/feed";
    private static final String API_WAZE_V1_FEED = API_WAZE_V1 + FEED;

    private final WazeFeedService wazeFeedService;

    public WazeFeedController(final WazeFeedService wazeFeedService) {
        this.wazeFeedService = wazeFeedService;
    }

    @Operation(summary = "Traffic incident announcements for Waze")
    @RequestMapping(method = RequestMethod.GET, path = API_WAZE_V1_FEED, produces = { APPLICATION_JSON_VALUE })
    public WazeFeedAnnouncementDto wazeFeedAnnouncement() {
        return wazeFeedService.findActive();
    }
}