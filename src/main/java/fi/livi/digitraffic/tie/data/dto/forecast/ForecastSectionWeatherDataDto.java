package fi.livi.digitraffic.tie.data.dto.forecast;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;

@JsonPropertyOrder({ "naturalId", "roadConditions" })
public class ForecastSectionWeatherDataDto {

    @ApiModelProperty(
            "Forecast section identifier 15 characters ie. 00004_112_000_0: \n" +
            "1. Road number 5 characters ie. 00004, \n" +
            "2. Road section 3 characters ie. 112, \n" +
            "3. Road section version 3 characters ie. 000, \n" +
            "4. Reserved for future needs 1 characters default 0")
    @JsonProperty("id")
    public final String naturalId;

    @ApiModelProperty("Road conditions data forecast section")
    public final List<RoadConditionDto> roadConditions;

    public ForecastSectionWeatherDataDto(final String naturalId, final List<RoadConditionDto> roadConditions) {
        this.naturalId = naturalId;
        this.roadConditions = roadConditions;
    }
}