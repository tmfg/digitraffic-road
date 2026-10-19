package fi.livi.digitraffic.tie.converter.waze;

import static fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedIncidentDto.WazeType.ACCIDENT_NONE;
import static fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedIncidentDto.WazeType.HAZARD_NONE;
import static fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedIncidentDto.WazeType.JAM_HEAVY_TRAFFIC;
import static fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedIncidentDto.WazeType.JAM_MODERATE_TRAFFIC;
import static fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedIncidentDto.WazeType.JAM_STAND_STILL_TRAFFIC;
import static fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedIncidentDto.WazeType.ROAD_CLOSED_CONSTRUCTION;
import static fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedIncidentDto.WazeType.ROAD_CLOSED_HAZARD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractSpringJUnitTest;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.Contact;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.Restriction;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.RoadWorkPhase;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TimeAndDuration;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncement;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.SituationType;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementProperties;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementType;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeDatex2FeatureDto;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedIncidentDto;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.metadata.geojson.Point;

public class WazeTypeConverterTest extends AbstractSpringJUnitTest {
    @Autowired
    private WazeTypeConverter wazeTypeConverter;

    private Optional<WazeFeedIncidentDto.WazeType> testConvert(final SituationType situationType,
                                                               final TrafficAnnouncementType trafficAnnouncementType) {
        return testConvert(situationType, trafficAnnouncementType, "");
    }

    private Optional<WazeFeedIncidentDto.WazeType> testConvert(final SituationType situationType,
                                                               final TrafficAnnouncementType trafficAnnouncementType,
                                                               final String datex2Message) {
        return testConvert(situationType, trafficAnnouncementType, datex2Message, new ArrayList<>());
    }

    private Optional<WazeFeedIncidentDto.WazeType> testConvert(final SituationType situationType,
                                                               final TrafficAnnouncementType trafficAnnouncementType,
                                                               final String datex2Message,
                                                               final List<TrafficAnnouncement> announcements) {
        final Geometry<Double> g = new Point(1.0, 2.2);
        final TrafficAnnouncementProperties properties = new TrafficAnnouncementProperties("id", 1, situationType,
                trafficAnnouncementType,
                Instant.now(), Instant.now(), announcements, new Contact());

        final TrafficAnnouncementFeature feature = new TrafficAnnouncementFeature(g, properties);
        final WazeDatex2FeatureDto dto =
                new WazeDatex2FeatureDto("id", situationType, datex2Message, null, feature);

        return wazeTypeConverter.convertToWazeType(dto, Instant.now());
    }

    private void assertType(final WazeFeedIncidentDto.WazeType expected,
                            final Optional<WazeFeedIncidentDto.WazeType> value) {
        if (expected != null) {
            assertTrue(value.isPresent());
            assertEquals(expected, value.get());
        } else {
            assertFalse(value.isPresent());
        }
    }

    @Test
    public void noTrafficAnnouncementType() {
        final Optional<WazeFeedIncidentDto.WazeType> type = testConvert(SituationType.TRAFFIC_ANNOUNCEMENT, null);

        assertType(null, type);
    }

    @Test
    public void jam_standStillTraffic() {
        final Optional<WazeFeedIncidentDto.WazeType> type =
                testConvert(SituationType.ROAD_WORK, TrafficAnnouncementType.GENERAL,
                        "<abnormalTrafficType>stationaryTraffic</abnormalTrafficType>");

        assertType(JAM_STAND_STILL_TRAFFIC, type);
    }

    @Test
    public void jam_heavyTraffic() {
        final Optional<WazeFeedIncidentDto.WazeType> type =
                testConvert(SituationType.ROAD_WORK, TrafficAnnouncementType.GENERAL,
                        "<abnormalTrafficType>heavyTraffic</abnormalTrafficType>");

        assertType(JAM_HEAVY_TRAFFIC, type);
    }

    @Test
    public void jam_moderateTraffic() {
        final Optional<WazeFeedIncidentDto.WazeType> type =
                testConvert(SituationType.ROAD_WORK, TrafficAnnouncementType.GENERAL,
                        "<abnormalTrafficType>queuingTraffic</abnormalTrafficType>");

        assertType(JAM_MODERATE_TRAFFIC, type);
    }

    @ParameterizedTest
    @MethodSource("convertTrafficAnnouncementProvider")
    public void convertTrafficAnnouncement(final TrafficAnnouncementType taType,
                                           final WazeFeedIncidentDto.WazeType expected) {
        final Optional<WazeFeedIncidentDto.WazeType> type = testConvert(SituationType.TRAFFIC_ANNOUNCEMENT, taType);

        assertType(expected, type);
    }

    private static Stream<Arguments> convertTrafficAnnouncementProvider() {
        return Stream.of(
                Arguments.of(TrafficAnnouncementType.ENDED, null),
                Arguments.of(TrafficAnnouncementType.GENERAL, HAZARD_NONE),
                Arguments.of(TrafficAnnouncementType.PRELIMINARY_ACCIDENT_REPORT, ACCIDENT_NONE),
                Arguments.of(TrafficAnnouncementType.ACCIDENT_REPORT, ACCIDENT_NONE)
        );
    }

    /**
     * Builds the part of the Simppeli announcement that {@link WazeTypeConverter#isRoadClosed} looks at: the
     * first announcement's road work phases. Only the restriction type and the phase time window matter, so
     * everything else is left null.
     */
    private static List<TrafficAnnouncement> announcementWithPhase(final Restriction.Type restrictionType,
                                                                   final Instant startTime,
                                                                   final Instant endTime) {
        final Restriction restriction = new Restriction(restrictionType, null);
        final RoadWorkPhase phase = new RoadWorkPhase("phase-id", null, null, null, List.of(restriction), null, null,
                null, null, null, null, new TimeAndDuration(startTime, endTime, null));

        return List.of(new TrafficAnnouncement(null, "title", null, null, null, List.of(phase), null, null, null,
                null, null, null));
    }

    private static List<TrafficAnnouncement> ongoingRoadClosure() {
        return announcementWithPhase(Restriction.Type.ROAD_CLOSED, Instant.now().minus(1, ChronoUnit.HOURS),
                Instant.now().plus(1, ChronoUnit.HOURS));
    }

    @Test
    public void trafficAnnouncement_roadClosed() {
        final Optional<WazeFeedIncidentDto.WazeType> type =
                testConvert(SituationType.TRAFFIC_ANNOUNCEMENT, TrafficAnnouncementType.GENERAL, "",
                        ongoingRoadClosure());

        assertType(ROAD_CLOSED_HAZARD, type);
    }

    @Test
    public void roadwork_roadClosed() {
        final Optional<WazeFeedIncidentDto.WazeType> type =
                testConvert(SituationType.ROAD_WORK, TrafficAnnouncementType.GENERAL, "", ongoingRoadClosure());

        assertType(ROAD_CLOSED_CONSTRUCTION, type);
    }

    /**
     * A closure that is still open-ended stays closed, so a null end time must not be read as "already over".
     */
    @Test
    public void roadwork_roadClosedWithoutEndTime() {
        final Optional<WazeFeedIncidentDto.WazeType> type =
                testConvert(SituationType.ROAD_WORK, TrafficAnnouncementType.GENERAL, "",
                        announcementWithPhase(Restriction.Type.ROAD_CLOSED,
                                Instant.now().minus(1, ChronoUnit.HOURS), null));

        assertType(ROAD_CLOSED_CONSTRUCTION, type);
    }

    @Test
    public void roadwork_roadClosurePhaseHasEnded() {
        final Optional<WazeFeedIncidentDto.WazeType> type =
                testConvert(SituationType.ROAD_WORK, TrafficAnnouncementType.GENERAL, "",
                        announcementWithPhase(Restriction.Type.ROAD_CLOSED,
                                Instant.now().minus(2, ChronoUnit.HOURS), Instant.now().minus(1, ChronoUnit.HOURS)));

        assertType(null, type);
    }

    /**
     * A planned closure must be classified as a closure before it starts: CIFS expects planned
     * closures in the feed in advance and Waze schedules them from the starttime we send.
     *
     * @see <a href="https://developers.google.com/waze/data-feed/road-closure-information">Waze road closure information</a>
     */
    @Test
    public void roadwork_roadClosurePhaseHasNotStartedYet() {
        final Optional<WazeFeedIncidentDto.WazeType> type =
                testConvert(SituationType.ROAD_WORK, TrafficAnnouncementType.GENERAL, "",
                        announcementWithPhase(Restriction.Type.ROAD_CLOSED,
                                Instant.now().plus(1, ChronoUnit.HOURS), Instant.now().plus(2, ChronoUnit.HOURS)));

        assertType(ROAD_CLOSED_CONSTRUCTION, type);
    }

    /**
     * The incident type must be decided from the instant the caller passes in, not from the clock.
     * {@code WazeDatex2JsonConverter} reads the clock once and uses the same instant both here and when
     * it picks the phases to publish. If this read the clock again, a phase could end in between, and
     * the type would say the road is closed while no phase was left to publish — the whole road work
     * would then be published as a closure.
     */
    @Test
    public void roadwork_roadClosedIsDecidedFromTheGivenInstant() {
        final Instant start = Instant.now().minus(2, ChronoUnit.HOURS);
        final Instant end = Instant.now().plus(1, ChronoUnit.HOURS);
        final TrafficAnnouncementFeature feature = roadWorkFeature(announcementWithPhase(Restriction.Type.ROAD_CLOSED, start, end));

        assertTrue(WazeTypeConverter.isRoadClosed(feature, end.minusSeconds(1)));
        assertFalse(WazeTypeConverter.isRoadClosed(feature, end.plusSeconds(1)));
    }

    private static TrafficAnnouncementFeature roadWorkFeature(final List<TrafficAnnouncement> announcements) {
        final TrafficAnnouncementProperties properties = new TrafficAnnouncementProperties("id", 1, SituationType.ROAD_WORK,
                TrafficAnnouncementType.GENERAL, Instant.now(), Instant.now(), announcements, new Contact());

        return new TrafficAnnouncementFeature(new Point(1.0, 2.2), properties);
    }

    /**
     * Only {@link Restriction.Type#ROAD_CLOSED} closes a road; a speed limit on the same phase must not.
     */    @Test
    public void roadwork_otherRestrictionIsNotARoadClosure() {
        final Optional<WazeFeedIncidentDto.WazeType> type =
                testConvert(SituationType.ROAD_WORK, TrafficAnnouncementType.GENERAL, "",
                        announcementWithPhase(Restriction.Type.SPEED_LIMIT,
                                Instant.now().minus(1, ChronoUnit.HOURS), Instant.now().plus(1, ChronoUnit.HOURS)));

        assertType(null, type);
    }

    /**
     * A road closure outranks whatever the Datex II message would otherwise map to.
     */
    @Test
    public void roadClosedTakesPrecedenceOverTheDatex2Type() {
        final Optional<WazeFeedIncidentDto.WazeType> type =
                testConvert(SituationType.ROAD_WORK, TrafficAnnouncementType.GENERAL,
                        "<abnormalTrafficType>heavyTraffic</abnormalTrafficType>", ongoingRoadClosure());

        assertType(ROAD_CLOSED_CONSTRUCTION, type);
    }
}
