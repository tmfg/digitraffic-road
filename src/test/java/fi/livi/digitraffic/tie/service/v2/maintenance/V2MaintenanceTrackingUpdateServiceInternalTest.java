package fi.livi.digitraffic.tie.service.v2.maintenance;

import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper.RANGE_X_MAX;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper.RANGE_X_MIN;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper.RANGE_Y_MAX;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper.RANGE_Y_MIN;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.TestTransaction;

import fi.livi.digitraffic.tie.AbstractServiceTest;

@Import({ V2MaintenanceTrackingUpdateService.class, V2MaintenanceTrackingDataService.class, JacksonAutoConfiguration.class, V2MaintenanceTrackingServiceTestHelper.class })
public class V2MaintenanceTrackingUpdateServiceInternalTest extends AbstractServiceTest {

    private static final Logger log = LoggerFactory.getLogger(V2MaintenanceTrackingUpdateServiceInternalTest.class);

    @Autowired
    private V2MaintenanceTrackingUpdateService v2MaintenanceTrackingUpdateService;

    @Autowired
    private V2MaintenanceTrackingDataService v2MaintenanceTrackingDataService;

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
        int count;
        do {
            count = v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(10);
            if (TestTransaction.isActive()) {
                TestTransaction.flagForCommit();
                TestTransaction.end();
            }
            TestTransaction.start();
        } while (count > 0);
        log.info("Handled {} trackings", count);
    }

    @Ignore("Just for internal testing")
    @Rollback(false)
    @Test
    public void longJumpInLineStringData() throws IOException {
        final StopWatch s = StopWatch.createStarted();
//        testHelper.saveTrackingFromResourceToDb("classpath:harja/service/big.json");
//        testHelper.saveTrackingFromResourceToDb("classpath:harja/service/big.json");testHelper.saveTrackingFromResourceToDb("classpath:harja/service/big.json");testHelper.saveTrackingFromResourceToDb("classpath:harja/service/big.json");testHelper.saveTrackingFromResourceToDb("classpath:harja/service/big.json");testHelper.saveTrackingFromResourceToDb("classpath:harja/service/big.json");testHelper.saveTrackingFromResourceToDb("classpath:harja/service/big.json");testHelper.saveTrackingFromResourceToDb("classpath:harja/service/big.json");
//        testHelper.saveTrackingFromResourceToDb("classpath:harja/service/big.json");
//        testHelper.saveTrackingFromResourceToDb("classpath:harja/service/distancegap/long-jump-1.json");
//        testHelper.saveTrackingFromResourceToDb("classpath:harja/service/distancegap/long-jump-2.json");
//        testHelper.saveTrackingFromResourceToDb("classpath:harja/service/distancegap/long-jump-3.json");
//        testHelper.saveTrackingFromResourceToDb("classpath:harja/service/distancegap/long-jump-4.json");
//        testHelper.saveTrackingFromResourceToDb("classpath:harja/service/big.json");

//        log.info("Handled count={} tookMs={}", v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(100), s.getTime());
    }

    @Test
    public void testFind() {
        final Instant start = Instant.parse("2021-01-22T00:00:00Z");

        v2MaintenanceTrackingDataService.findMaintenanceTrackings(start, start.plus(1, ChronoUnit.DAYS),
            RANGE_X_MIN, RANGE_Y_MIN, RANGE_X_MAX, RANGE_Y_MAX, Collections.emptyList());
    }

    // 3 m 28 s  -> no cache
    // 2m 7 s -> cache
    // 3m 4s -> cache optimized

    // always db
    // 2020-12-11 23:00:50,044 INFO	[main] - fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateService: method=handleUnhandledMaintenanceTrackingData tookMs=169478
    //2020-12-11 23:00:50,044 INFO	[main] - fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateService: Got data from db=97 from cache=20081
    //2020-12-11 23:00:50,044 INFO	[main] - fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateService: When cached     dbQueryTook=2
    //2020-12-11 23:00:50,044 INFO	[main] - fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateService: When not cached dbQueryTook=13
    //2020-12-11 23:00:50,044 INFO	[main] - fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateServiceInternalTest: Handled count=1 tookMs=170235

    // cahce
    // 2020-12-11 23:06:12,809 INFO	[main] - fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateService: method=handleUnhandledMaintenanceTrackingData tookMs=96862
    //2020-12-11 23:06:12,809 INFO	[main] - fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateService: Got data from db=97 from cache=20081
    //2020-12-11 23:06:12,809 INFO	[main] - fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateService: When cached     dbQueryTook=0
    //2020-12-11 23:06:12,809 INFO	[main] - fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateService: When not cached dbQueryTook=8
    //2020-12-11 23:06:12,810 INFO	[main] - fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateServiceInternalTest: Handled count=1 tookMs=97743

    // always db 2.
    // 2020-12-11 23:14:11,949 INFO	[main] - fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateService: method=handleUnhandledMaintenanceTrackingData tookMs=158824
    //2020-12-11 23:14:11,950 INFO	[main] - fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateService: Got data from db=97 from cache=20081
    //2020-12-11 23:14:11,950 INFO	[main] - fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateService: When cached     dbQueryTookAvg=2 dbQueryTookTotal=44810
    //2020-12-11 23:14:11,950 INFO	[main] - fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateService: When not cached dbQueryTookAvg=8 dbQueryTookTotal=841
    //2020-12-11 23:14:11,950 INFO	[main] - fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateServiceInternalTest: Handled count=1 tookMs=159732 = 2,6 min

    // cache 2
    // 2020-12-11 23:17:21,894 INFO	[main] - fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateService: method=handleUnhandledMaintenanceTrackingData tookMs=103548
    //2020-12-11 23:17:21,894 INFO	[main] - fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateService: Got data from db=97 from cache=20081
    //2020-12-11 23:17:21,894 INFO	[main] - fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateService: When cached     dbQueryTookAvg=0 dbQueryTookTotal=0
    //2020-12-11 23:17:21,894 INFO	[main] - fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateService: When not cached dbQueryTookAvg=10 dbQueryTookTotal=994
    //2020-12-11 23:17:21,894 INFO	[main] - fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateServiceInternalTest: Handled count=1 tookMs=104507 1,7 min
}
