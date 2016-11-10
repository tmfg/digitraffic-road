package fi.livi.digitraffic.tie.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import fi.livi.digitraffic.tie.metadata.model.ForecastSectionWeather;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@JsonPropertyOrder({ "roadId", "roadConditions" })
public class ForecastSectionWeatherDataDto {

    @ApiModelProperty(value =
            "Forecast section identifier 15 characters ie. 00004_112_000_0: \n" +
            "1. Road number 5 characters ie. 00004, \n" +
            "2. Road section 3 characters ie. 112, \n" +
            "3. Road section version 3 characters ie. 000, \n" +
            "4. Reserved for future needs 1 characters default 0")
    @JsonProperty("roadId")
    public final String naturalId;

    @ApiModelProperty(value = "Road conditions data forecast section")
    public final List<ForecastSectionWeather> roadConditions;

    public ForecastSectionWeatherDataDto(String naturalId, List<ForecastSectionWeather> roadConditions) {
        this.naturalId = naturalId;
        this.roadConditions = roadConditions;
    }
}
