package fi.livi.digitraffic.tie.metadata.service.traveltime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Freeflowspeed {

    public final String unit;

    public final String value;

    public Freeflowspeed(@JsonProperty("unit") final String unit,
                         @JsonProperty("value") final String value) {
        this.unit = unit;
        this.value = value;
    }
}
