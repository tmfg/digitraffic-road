package fi.livi.digitraffic.tie.converter.waze;

import fi.livi.digitraffic.tie.dto.trafficmessage.v1.Restriction;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.RoadWorkPhase;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncement;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedIncidentDto;
import org.apache.commons.lang3.tuple.Pair;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedIncidentDto.WazeType.ROAD_CLOSED_CONSTRUCTION;

public abstract class WazeAnnouncementDurationConverter {
    private static final DateTimeFormatter wazeDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx");

    private WazeAnnouncementDurationConverter() {}

    private static boolean hasRoadClosed(final RoadWorkPhase p) {
        return p.restrictions.stream().anyMatch(r -> r.type == Restriction.Type.ROAD_CLOSED);
    }

    private static Pair<String, String> createDuration(final ZonedDateTime startTime, final ZonedDateTime endTime) {
        final String starttime = Optional.ofNullable(startTime)
                .map(ts -> ts.format(wazeDateTimeFormatter))
                .orElse(null);

        final String endtime = Optional.ofNullable(endTime)
                .map(ts -> ts.format(wazeDateTimeFormatter))
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
