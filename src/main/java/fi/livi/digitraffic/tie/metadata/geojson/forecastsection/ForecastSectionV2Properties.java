package fi.livi.digitraffic.tie.metadata.geojson.forecastsection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;

@JsonPropertyOrder({ "naturalId", "description" })
public class ForecastSectionV2Properties {

    @ApiModelProperty(value =
                          "Forecast section identifier 26 characters ie. 00004_342_01435_0_274.569: \n" +
                          "1. Road number 5 characters ie. 00004, \n" +
                          "2. Road section 3 characters ie. 342, \n" +
                          "3. Smallest start distance 5 characters ie. 000, \n" +
                          "4. Smallest carriageway 1 character, \n" +
                          "5. Smallest ALKU_PAALU ie. 274.569")
    @JsonProperty("id")
    private String naturalId;

    @ApiModelProperty(value = "Forecast section description")
    private String description;

    public ForecastSectionV2Properties() {
    }

    // TODO: add properties
    public ForecastSectionV2Properties(String naturalId, String description) {
        this.naturalId = naturalId;
        this.description = description;
    }

    public String getNaturalId() {
        return naturalId;
    }

    public String getDescription() {
        return description;
    }
}
