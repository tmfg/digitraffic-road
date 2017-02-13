package fi.livi.digitraffic.tie.metadata.service.traveltime.dto;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TravelTimeMeasurementsDto {

    public final Date periodStart;

    public final int duration;

    public final List<TravelTimeMeasurementLinkDto>  measurements;

    public TravelTimeMeasurementsDto(@JsonProperty("periodstart") final Date periodStart,
                                     @JsonProperty("duration") final int duration,
                                     @JsonProperty("irs") List<TravelTimeMeasurementLinkDto> measurements) {
        this.periodStart = periodStart;
        this.duration = duration;
        this.measurements = measurements;
    }
}