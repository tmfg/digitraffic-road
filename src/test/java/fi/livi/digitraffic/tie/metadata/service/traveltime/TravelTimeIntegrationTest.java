package fi.livi.digitraffic.tie.metadata.service.traveltime;

import static org.junit.Assert.assertNotNull;

import java.time.ZonedDateTime;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.base.MetadataTestBase;

public class TravelTimeIntegrationTest extends MetadataTestBase {

    @Autowired
    private TravelTimeClient travelTimeClient;

    @Test
    public void getMediansSucceeds() {

        TravelTimeMediansDto medians = travelTimeClient.getMedians(ZonedDateTime.now().minusHours(2));

        assertNotNull(medians);
    }
}
