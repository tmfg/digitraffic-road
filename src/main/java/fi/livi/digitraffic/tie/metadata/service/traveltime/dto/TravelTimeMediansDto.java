package fi.livi.digitraffic.tie.metadata.service.traveltime.dto;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TravelTimeMediansDto {

    public final Date periodStart;

    public final int duration;

    public final String supplier;

    public final String service;

    public final Date creationTime;

    public final Date lastStaticDataUpdate;

    public final List<TravelTimeMedianDto> medians;

    public TravelTimeMediansDto(@JsonProperty("periodstart") final Date periodStart,
                                @JsonProperty("duration") final int duration,
                                @JsonProperty("SUP") final String supplier,
                                @JsonProperty("service") final String service,
                                @JsonProperty("creationtime") final Date creationTime,
                                @JsonProperty("LastStaticDataUpdate") final Date lastStaticDataUpdate,
                                @JsonProperty("links") List<TravelTimeMedianDto> medians) {
        this.periodStart = periodStart;
        this.duration = duration;
        this.supplier = supplier;
        this.service = service;
        this.creationTime = creationTime;
        this.lastStaticDataUpdate = lastStaticDataUpdate;
        this.medians = medians;
    }
}