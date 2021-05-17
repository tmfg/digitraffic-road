package fi.livi.digitraffic.tie.service.v3.maintenance;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import fi.livi.digitraffic.tie.AbstractServiceTest;

public class V3MaintenanceTrackingUpdateServiceIIntegrationTest extends AbstractServiceTest {

    private static final Logger log = LoggerFactory.getLogger(V3MaintenanceTrackingUpdateServiceIIntegrationTest.class);

    @Autowired
    private V3MaintenanceTrackingServiceTestHelper testHelper;

//    @Before
//    public void init() {
//        testHelper.clearDb();
//    }

    @Disabled("Just for internal testing")
    @Rollback(value = false)
    @Test
    public void loadBigTrackingData() throws IOException {
        testHelper.saveTrackingFromResourceToDbAsObservations("classpath:harja/internal-testing/seuranta-big.json");
    }

    @Disabled("Just for internal testing")
    @Rollback(value = false)
    @Test
    public void loadLongJumpInLineStringData() throws IOException {
        testHelper.saveTrackingFromResourceToDbAsObservations("classpath:harja/service/distancegap/long-jump-twice-1.json");
    }

    @Disabled("Just for internal testing")
    @Rollback(value = false)
    @Test
    public void loadArrayOfTrackingsData() throws IOException {
        testHelper.clearDb();
        testHelper.saveTrackingFromResourceToDbAsObservationsFromMultipleMessages("classpath:harja/internal-testing/tracking-array-to-combine.json");
    }

    @Disabled("Just for internal testing")
    @Rollback(value = false)
    @Test
    public void handleUnhandledObservationsData() {
        testHelper.handleUnhandledWorkMachineObservations(1000);
    }


    @Disabled("Just for internal testing")
    @Rollback(value = false)
    @Test
    public void loadLineStrings_ShouldBeHandledAsOneIfNoDistanceGap() throws IOException {
        testHelper.saveTrackingFromResourceToDbAsObservations("classpath:harja/service/linestring/linestring-first.json");
        testHelper.saveTrackingFromResourceToDbAsObservations("classpath:harja/service/linestring/linestring-second.json");
    }

    @Disabled("Just for internal testing")
    @Rollback(value = false)
    @Test
    public void loadSinglePointLineStrings_ShouldBeHandledAsLineStringTrackings() throws IOException {
        testHelper.saveTrackingFromResourceToDbAsObservations("classpath:harja/service/linestring/point-linestring-1.json");
        testHelper.saveTrackingFromResourceToDbAsObservations("classpath:harja/service/linestring/point-linestring-2.json");
        testHelper.saveTrackingFromResourceToDbAsObservations("classpath:harja/service/linestring/point-linestring-3.json");
    }
}
