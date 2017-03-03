package fi.livi.digitraffic.tie.data.service.traveltime;

import static org.junit.Assert.assertNotNull;

import java.time.ZonedDateTime;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.data.service.traveltime.dto.TravelTimeMediansDto;

public class TravelTimeIntegrationTest extends AbstractTest {

    @Autowired
    private TravelTimeClient travelTimeClient;

    @Test
    @Ignore("Needs username and password")
    public void getMediansSucceeds() {

        TravelTimeMediansDto medians = travelTimeClient.getMedians(ZonedDateTime.now().minusHours(2));

        assertNotNull(medians);
    }
}
