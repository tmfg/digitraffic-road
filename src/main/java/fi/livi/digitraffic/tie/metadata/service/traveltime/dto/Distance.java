package fi.livi.digitraffic.tie.metadata.service.traveltime.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Distance {

    public final String unit;

    public final BigDecimal value;

    public Distance(@JsonProperty("unit") final String unit,
                    @JsonProperty("value") final BigDecimal value) {
        this.unit = unit;
        this.value = value;
    }
}
