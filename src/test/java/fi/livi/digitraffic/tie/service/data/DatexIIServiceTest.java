package fi.livi.digitraffic.tie.service.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.io.ParseException;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractRestWebTestWithRegionGeometryGitAndDataServiceMock;
import fi.livi.digitraffic.tie.DataDatex2SituationTestHelper;
import fi.livi.digitraffic.tie.dao.data.DataDatex2SituationRepository;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.TrafficAnnouncementProperties.SituationType;
import fi.livi.digitraffic.tie.external.tloik.ims.v1_2_2.MessageTypeEnum;

/**
 * Service-level tests for {@link DatexIIService}.
 * Verifies that only the latest version of each situation is considered when evaluating
 * active situations, and that the time filter is applied to that latest version only.
 */
public class DatexIIServiceTest extends AbstractRestWebTestWithRegionGeometryGitAndDataServiceMock {

    @Autowired
    private DatexIIService datexIIService;

    @Autowired
    private DataDatex2SituationRepository dataDatex2SituationRepository;

    private DataDatex2SituationTestHelper helper;

    @BeforeEach
    void setUpHelper() {
        helper = new DataDatex2SituationTestHelper(dataDatex2SituationRepository);
    }

    private void insertSituationVersion(final String situationId,
                                        final long situationVersion,
                                        final SituationType situationType,
                                        final Instant startTime,
                                        final Instant endTime) throws ParseException {
        helper.insertSituation(situationId, situationVersion, situationType,
                MessageTypeEnum.SIMPPELI, DataDatex2SituationTestHelper.DEFAULT_SIMPPELI_VERSION,
                DataDatex2SituationTestHelper.DEFAULT_SIMPPELI_MESSAGE,
                startTime, endTime);
    }

    /**
     * A situation with a single version and no end_time is returned as active.
     */
    @Test
    public void findTrafficAnnouncements_singleVersionWithNoEndTime_isReturned() throws ParseException {
        final Instant now = Instant.now();
        insertSituationVersion("TEST-SITUATION-002", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                now.minus(5, ChronoUnit.HOURS), null);

        final var result = datexIIService.findTrafficAnnouncements(null, null, null);

        assertEquals(1, result.getFeatures().size(),
                "Situation with no end_time should be returned as active.");
    }

    /**
     * A situation whose only version has an expired end_time is not returned.
     */
    @Test
    public void findTrafficAnnouncements_singleVersionWithExpiredEndTime_isNotReturned() throws ParseException {
        final Instant now = Instant.now();
        insertSituationVersion("TEST-SITUATION-003", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                now.minus(5, ChronoUnit.HOURS),
                now.minus(1, ChronoUnit.HOURS)); // expired

        final var result = datexIIService.findTrafficAnnouncements(null, null, null);

        assertEquals(0, result.getFeatures().size(),
                "Situation whose only version has an expired end_time should not be returned.");
    }

    /**
     * When the latest version has a future end_time the situation is returned,
     * regardless of older versions.
     */
    @Test
    public void findTrafficAnnouncements_latestVersionWithFutureEndTime_isReturned() throws ParseException {
        final Instant now = Instant.now();
        final Instant startTime = now.minus(5, ChronoUnit.HOURS);

        // Version 1: also active (no end_time)
        insertSituationVersion("TEST-SITUATION-004", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                startTime, null);

        // Version 2 (latest): active end_time in the future
        insertSituationVersion("TEST-SITUATION-004", 2L, SituationType.TRAFFIC_ANNOUNCEMENT,
                startTime, now.plus(1, ChronoUnit.HOURS));

        final var result = datexIIService.findTrafficAnnouncements(null, null, null);

        assertEquals(1, result.getFeatures().size(),
                "When the latest version has an active (future) end_time, the situation should be returned.");
    }

    /**
     * When {@code from} is omitted the default is {@code now - 1 hour}.
     * A situation whose end_time is within that window (ended 30 min ago) must be returned,
     * while one whose end_time is outside the window (ended 90 min ago) must not.
     */
    @Test
    public void findTrafficAnnouncements_defaultFromIsNowMinus1Hour() throws ParseException {
        final Instant now = Instant.now();
        final Instant start = now.minus(5, ChronoUnit.HOURS);

        // ended 30 min ago — within the default 1-hour window → should be returned
        insertSituationVersion("TEST-SITUATION-WITHIN-WINDOW", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                start, now.minus(30, ChronoUnit.MINUTES));

        // ended 90 min ago — outside the default 1-hour window → should NOT be returned
        insertSituationVersion("TEST-SITUATION-OUTSIDE-WINDOW", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                start, now.minus(90, ChronoUnit.MINUTES));

        final var resultDefault = datexIIService.findTrafficAnnouncements(null, null, null);
        assertEquals(1, resultDefault.getFeatures().size(),
                "Only the situation within the default 1-hour window should be returned.");

        // With an explicit from far in the past both situations should be visible
        final var resultExplicit = datexIIService.findTrafficAnnouncements(now.minus(2, ChronoUnit.HOURS), null, null);
        assertEquals(2, resultExplicit.getFeatures().size(),
                "With an explicit from=now-2h both situations should be returned.");
    }

    /**
     * When the latest version has an expired end_time the situation must not be returned,
     * even if an older version for the same situation_id has no end_time.
     */
    @Test
    public void findTrafficAnnouncements_latestVersionWithExpiredEndTime_isNotReturned() throws ParseException {
        final Instant now = Instant.now();
        final Instant startTime = now.minus(5, ChronoUnit.HOURS);

        // Version 1: no end_time
        insertSituationVersion("TEST-SITUATION-001", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                startTime, null);

        // Version 2 (latest): expired end_time — situation is closed
        insertSituationVersion("TEST-SITUATION-001", 2L, SituationType.TRAFFIC_ANNOUNCEMENT,
                startTime, now.minus(1, ChronoUnit.HOURS));

        final var result = datexIIService.findTrafficAnnouncements(null, null, null);

        assertEquals(0, result.getFeatures().size(),
                "Latest version has expired end_time — situation must not be returned.");
    }
}
