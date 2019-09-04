package fi.livi.digitraffic.tie.data.dto.trafficsigns;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeviceDataSchema {
    @JsonProperty
    private String tunnus;
    @JsonProperty
    private String nayttama;
    @JsonProperty
    private String lisatieto;
    @JsonProperty
    private ZonedDateTime voimaan;
    @JsonProperty
    private String syy;
}
