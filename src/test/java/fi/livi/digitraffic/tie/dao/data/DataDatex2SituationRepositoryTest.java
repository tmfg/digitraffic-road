package fi.livi.digitraffic.tie.dao.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractJpaTest;
import fi.livi.digitraffic.tie.DataDatex2SituationTestHelper;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.TrafficAnnouncementProperties.SituationType;

/**
 * DAO-level tests for {@link DataDatex2SituationRepository#findLatestByType}.
 * The query uses an {@code is_latest_version} flag (maintained by a DB trigger) to ensure
 * only the latest version per {@code situation_id} is considered before the time filter is applied.
 * This prevents an older version without an end_time from being returned when the latest version
 * has an expired end_time.
 */
public class DataDatex2SituationRepositoryTest extends AbstractJpaTest {

    /** Year ~3000 — mirrors the TIME_END sentinel used in DatexIIService. */
    private static final Instant FAR_FUTURE = Instant.ofEpochMilli(32503683600000L);

    @Autowired
    private DataDatex2SituationRepository dataDatex2SituationRepository;

    private DataDatex2SituationTestHelper helper;

    @BeforeEach
    void setUpHelper() {
        helper = new DataDatex2SituationTestHelper(dataDatex2SituationRepository);
    }

    /**
     * When the latest version has an expired end_time the situation must not be returned,
     * even if an older version for the same situation_id has no end_time.
     */
    @Test
    public void findLatestByType_latestVersionWithExpiredEndTime_isNotReturned() throws Exception {
        final Instant startTime = Instant.now().minus(5, ChronoUnit.HOURS);

        // Version 1: no end_time
        helper.insertSituation("GUID-DAO-001", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                startTime, null);

        // Version 2 (latest): expired end_time — situation is closed
        helper.insertSituation("GUID-DAO-001", 2L, SituationType.TRAFFIC_ANNOUNCEMENT,
                startTime, Instant.now().minus(1, ChronoUnit.HOURS));

        final List<Long> ids = dataDatex2SituationRepository.findLatestByType(
                SituationType.TRAFFIC_ANNOUNCEMENT.name(), Instant.now(), FAR_FUTURE, null);

        assertEquals(0, ids.size(),
                "Latest version has expired end_time — situation must not be returned.");
    }

    /**
     * Single version with no end_time is returned as active.
     */
    @Test
    public void findLatestByType_singleVersionNoEndTime_isReturned() throws Exception {
        helper.insertSituation("GUID-DAO-002", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                Instant.now().minus(5, ChronoUnit.HOURS), null);

        final List<Long> ids = dataDatex2SituationRepository.findLatestByType(
                SituationType.TRAFFIC_ANNOUNCEMENT.name(), Instant.now(), FAR_FUTURE, null);

        assertEquals(1, ids.size(),
                "A situation with no end_time should be returned as active.");
    }

    /**
     * Single version with an expired end_time is not returned.
     */
    @Test
    public void findLatestByType_singleVersionWithExpiredEndTime_isNotReturned() throws Exception {
        helper.insertSituation("GUID-DAO-003", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                Instant.now().minus(5, ChronoUnit.HOURS),
                Instant.now().minus(1, ChronoUnit.HOURS));

        final List<Long> ids = dataDatex2SituationRepository.findLatestByType(
                SituationType.TRAFFIC_ANNOUNCEMENT.name(), Instant.now(), FAR_FUTURE, null);

        assertEquals(0, ids.size(),
                "A situation with an expired end_time must not be returned.");
    }

    /**
     * When the latest version has a future end_time, exactly one id is returned
     * regardless of older versions.
     */
    @Test
    public void findLatestByType_latestVersionWithFutureEndTime_isReturned() throws Exception {
        final Instant startTime = Instant.now().minus(5, ChronoUnit.HOURS);

        helper.insertSituation("GUID-DAO-004", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                startTime, null);

        helper.insertSituation("GUID-DAO-004", 2L, SituationType.TRAFFIC_ANNOUNCEMENT,
                startTime, Instant.now().plus(1, ChronoUnit.HOURS));

        final List<Long> ids = dataDatex2SituationRepository.findLatestByType(
                SituationType.TRAFFIC_ANNOUNCEMENT.name(), Instant.now(), FAR_FUTURE, null);

        assertEquals(1, ids.size(),
                "Latest version has a future end_time — exactly one id should be returned.");
    }

    /**
     * Two distinct active situations each produce one id (one per situation_id).
     */
    @Test
    public void findLatestByType_twoDistinctActiveSituations_bothReturned() throws Exception {
        final Instant startTime = Instant.now().minus(5, ChronoUnit.HOURS);

        helper.insertSituation("GUID-DAO-005a", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                startTime, null);

        helper.insertSituation("GUID-DAO-005b", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                startTime, Instant.now().plus(2, ChronoUnit.HOURS));

        final List<Long> ids = dataDatex2SituationRepository.findLatestByType(
                SituationType.TRAFFIC_ANNOUNCEMENT.name(), Instant.now(), FAR_FUTURE, null);

        assertEquals(2, ids.size(),
                "Two distinct active situations should produce two ids.");
    }

    /**
     * Only situations matching the requested situation type are returned.
     */
    @Test
    public void findLatestByType_situationTypeFilterIsRespected() throws Exception {
        helper.insertSituation("GUID-DAO-006", 1L, SituationType.ROAD_WORK,
                Instant.now().minus(5, ChronoUnit.HOURS), null);

        final List<Long> ids = dataDatex2SituationRepository.findLatestByType(
                SituationType.TRAFFIC_ANNOUNCEMENT.name(), Instant.now(), FAR_FUTURE, null);

        assertEquals(0, ids.size(),
                "A ROAD_WORK situation must not appear in TRAFFIC_ANNOUNCEMENT results.");
    }
}
