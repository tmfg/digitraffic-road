package fi.livi.digitraffic.tie.data.dao;

import static fi.livi.digitraffic.test.util.AssertUtil.assertCollectionSize;
import static fi.livi.digitraffic.tie.dto.trafficmessage.v1.SituationType.TRAFFIC_ANNOUNCEMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractJpaTest;
import fi.livi.digitraffic.tie.dao.trafficmessage.datex2.Datex2Repository;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2Situation;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2SituationRecord;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2SituationRecordType;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2SituationRecordValidyStatus;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.SituationType;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.TrafficAnnouncementType;
import jakarta.persistence.EntityManager;

public class Datex2RepositoryTest extends AbstractJpaTest {

    @Autowired
    private Datex2Repository datex2Repository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    public void insertDatex() {
        datex2Repository.deleteAll();
        final Datex2 datex2 = new Datex2(SituationType.ROAD_WORK, null);

        datex2.setImportTime(Instant.now());
        datex2.setMessage("Message of high importance");

        datex2Repository.save(datex2);
    }

    @Test
    public void testDelete() {
        final List<Datex2> all = datex2Repository.findAll();
        assertCollectionSize(1, all);

        datex2Repository.delete(all.getFirst());

        final List<Datex2> after = datex2Repository.findAll();
        assertCollectionSize(0, after);
    }

    @Test
    public void testThatNewAndOldTypesForAllSituationTypesAreStillSavedToDb() {
        for (final SituationType type : SituationType.values()) {
            datex2Repository.deleteAll();
            if (SituationType.TRAFFIC_ANNOUNCEMENT == type) {
                // Test also that new TrafficAnnouncementTypes are saved
                for (final TrafficAnnouncementType trafficAnnouncementType : TrafficAnnouncementType.values()) {
                    datex2Repository.deleteAll();
                    createAndSaveDatex2Message(type, trafficAnnouncementType);
                    final List<Datex2> found = datex2Repository.findAll();
                    assertCollectionSize(1, found);
                    assertEquals(type, found.getFirst().getSituationType());
                    assertEquals(trafficAnnouncementType, found.getFirst().getTrafficAnnouncementType());
                }
            } else {
                createAndSaveDatex2Message(type, null);
                final List<Datex2> found = datex2Repository.findAll();
                assertCollectionSize(1, found);
                assertEquals(type, found.getFirst().getSituationType());
                assertNull(found.getFirst().getTrafficAnnouncementType());
            }

        }
    }

    private void createAndSaveDatex2Message(final SituationType type, final TrafficAnnouncementType trafficAnnouncementType) {
        final Datex2 datex2 = new Datex2(type, trafficAnnouncementType);
        datex2.setImportTime(Instant.now());
        datex2.setMessage("Message of high importance");
        datex2Repository.save(datex2);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    public void activeInPastHours() {

        final String pastSituationId = "GUID12345678";
        final List<Datex2> beroreActive10Hours = datex2Repository.findAllActiveBySituationType(10, TRAFFIC_ANNOUNCEMENT.name());
        // Situation should not exist
        assertFalse(beroreActive10Hours.stream()
            .anyMatch(d -> d.getSituations() != null && d.getSituations().stream()
                         .anyMatch(s -> s.getSituationId().equals(pastSituationId))));

        // Create traffic disorder in past for 2 hours
        createDatex2InPast2h(pastSituationId, SituationType.TRAFFIC_ANNOUNCEMENT, TrafficAnnouncementType.GENERAL);

        // Situation should be found >= 3 h
        final List<Datex2> active3Hours = datex2Repository.findAllActiveBySituationType(3, TRAFFIC_ANNOUNCEMENT.name());
        assertTrue(active3Hours.stream()
            .anyMatch(d -> d.getSituations() != null && d.getSituations().stream()
                .anyMatch(s -> s.getSituationId().equals(pastSituationId))));

        // 2 h in past wont find it as its little bit over 2 h old
        final List<Datex2> afterActive2Hours = datex2Repository.findAllActiveBySituationType(2, TRAFFIC_ANNOUNCEMENT.name());
        assertFalse(afterActive2Hours.stream()
            .anyMatch(d -> d.getSituations() != null && d.getSituations().stream()
                .anyMatch(s -> s.getSituationId().equals(pastSituationId))));
    }

    private void createDatex2InPast2h(final String situationId, final SituationType type, final TrafficAnnouncementType trafficAnnouncementType) {
        final Datex2 datex2 = new Datex2(type, trafficAnnouncementType);
        datex2.setImportTime(Instant.now());
        datex2.setMessage("xml message");
        datex2.setPublicationTime(Instant.now());

        final Datex2Situation situation = new Datex2Situation();
        situation.setSituationId(situationId);
        situation.setDatex2(datex2);
        datex2.setSituations(Collections.singletonList(situation));

        final Datex2SituationRecord record = new Datex2SituationRecord();
        record.setType(Datex2SituationRecordType.TRAFFIC_ELEMENT_ACCIDENT);
        record.setSituationRecordId(situationId + "01");
        record.setVersionTime(Instant.now().minus(10, ChronoUnit.HOURS));
        record.setCreationTime(Instant.now().minus(10, ChronoUnit.HOURS));
        record.setValidyStatus(Datex2SituationRecordValidyStatus.DEFINED_BY_VALIDITY_TIME_SPEC);
        record.setOverallStartTime(Instant.now().minus(10, ChronoUnit.HOURS));
        record.setOverallEndTime(Instant.now().minus(2, ChronoUnit.HOURS).minusSeconds(1));
        record.setLifeCycleManagementCanceled(false);
        record.setSituation(situation);
        situation.setSituationRecords(Collections.singletonList(record));

        datex2Repository.save(datex2);
        datex2Repository.flush(); // native query used in findActive

    }

}
