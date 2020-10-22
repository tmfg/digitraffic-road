package fi.livi.digitraffic.tie.service.v2.maintenance;

import java.io.IOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;

import fi.livi.digitraffic.tie.AbstractServiceTest;

@Import({ V2MaintenanceTrackingUpdateService.class, JacksonAutoConfiguration.class, V2MaintenanceTrackingServiceTestHelper.class })
public class V2MaintenanceTrackingUpdateServiceInternalTest extends AbstractServiceTest {

    private static final Logger log = LoggerFactory.getLogger(V2MaintenanceTrackingUpdateServiceInternalTest.class);

    @Autowired
    private V2MaintenanceTrackingUpdateService v2MaintenanceTrackingUpdateService;

    @Autowired
    private V2MaintenanceTrackingServiceTestHelper testHelper;

    @Before
    public void cleanDb() {
        testHelper.clearDb();
    }

    @Ignore("Just for internal testing")
    @Rollback(false)
    @Test
    public void handleUnhandledMaintenanceTrackingData() {
        final int count = v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(1000);
        log.info("Handled {} trackings", count);
    }

    @Ignore("Just for internal testing")
    @Rollback(false)
    @Test
    public void longJumpInLineStringData() throws IOException {
        testHelper.saveTrackingFromResourceToDb("classpath:harja/service/distancegap/long-jump-1.json");
        testHelper.saveTrackingFromResourceToDb("classpath:harja/service/distancegap/long-jump-2.json");
        testHelper.saveTrackingFromResourceToDb("classpath:harja/service/distancegap/long-jump-3.json");
        testHelper.saveTrackingFromResourceToDb("classpath:harja/service/distancegap/long-jump-4.json");

        log.info("Handled count={}", v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(100));
    }
}
