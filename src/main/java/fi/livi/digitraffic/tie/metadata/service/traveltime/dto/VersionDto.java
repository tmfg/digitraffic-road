package fi.livi.digitraffic.tie.metadata.service.traveltime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VersionDto {

    public final String minor;

    public final String major;

    public VersionDto(@JsonProperty("minor") final String minor,
                      @JsonProperty("major") final String major) {
        this.minor = minor;
        this.major = major;
    }
}
