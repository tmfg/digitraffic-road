package fi.livi.digitraffic.tie.metadata.geojson.tms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.roadstation.RoadStationProperties;
import fi.livi.digitraffic.tie.model.tms.CalculatorDeviceType;
import fi.livi.digitraffic.tie.model.tms.TmsStationType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "TMS station properties", name = "TmsStationProperties")
@JsonPropertyOrder({ "roadStationId", "tmsNumber", "name" })
public class TmsStationProperties extends RoadStationProperties {

    @JsonIgnore // Using road station's natural id
    private long id;

    // tms aseman naturalId
    @Schema(description = "TMS station number (naturalId)", required = true)
    @JsonProperty(value = "tmsNumber")
    private long tmsNaturalId;

    @Schema(description = "Direction 1 municipality (1 = According to the road register address increasing direction. I.e. on the road 4 to Lahti, if we are in Korso.)", required = true)
    private String direction1Municipality;

    @Schema(description = "Direction 1 municipality code")
    private Integer direction1MunicipalityCode;

    @Schema(description = "Direction 2 municipality (2 = According to the road register address decreasing direction. I.e. on the road 4 to Helsinki, if we are in Korso.)", required = true)
    private String direction2Municipality;

    @Schema(description = "Direction 2 municipality code")
    private Integer direction2MunicipalityCode;

    @Schema(description = "Type of  TMS station")
    @JsonProperty(value = "tmsStationType")
    private TmsStationType tmsStationType;

    @Schema(description = "Type of calculation device")
    private CalculatorDeviceType calculatorDeviceType;

    /** Sensors natural ids */
    @Schema(description = "Tms Station Sensors ids")
    private List<Long> stationSensors = new ArrayList<>();

    @Schema(description = "Free flow speed to direction 1 [km/h]")
    private Double freeFlowSpeed1;

    @Schema(description = "Free flow speed to direction 2 [km/h]")
    private Double freeFlowSpeed2;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public long getTmsNaturalId() {
        return tmsNaturalId;
    }

    public void setTmsNaturalId(final long tmsNaturalId) {
        this.tmsNaturalId = tmsNaturalId;
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

    public TmsStationType getTmsStationType() {
        return tmsStationType;
    }

    public void setTmsStationType(final TmsStationType tmsStationType) {
        this.tmsStationType = tmsStationType;
    }

    public void setCalculatorDeviceType(final CalculatorDeviceType calculatorDeviceType) {
        this.calculatorDeviceType = calculatorDeviceType;
    }

    public CalculatorDeviceType getCalculatorDeviceType() {
        return calculatorDeviceType;
    }

    public List<Long> getStationSensors() {
        return stationSensors;
    }

    public void setStationSensors(final List<Long> stationSensors) {
        this.stationSensors = stationSensors;
        Collections.sort(this.stationSensors);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final TmsStationProperties that = (TmsStationProperties) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(id, that.id)
                .append(tmsNaturalId, that.tmsNaturalId)
                .append(direction1Municipality, that.direction1Municipality)
                .append(direction1MunicipalityCode, that.direction1MunicipalityCode)
                .append(direction2Municipality, that.direction2Municipality)
                .append(direction2MunicipalityCode, that.direction2MunicipalityCode)
                .append(tmsStationType, that.tmsStationType)
                .append(calculatorDeviceType, that.calculatorDeviceType)
                .append(stationSensors, that.stationSensors)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(id)
                .append(tmsNaturalId)
                .append(direction1Municipality)
                .append(direction1MunicipalityCode)
                .append(direction2Municipality)
                .append(direction2MunicipalityCode)
                .append(tmsStationType)
                .append(calculatorDeviceType)
                .append(stationSensors)
                .toHashCode();
    }

    public Double getFreeFlowSpeed1() {
        return freeFlowSpeed1;
    }

    public void setFreeFlowSpeed1(final Double freeFlowSpeed1) {
        this.freeFlowSpeed1 = freeFlowSpeed1;
    }

    public Double getFreeFlowSpeed2() {
        return freeFlowSpeed2;
    }

    public void setFreeFlowSpeed2(final Double freeFlowSpeed2) {
        this.freeFlowSpeed2 = freeFlowSpeed2;
    }
}
