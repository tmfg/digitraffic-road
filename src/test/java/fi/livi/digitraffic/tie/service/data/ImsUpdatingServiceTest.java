package fi.livi.digitraffic.tie.service.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

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
    private static final Instant VERSION_TIME = Instant.parse("2025-01-01T09:00:00Z");

    // Convenience: call without versionTime (null) for non-cancel tests
    private Pair<Instant, Instant> getStartAndEndTimes(final List<TrafficAnnouncement> announcements) {
        return service.getStartAndEndTimes(announcements, null);
    }

    private Pair<Instant, Instant> getStartAndEndTimes(final List<TrafficAnnouncement> announcements,
                                                        final Instant versionTime) {
        return service.getStartAndEndTimes(announcements, versionTime);
    }

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

        final var result = getStartAndEndTimes(announcements);

        assertEquals(T1, result.getLeft(),  "startTime should be T1");
        assertEquals(T3, result.getRight(), "endTime should be T3");
    }

    @Test
    void singleAnnouncement_withoutEndTime_returnsNullEndTime() {
        final var announcements = List.of(announcement(T1, null));

        final var result = getStartAndEndTimes(announcements);

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

        final var result = getStartAndEndTimes(announcements);

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

        final var result = getStartAndEndTimes(announcements);

        assertNull(result.getRight(),
                "endTime must be null when at least one announcement is open-ended");
    }

    @Test
    void multipleAnnouncements_noneHaveEndTime_returnsNullEndTime() {
        final var announcements = List.of(
                announcement(T1, null),
                announcement(T2, null));

        final var result = getStartAndEndTimes(announcements);

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

        final var result = getStartAndEndTimes(announcements);

        assertEquals(T1, result.getLeft(), "startTime should be the minimum across all announcements");
    }

    @Test
    void multipleAnnouncements_someStartTimesNull_returnsMinNonNullStartTime() {
        // Announcements with null startTime are ignored for startTime selection
        final var announcements = List.of(
                announcement(null, T4),
                announcement(T2,   T4));

        final var result = getStartAndEndTimes(announcements);

        assertEquals(T2, result.getLeft(), "startTime should be the earliest non-null value");
    }

    // -------------------------------------------------------------------------
    // Edge cases
    // -------------------------------------------------------------------------

    @Test
    void emptyAnnouncementList_returnsBothNull() {
        final var result = getStartAndEndTimes(List.of());

        assertNull(result.getLeft(),  "startTime should be null for empty list");
        assertNull(result.getRight(), "endTime should be null for empty list");
    }

    @Test
    void singleAnnouncement_bothTimesNull_returnsBothNull() {
        final var result = getStartAndEndTimes(List.of(announcement(null, null)));

        assertNull(result.getLeft(),  "startTime should be null");
        assertNull(result.getRight(), "endTime should be null");
    }

    @Test
    void singleAnnouncement_missingTimeAndDuration_returnsBothNull() {
        // timeAndDuration is not required by the IMS JSON schema;
        // the method must not throw and must treat the announcement as open-ended.
        final var announcement = new TrafficAnnouncement(); // timeAndDuration left null

        final var result = getStartAndEndTimes(List.of(announcement));

        assertNull(result.getLeft(),  "startTime should be null when timeAndDuration is absent");
        assertNull(result.getRight(), "endTime should be null when timeAndDuration is absent");
    }

    @Test
    void multipleAnnouncements_oneMissingTimeAndDuration_returnsNullEndTime() {
        // One normal announcement with times, one without timeAndDuration.
        // The missing block makes the situation open-ended; startTime comes from the normal one.
        final var normal  = announcement(T1, T3);
        final var missing = new TrafficAnnouncement(); // no timeAndDuration

        final var result = getStartAndEndTimes(List.of(normal, missing));

        assertEquals(T1, result.getLeft(),  "startTime should come from the announcement that has it");
        assertNull(result.getRight(), "endTime must be null when any announcement lacks timeAndDuration");
    }

    // -------------------------------------------------------------------------
    // earlyClosing=CANCELED
    // -------------------------------------------------------------------------

    @Test
    void singleAnnouncement_earlyClosingCanceled_usesVersionTime() {
        // When versionTime is provided, it should be used as endTime for a canceled situation.
        final var announcement = new TrafficAnnouncement()
                .withTimeAndDuration(new TimeAndDuration(T1, null, null))
                .withEarlyClosing(TrafficAnnouncement.EarlyClosing.CANCELED);

        final var result = getStartAndEndTimes(List.of(announcement), VERSION_TIME);

        assertEquals(T1, result.getLeft(), "startTime should be preserved when earlyClosing=CANCELED");
        assertEquals(VERSION_TIME, result.getRight(), "endTime should be versionTime when provided");
    }

    @Test
    void singleAnnouncement_earlyClosingCanceled_nullVersionTime_fallsBackToNow() {
        // When versionTime is null, fall back to Instant.now() as endTime.
        final var announcement = new TrafficAnnouncement()
                .withTimeAndDuration(new TimeAndDuration(T1, null, null))
                .withEarlyClosing(TrafficAnnouncement.EarlyClosing.CANCELED);

        final var before = Instant.now();
        final var result = getStartAndEndTimes(List.of(announcement), null);
        final var after = Instant.now();

        assertEquals(T1, result.getLeft(), "startTime should be preserved when earlyClosing=CANCELED");
        assertNotNull(result.getRight(), "endTime must not be null for a canceled situation");
        assertTrue(!result.getRight().isBefore(before) && !result.getRight().isAfter(after),
                "endTime should fall back to approximately now when versionTime is null");
    }

    @Test
    void singleAnnouncement_earlyClosingCanceled_nullStartTime_fallsBackToEndTime() {
        // When earlyClosing=CANCELED and no startTime is available, startTime must fall
        // back to the canceledEndTime to satisfy the DB NOT NULL constraint on start_time.
        final var announcement = new TrafficAnnouncement()
                .withEarlyClosing(TrafficAnnouncement.EarlyClosing.CANCELED); // no timeAndDuration

        final var result = getStartAndEndTimes(List.of(announcement), VERSION_TIME);

        assertEquals(VERSION_TIME, result.getLeft(),
                "startTime must not be null — falls back to canceledEndTime (versionTime)");
        assertEquals(VERSION_TIME, result.getRight(), "endTime should be versionTime");
    }

    @Test
    void multipleAnnouncements_allHaveEarlyClosingCanceled_usesVersionTime() {
        // All announcements carry earlyClosing=CANCELED → situation is fully canceled.
        final var ann1 = new TrafficAnnouncement()
                .withTimeAndDuration(new TimeAndDuration(T1, null, null))
                .withEarlyClosing(TrafficAnnouncement.EarlyClosing.CANCELED);
        final var ann2 = new TrafficAnnouncement()
                .withTimeAndDuration(new TimeAndDuration(T2, null, null))
                .withEarlyClosing(TrafficAnnouncement.EarlyClosing.CANCELED);

        final var result = getStartAndEndTimes(List.of(ann1, ann2), VERSION_TIME);

        assertEquals(T1, result.getLeft(), "startTime should be the earliest across all canceled announcements");
        assertEquals(VERSION_TIME, result.getRight(), "endTime should be versionTime");
    }

    @Test
    void multipleAnnouncements_onlyOneHasEarlyClosingCanceled_returnsNullEndTime() {
        // Consistent with the endTime invariant: a situation is only fully canceled when
        // ALL its announcements carry earlyClosing=CANCELED.
        final var normal    = announcement(T1, null);
        final var cancelled = new TrafficAnnouncement()
                .withTimeAndDuration(new TimeAndDuration(T2, null, null))
                .withEarlyClosing(TrafficAnnouncement.EarlyClosing.CANCELED);

        final var result = getStartAndEndTimes(List.of(normal, cancelled), VERSION_TIME);

        assertNull(result.getRight(),
                "endTime must be null when not all announcements have earlyClosing=CANCELED");
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private static TrafficAnnouncement announcement(final Instant startTime, final Instant endTime) {
        return new TrafficAnnouncement()
                .withTimeAndDuration(new TimeAndDuration(startTime, endTime, null));
    }
}

