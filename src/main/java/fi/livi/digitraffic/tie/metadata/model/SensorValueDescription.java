package fi.livi.digitraffic.tie.metadata.model;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Immutable;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel(description = "Additional information of sensor values")
@Entity
@DynamicUpdate
@Immutable
public class SensorValueDescription {

    @EmbeddedId
    SensorValueDescriptionPK sensorValueDescriptionPK;

    @ApiModelProperty(value = "Sensor description [en]", position = 2)
    private String descriptionEn;

    @ApiModelProperty(value = "Sensor description [fi]")
    private String descriptionFi;

    @ApiModelProperty(value = "Sensor value")
    public Double getSensorValue() {
        return sensorValueDescriptionPK.getSensorValue();
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public void setDescriptionEn(final String descriptionEn) {
        this.descriptionEn = descriptionEn;
    }

    public String getDescriptionFi() {
        return descriptionFi;
    }

    public void setDescriptionFi(final String descriptionFi) {
        this.descriptionFi = descriptionFi;
    }

    @Override
    public String toString() {
        return new ToStringHelpper(this)
                .appendField("sensorId", sensorValueDescriptionPK.getSensorId())
                .appendField("sensorValue", this.getSensorValue())
                .appendField("descriptionEn", getDescriptionEn())
                .appendField("descriptionFi", getDescriptionFi())
                .toString();
    }

}
