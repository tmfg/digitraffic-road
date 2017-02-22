package fi.livi.digitraffic.tie.data.service.traveltime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TravelTimeMeasurementDto {

    public final int offset;

    public final long travelTime;

    public TravelTimeMeasurementDto(@JsonProperty("os") final int offset,
                                    @JsonProperty("tt") final long travelTime) {
        this.offset = offset;
        this.travelTime = travelTime;
    }
}