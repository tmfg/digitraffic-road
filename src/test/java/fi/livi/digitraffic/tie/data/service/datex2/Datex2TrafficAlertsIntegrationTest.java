package fi.livi.digitraffic.tie.data.service.datex2;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import fi.livi.digitraffic.tie.AbstractTest;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration
@TestPropertySource(properties = "datex2.traffic.alerts.url=https://ava.liikennevirasto.fi/incidents/datex2/")
public class Datex2TrafficAlertsIntegrationTest extends AbstractTest {
    @Autowired
    private Datex2TrafficAlertMessageUpdater datex2TrafficAlertMessageUpdater;

    @Test
    @Ignore("For manual integration testing")
    public void updateTrafficAlertMessages() {
        datex2TrafficAlertMessageUpdater.updateDatex2TrafficAlertMessages();
    }
}
