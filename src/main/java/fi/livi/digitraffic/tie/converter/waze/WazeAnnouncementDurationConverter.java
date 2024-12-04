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

    private static Pair<String, String> createDuration(final Instant startTime, final Instant endTime) {
        final String starttime = Optional.ofNullable(startTime)
                .map(wazeDateTimeFormatter::format)
                .orElse(null);

        final String endtime = Optional.ofNullable(endTime)
                .map(wazeDateTimeFormatter::format)
                .orElse(null);

        return Pair.of(starttime, endtime);
    }
    public static Pair<String, String> getAnnouncementDuration(final TrafficAnnouncement announcement, final Optional<WazeFeedIncidentDto.WazeType> maybeType) {
        if(maybeType.isPresent() && maybeType.get() == ROAD_CLOSED_CONSTRUCTION) {
            final Optional<RoadWorkPhase> maybePhase = announcement.roadWorkPhases.stream().filter(WazeAnnouncementDurationConverter::hasRoadClosed).findFirst();
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
