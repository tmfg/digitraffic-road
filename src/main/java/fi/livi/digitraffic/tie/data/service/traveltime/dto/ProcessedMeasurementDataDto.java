package fi.livi.digitraffic.tie.data.service.traveltime.dto;

import java.util.Date;

public class ProcessedMeasurementDataDto {

    public final Date endTimestamp;

    public final long travelTime;

    public final long linkId;

    public ProcessedMeasurementDataDto(Date endTimestamp, long travelTime, long linkId) {
        this.endTimestamp = endTimestamp;
        this.travelTime = travelTime;
        this.linkId = linkId;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ", end timestamp="
                + endTimestamp + ", travel time=" + travelTime;
    }
}
