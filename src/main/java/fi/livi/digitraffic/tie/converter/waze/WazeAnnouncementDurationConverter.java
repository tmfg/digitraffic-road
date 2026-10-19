package fi.livi.digitraffic.tie.converter.waze;

import static fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedIncidentDto.WazeType.ROAD_CLOSED_CONSTRUCTION;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import fi.livi.digitraffic.tie.dto.trafficmessage.v1.Restriction;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.RoadWorkPhase;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncement;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedIncidentDto;

public abstract class WazeAnnouncementDurationConverter {
    // The timestamp must be in ISO8601 format in granularity of seconds and include the time zone offset.
    //2023-04-07T09:00:00+01:00
    private static final DateTimeFormatter wazeDateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx")
                    .withZone(ZoneId.from(ZoneOffset.UTC));

    private WazeAnnouncementDurationConverter() {}

    private static boolean hasRoadClosed(final RoadWorkPhase p) {
        return p.restrictions.stream().anyMatch(r -> r.type == Restriction.Type.ROAD_CLOSED);
    }

    /**
     * A phase stays relevant until it has ended; a missing end time means it is valid indefinitely.
     * The start time is deliberately ignored, because CIFS treats every period of disruption as its
     * own incident and expects planned closures to be published in advance:
     * "You can use the Waze Partner feed to provide planned and real time road closures. The planned
     * closures you send to Waze [...] will appear on the Waze app during the actual time of the
     * closure." Waze schedules them itself from the starttime we send.
     *
     * <p>The caller passes the time in so that every decision made about one announcement is made at
     * the same instant. Reading the clock separately in each check would let a phase end midway
     * through the conversion, after which the incident type says the road is closed but no phase is
     * left to publish, and the announcement would be published as a closure as a whole.</p>
     *
     * @see <a href="https://developers.google.com/waze/data-feed/road-closure-information">Waze road closure information</a>
     */
    public static boolean hasNotEnded(final RoadWorkPhase phase, final Instant now) {
        final Instant endTime = phase.timeAndDuration.endTime;
        return endTime == null || now.isBefore(endTime);
    }

    public static Pair<String, String> createDuration(final Instant startTime, final Instant endTime) {
        final String starttime = Optional.ofNullable(startTime)
                .map(wazeDateTimeFormatter::format)
                .orElse(null);

        final String endtime = Optional.ofNullable(endTime)
                .map(wazeDateTimeFormatter::format)
                .orElse(null);

        return Pair.of(starttime, endtime);
    }
    public static Pair<String, String> getAnnouncementDuration(final TrafficAnnouncement announcement,
                                                               final Optional<WazeFeedIncidentDto.WazeType> maybeType,
                                                               final Instant now) {
        if(maybeType.isPresent() && maybeType.get() == ROAD_CLOSED_CONSTRUCTION) {
            final Optional<RoadWorkPhase> maybePhase = announcement.roadWorkPhases.stream()
                    .filter(phase -> hasNotEnded(phase, now))
                    .filter(WazeAnnouncementDurationConverter::hasRoadClosed).findFirst();
            // get from phases

            if(maybePhase.isPresent()) {
                final RoadWorkPhase phase = maybePhase.get();
                return createDuration(phase.timeAndDuration.startTime, phase.timeAndDuration.endTime);
            }
        }

        // default, get from announcement
        return createDuration(announcement.timeAndDuration.startTime, announcement.timeAndDuration.endTime);
    }
}
