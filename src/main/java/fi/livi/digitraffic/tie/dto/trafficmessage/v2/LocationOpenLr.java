package fi.livi.digitraffic.tie.dto.trafficmessage.v2;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "OpenLR encoded location reference, an alternative way to specify the location.",
        name = "LocationOpenLrV2")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocationOpenLr {

    @Schema(description = "Base64-encoded OpenLR binary location reference")
    public String binary;

    public LocationOpenLr() {
    }

    public LocationOpenLr(final String binary) {
        this.binary = binary;
    }
}
