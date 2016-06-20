package fi.livi.digitraffic.tie.metadata.geojson.lamstation;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.roadstation.RoadStationProperties;
import fi.livi.digitraffic.tie.metadata.geojson.roadweather.RoadWeatherStationProperties;
import fi.livi.digitraffic.tie.metadata.model.CalculatorDeviceType;
import fi.livi.digitraffic.tie.metadata.model.LamStationType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Lam station properties", value = "LamStationProperties", parent = RoadWeatherStationProperties.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "lamNaturalId", "naturalId", "name" })
public class LamStationProperties extends RoadStationProperties {

    @ApiModelProperty(value = "Lam station's unique id")
    @JsonIgnore // Using road station's natural id
    private long id;

    // lam aseman naturalId
    @ApiModelProperty(value = "LAM station identifier (naturalId)", required = true)
    @JsonProperty(value = "lamId")
    private long lamNaturalId;

    @ApiModelProperty(value = "Direction 1 municipality (1 = According to the road register address increasing direction. I.e. on the road 4 to Lahti, if we are in Korso.)", required = true, position = 1)
    private String direction1Municipality;

    @ApiModelProperty(value = "Direction 1 municipality code")
    private Integer direction1MunicipalityCode;

    @ApiModelProperty(value = "Direction 2 municipality (2 = According to the road register address decreasing direction. I.e. on the road 4 to Helsinki, if we are in Korso.)", required = true, position = 1)
    private String direction2Municipality;

    @ApiModelProperty(value = "Direction 2 municipality code")
    private Integer direction2MunicipalityCode;

    @ApiModelProperty(value = "Type of  lam station")
    private LamStationType lamStationType;

    @ApiModelProperty(value = "Type of calculation device")
    private CalculatorDeviceType calculatorDeviceType;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public long getLamNaturalId() {
        return lamNaturalId;
    }

    public void setLamNaturalId(final long lamNaturalId) {
        this.lamNaturalId = lamNaturalId;
    }

    public String getDirection1Municipality() {
        return direction1Municipality;
    }

    public void setDirection1Municipality(final String direction1Municipality) {
        this.direction1Municipality = direction1Municipality;
    }

    public Integer getDirection1MunicipalityCode() {
        return direction1MunicipalityCode;
    }

    public void setDirection1MunicipalityCode(final Integer direction1MunicipalityCode) {
        this.direction1MunicipalityCode = direction1MunicipalityCode;
    }

    public String getDirection2Municipality() {
        return direction2Municipality;
    }

    public void setDirection2Municipality(final String direction2Municipality) {
        this.direction2Municipality = direction2Municipality;
    }

    public Integer getDirection2MunicipalityCode() {
        return direction2MunicipalityCode;
    }

    public void setDirection2MunicipalityCode(final Integer direction2MunicipalityCode) {
        this.direction2MunicipalityCode = direction2MunicipalityCode;
    }

    public LamStationType getLamStationType() {
        return lamStationType;
    }

    public void setLamStationType(final LamStationType lamStationType) {
        this.lamStationType = lamStationType;
    }

    public void setCalculatorDeviceType(CalculatorDeviceType calculatorDeviceType) {
        this.calculatorDeviceType = calculatorDeviceType;
    }

    public CalculatorDeviceType getCalculatorDeviceType() {
        return calculatorDeviceType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        LamStationProperties that = (LamStationProperties) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(id, that.id)
                .append(lamNaturalId, that.lamNaturalId)
                .append(direction1Municipality, that.direction1Municipality)
                .append(direction1MunicipalityCode, that.direction1MunicipalityCode)
                .append(direction2Municipality, that.direction2Municipality)
                .append(direction2MunicipalityCode, that.direction2MunicipalityCode)
                .append(lamStationType, that.lamStationType)
                .append(calculatorDeviceType, that.calculatorDeviceType)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(id)
                .append(lamNaturalId)
                .append(direction1Municipality)
                .append(direction1MunicipalityCode)
                .append(direction2Municipality)
                .append(direction2MunicipalityCode)
                .append(lamStationType)
                .append(calculatorDeviceType)
                .toHashCode();
    }
}
