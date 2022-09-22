package fi.livi.digitraffic.tie.service.maintenance.v1;

import static fi.livi.digitraffic.tie.TestUtils.commitAndEndTransactionAndStartNew;
import static fi.livi.digitraffic.tie.TestUtils.flushCommitEndTransactionAndStartNew;
import static fi.livi.digitraffic.tie.helper.AssertHelper.assertCollectionSize;
import static fi.livi.digitraffic.tie.helper.AssertHelper.assertEmpty;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingRepository;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTracking;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingWorkMachine;
import fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper;

public class MaintenanceTrackingMqttDataServiceTest extends AbstractServiceTest {


    private final String DOMAIN_WITH_SOURCE = "domain-with-source";
    private final String DOMAIN_WITHOUT_SOURCE = "domain-without-source";

    @Autowired
    private MaintenanceTrackingMqttDataService maintenanceTrackingWebDataServiceV1;

    @Autowired
    private V3MaintenanceTrackingServiceTestHelper testHelper;

    @Autowired
    private V2MaintenanceTrackingRepository v2MaintenanceTrackingRepository;

    @AfterEach
    @BeforeEach
    public void init() {
        testHelper.clearDb();
        testHelper.deleteDomains(DOMAIN_WITH_SOURCE, DOMAIN_WITHOUT_SOURCE);
        commitAndEndTransactionAndStartNew();
    }

    @Test
    public void findTrackingsForMqttCreatedAfterEmpty() {
        assertEmpty(maintenanceTrackingWebDataServiceV1.findTrackingsForMqttCreatedAfter(Instant.now()));
    }

    @Test
    public void findTrackingsForMqttCreatedAfter() {
        // Create trackings for domains with and witout soure
        final MaintenanceTrackingWorkMachine wm1 = testHelper.createAndSaveWorkMachine();
        testHelper.insertDomain(DOMAIN_WITH_SOURCE, "Foo/Bar");
        commitAndEndTransactionAndStartNew();
        testHelper.insertTrackingForDomain(DOMAIN_WITH_SOURCE, wm1.getId());
        testHelper.insertTrackingForDomain(DOMAIN_WITH_SOURCE, wm1.getId());
        flushCommitEndTransactionAndStartNew(entityManager);

        final List<MaintenanceTracking> all = v2MaintenanceTrackingRepository.findAll();
        assertCollectionSize(2, all);
        final Instant created = all.get(0).getCreated().toInstant();

        // no data as param is exlusive
        assertCollectionSize(0, maintenanceTrackingWebDataServiceV1.findTrackingsForMqttCreatedAfter(created));
        // Now data is returned as param is exlusive and it's before the created time
        assertCollectionSize(2, maintenanceTrackingWebDataServiceV1.findTrackingsForMqttCreatedAfter(created.minusMillis(1)));
    }

}
