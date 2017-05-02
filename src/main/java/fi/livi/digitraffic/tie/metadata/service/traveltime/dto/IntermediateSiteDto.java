package fi.livi.digitraffic.tie.metadata.service.traveltime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IntermediateSiteDto {

    public final int index;

    public final int number;

    public IntermediateSiteDto(@JsonProperty("index") final int index,
                               @JsonProperty("number") final int number) {
        this.index = index;
        this.number = number;
    }
}
