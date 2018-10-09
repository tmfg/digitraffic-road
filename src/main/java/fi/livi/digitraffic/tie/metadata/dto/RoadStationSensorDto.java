package fi.livi.digitraffic.tie.metadata.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.model.SensorValueDescription;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Road station sensor")
@JsonPropertyOrder(value = {"id", "name", "shortName", "descriptionFi", "descriptionSv", "descriptionEn", "unit", "accuracy", "nameOld", "sensorValueDescriptions"})
public class RoadStationSensorDto {

    @ApiModelProperty(value = "Sensor id", position = 1)
    @JsonProperty("id")
    private long naturalId;

    @ApiModelProperty(value = "Sensor old name. For new sensors will equal name. Will deprecate in future.", position = 2, notes = "noteja")
    @JsonProperty(value = "nameOld")
    private String name;

    @ApiModelProperty(value = "Unit of sensor value")
    private String unit;

    @ApiModelProperty(value = "Sensor description [fi]")
    private String descriptionFi;

    @ApiModelProperty(value = "Sensor description [sv]")
    private String descriptionSv;

    @ApiModelProperty(value = "Sensor description [en]")
    private String descriptionEn;

    @ApiModelProperty(value = "Sensor name [fi]")
    @JsonProperty(value = "name")
    private String nameFi;

    @ApiModelProperty(value = "Short name for sensor [fi]")
    @JsonProperty(value = "shortName")
    private String shortNameFi;

    @ApiModelProperty(value = "Sensor accuracy")
    private Integer accuracy;

    @ApiModelProperty("Possible additional descriptions for sensor values")
    private List<SensorValueDescription> sensorValueDescriptions;

    @ApiModelProperty(value = "Presentation name for sensor [fi]")
    private String presentationNameFi;

    @ApiModelProperty(value = "Presentation name for sensor [sv]")
    private String presentationNameSv;

    @ApiModelProperty(value = "Presentation name for sensor [en]")
    private String presentationNameEn;

    public RoadStationSensorDto(long naturalId, String name, String unit, String descriptionFi, String descriptionSv, String descriptionEn,
                                String nameFi, String shortNameFi, Integer accuracy,
                                List<SensorValueDescription> sensorValueDescriptions, String presentationNameFi, String presentationNameSv,
                                String presentationNameEn) {
        this.naturalId = naturalId;
        this.name = name;
        this.unit = unit;
        this.descriptionFi = descriptionFi;
        this.descriptionSv = descriptionSv;
        this.descriptionEn = descriptionEn;
        this.nameFi = nameFi;
        this.shortNameFi = shortNameFi;
        this.accuracy = accuracy;
        this.sensorValueDescriptions = sensorValueDescriptions;
        this.presentationNameFi = presentationNameFi;
        this.presentationNameSv = presentationNameSv;
        this.presentationNameEn = presentationNameEn;
    }

    public long getNaturalId() {
        return naturalId;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    @ApiModelProperty(value = "Sensor description [fi]")
    public String getDescription() {
        return getDescriptionFi();
    }

    public String getDescriptionFi() {
        return descriptionFi;
    }

    public String getDescriptionSv() {
        return descriptionSv;
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public String getNameFi() {
        return nameFi;
    }

    public String getShortNameFi() {
        return shortNameFi;
    }

    public Integer getAccuracy() {
        return accuracy;
    }

    public List<SensorValueDescription> getSensorValueDescriptions() {
        return sensorValueDescriptions;
    }

    public String getPresentationNameFi() {
        return presentationNameFi;
    }

    public String getPresentationNameSv() {
        return presentationNameSv;
    }

    public String getPresentationNameEn() {
        return presentationNameEn;
    }

}
