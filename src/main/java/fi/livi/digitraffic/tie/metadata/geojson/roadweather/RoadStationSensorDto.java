package fi.livi.digitraffic.tie.metadata.geojson.roadweather;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Road Weather Station Sensors", value = "RoadStationSensor")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "naturalId", "nameFi", "nameEn", "shortNameFi", "description", "sensorTypeId", "altitude" })
public class RoadStationSensorDto {

    @JsonIgnore
    private long id;

    @ApiModelProperty(value = "Road Weather Station Sensor unique id", position = 1)
    @JsonIgnore
    private Long lotjuId;

    @ApiModelProperty(value = "Sensor description", position = 3)
    private String description;

    @ApiModelProperty(value = "Sensor name", position = 2)
    private String nameEn;

    @JsonProperty("id")
    private long naturalId;
    private String nameFi;
    private String shortNameFi;
    private String calculationFormula;
    private Integer accuracy;
    private String unit;
    private Integer r;
    private Integer g;
    private Integer b;

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public Long getLotjuId() {
        return lotjuId;
    }

    public void setLotjuId(Long lotjuId) {
        this.lotjuId = lotjuId;
    }


    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNaturalId(long naturalId) {
        this.naturalId = naturalId;
    }

    public long getNaturalId() {
        return naturalId;
    }

    public void setNameFi(String nameFi) {
        this.nameFi = nameFi;
    }

    public String getNameFi() {
        return nameFi;
    }

    public void setShortNameFi(String shortNameFi) {
        this.shortNameFi = shortNameFi;
    }

    public String getShortNameFi() {
        return shortNameFi;
    }

    public void setCalculationFormula(String calculationFormula) {
        this.calculationFormula = calculationFormula;
    }

    public String getCalculationFormula() {
        return calculationFormula;
    }

    public void setAccuracy(Integer accuracy) {
        this.accuracy = accuracy;
    }

    public Integer getAccuracy() {
        return accuracy;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUnit() {
        return unit;
    }

    public void setR(Integer r) {
        this.r = r;
    }

    public Integer getR() {
        return r;
    }

    public void setG(Integer g) {
        this.g = g;
    }

    public Integer getG() {
        return g;
    }

    public void setB(Integer b) {
        this.b = b;
    }

    public Integer getB() {
        return b;
    }

    @Override
    public String toString() {
        return new ToStringHelpper(this)
                .appendField("id", getId())
                .appendField("lotjuId", this.getLotjuId())
                .appendField("naturalId", getNaturalId())
                .appendField("nameFi", getNameFi())
                .appendField("unit", getUnit())
                .toString();
    }
}
