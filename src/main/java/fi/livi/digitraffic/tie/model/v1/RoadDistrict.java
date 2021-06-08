package fi.livi.digitraffic.tie.model.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;


/** Exist only for API v1 compatibility reasons **/
@Deprecated()
@JsonPropertyOrder({ "id", "name" })
public class RoadDistrict {

    @ApiModelProperty(value = "Road district id (ELY)")
    @JsonProperty(value = "id")
    public int naturalId;

    @ApiModelProperty(value = "Road district name")
    public String name;

    @ApiModelProperty("Road district speed limit season")
    public String speedLimitSeason;
}
