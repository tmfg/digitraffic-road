package fi.livi.digitraffic.tie.model.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;

/** Exist only for API v1 compatibility reasons **/
@Deprecated()
@JsonPropertyOrder({ "id", "name" })
public class RoadDistrict {

    @Schema(description = "Road district id (ELY)")
    @JsonProperty(value = "id")
    public int naturalId;

    @Schema(description = "Road district name")
    public String name;

    @Schema(description = "Road district speed limit season")
    public String speedLimitSeason;
}
