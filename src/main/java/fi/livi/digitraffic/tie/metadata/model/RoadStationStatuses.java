package fi.livi.digitraffic.tie.metadata.model;

import java.time.ZonedDateTime;
import java.util.List;

public class RoadStationStatuses {
    private final ZonedDateTime timestamp;

    private final List<RoadStationStatus> roadStationStatusData;

    public RoadStationStatuses(final List<RoadStationStatus> roadStationStatusData) {
        this.timestamp = ZonedDateTime.now();
        this.roadStationStatusData = roadStationStatusData;
    }

    public String getTimestamp() {
        return timestamp.toString();
    }

    public List<RoadStationStatus> getRoadStationStatusData() {
        return roadStationStatusData;
    }
}
