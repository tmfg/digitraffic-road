package fi.livi.digitraffic.tie.service.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fi.livi.digitraffic.tie.dao.data.DataDatex2SituationRepository;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.TimeAndDuration;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.TrafficAnnouncement;
import fi.livi.digitraffic.tie.service.trafficmessage.DatexII223UpdateService;

/**
 * Unit tests for {@link ImsUpdatingService#getStartAndEndTimes}.
 *
 * <p>Key invariant: a situation is only "fully ended" when <em>all</em> its
 * announcements carry an {@code endTime}.  If any announcement is open-ended
 * ({@code endTime == null}) the whole situation must remain open-ended so it
 * is not prematurely hidden from the API.</p>
 */
class ImsUpdatingServiceTest {

    private ImsUpdatingService service;

    private static final Instant T1 = Instant.parse("2025-01-01T10:00:00Z");
    private static final Instant T2 = Instant.parse("2025-01-01T12:00:00Z");
    private static final Instant T3 = Instant.parse("2025-01-01T14:00:00Z");
    private static final Instant T4 = Instant.parse("2025-01-01T16:00:00Z");

    @BeforeEach
    void setUp() {
        service = new ImsUpdatingService(
                mock(DatexII223UpdateService.class),
                mock(DataDatex2SituationRepository.class));
    }

    // -------------------------------------------------------------------------
    // Single announcement
    // -------------------------------------------------------------------------

    @Test
    void singleAnnouncement_withEndTime_returnsEndTime() {
        final var announcements = List.of(announcement(T1, T3));

        final var result = service.getStartAndEndTimes(announcements);

        assertEquals(T1, result.getLeft(),  "startTime should be T1");
        assertEquals(T3, result.getRight(), "endTime should be T3");
    }

    @Test
    void singleAnnouncement_withoutEndTime_returnsNullEndTime() {
        final var announcements = List.of(announcement(T1, null));

        final var result = service.getStartAndEndTimes(announcements);

        assertEquals(T1, result.getLeft(), "startTime should be T1");
        assertNull(result.getRight(), "endTime should be null — announcement is open-ended");
    }

    // -------------------------------------------------------------------------
    // Multiple announcements (e.g. language variants of the same event)
    // -------------------------------------------------------------------------

    @Test
    void multipleAnnouncements_allHaveEndTime_returnsMaxEndTime() {
        // Three language variants, each with a slightly different endTime.
        // Situation ends when the last one ends.
        final var announcements = List.of(
                announcement(T1, T2),
                announcement(T1, T4),  // latest — should win
                announcement(T1, T3));

        final var result = service.getStartAndEndTimes(announcements);

        assertEquals(T4, result.getRight(), "endTime should be the maximum across all announcements");
    }

    @Test
    void multipleAnnouncements_someHaveEndTimeSomeDoNot_returnsNullEndTime() {
        // Announcement 1 has ended; announcement 2 is still open.
        // The situation as a whole must remain open-ended.
        final var announcements = List.of(
                announcement(T1, T2),   // has endTime
                announcement(T1, null)  // open-ended
        );

        final var result = service.getStartAndEndTimes(announcements);

        assertNull(result.getRight(),
                "endTime must be null when at least one announcement is open-ended");
    }

    @Test
    void multipleAnnouncements_noneHaveEndTime_returnsNullEndTime() {
        final var announcements = List.of(
                announcement(T1, null),
                announcement(T2, null));

        final var result = service.getStartAndEndTimes(announcements);

        assertNull(result.getRight(), "endTime should be null when no announcement has endTime");
    }

    // -------------------------------------------------------------------------
    // startTime selection: always the earliest across all announcements
    // -------------------------------------------------------------------------

    @Test
    void multipleAnnouncements_returnsMinStartTime() {
        final var announcements = List.of(
                announcement(T3, T4),
                announcement(T1, T4),  // earliest start — should win
                announcement(T2, T4));

        final var result = service.getStartAndEndTimes(announcements);

        assertEquals(T1, result.getLeft(), "startTime should be the minimum across all announcements");
    }

    @Test
    void multipleAnnouncements_someStartTimesNull_returnsMinNonNullStartTime() {
        // Announcements with null startTime are ignored for startTime selection
        final var announcements = List.of(
                announcement(null, T4),
                announcement(T2,   T4));

        final var result = service.getStartAndEndTimes(announcements);

        assertEquals(T2, result.getLeft(), "startTime should be the earliest non-null value");
    }

    // -------------------------------------------------------------------------
    // Edge cases
    // -------------------------------------------------------------------------

    @Test
    void emptyAnnouncementList_returnsBothNull() {
        final var result = service.getStartAndEndTimes(List.of());

        assertNull(result.getLeft(),  "startTime should be null for empty list");
        assertNull(result.getRight(), "endTime should be null for empty list");
    }

    @Test
    void singleAnnouncement_bothTimesNull_returnsBothNull() {
        final var result = service.getStartAndEndTimes(List.of(announcement(null, null)));

        assertNull(result.getLeft(),  "startTime should be null");
        assertNull(result.getRight(), "endTime should be null");
    }

    @Test
    void singleAnnouncement_missingTimeAndDuration_returnsBothNull() {
        // timeAndDuration is not required by the IMS JSON schema;
        // the method must not throw and must treat the announcement as open-ended.
        final var announcement = new TrafficAnnouncement(); // timeAndDuration left null

        final var result = service.getStartAndEndTimes(List.of(announcement));

        assertNull(result.getLeft(),  "startTime should be null when timeAndDuration is absent");
        assertNull(result.getRight(), "endTime should be null when timeAndDuration is absent");
    }

    @Test
    void multipleAnnouncements_oneMissingTimeAndDuration_returnsNullEndTime() {
        // One normal announcement with times, one without timeAndDuration.
        // The missing block makes the situation open-ended; startTime comes from the normal one.
        final var normal  = announcement(T1, T3);
        final var missing = new TrafficAnnouncement(); // no timeAndDuration

        final var result = service.getStartAndEndTimes(List.of(normal, missing));

        assertEquals(T1, result.getLeft(),  "startTime should come from the announcement that has it");
        assertNull(result.getRight(), "endTime must be null when any announcement lacks timeAndDuration");
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private static TrafficAnnouncement announcement(final Instant startTime, final Instant endTime) {
        return new TrafficAnnouncement()
                .withTimeAndDuration(new TimeAndDuration(startTime, endTime, null));
    }
}

