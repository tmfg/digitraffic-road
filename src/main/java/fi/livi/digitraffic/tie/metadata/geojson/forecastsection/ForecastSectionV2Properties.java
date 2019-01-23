package fi.livi.digitraffic.tie.metadata.geojson.forecastsection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;

@JsonPropertyOrder({ "naturalId", "description" })
public class ForecastSectionV2Properties {

    @ApiModelProperty(value =
                          "Forecast section identifier ie. 00004_342_01435_0_274.569: \n" +
                          "1. Road number 5 characters ie. 00004, \n" +
                          "2. Road section 3 characters ie. 342, \n" +
                          "3. Start distance 5 characters ie. 000, \n" +
                          "4. Carriageway 1 character, \n" +
                          "5. Measure value of link start point. Varying number of characters ie. 274.569, \n" +
                          "Refers to Digiroad content at https://aineistot.liikennevirasto.fi/digiroad/")
    @JsonProperty("id")
    private String naturalId;

    @ApiModelProperty(value = "Forecast section description")
    private String description;

    @ApiModelProperty(value = "Forecast section road number")
    private int roadNumber;

    @ApiModelProperty(value = "Road section number")
    private int roadSectionNumber;

    @ApiModelProperty(value = "Forecast section length in meters")
    private Integer length;

    public ForecastSectionV2Properties() {
    }

    public ForecastSectionV2Properties(final String naturalId, final String description, final int roadNumber, final int roadSectionNumber,
                                       final Integer length) {
        this.naturalId = naturalId;
        this.description = description;
        this.roadNumber = roadNumber;
        this.roadSectionNumber = roadSectionNumber;
        this.length = length;
    }

    public String getNaturalId() {
        return naturalId;
    }

    public String getDescription() {
        return description;
    }

    public int getRoadNumber() {
        return roadNumber;
    }

    public int getRoadSectionNumber() {
        return roadSectionNumber;
    }

    public Integer getLength() {
        return length;
    }
}
