package fi.livi.digitraffic.tie.data.dto.freeflowspeed;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "LinkFreeFlowSpeedData")
@Entity
@Immutable
public class LinkFreeFlowSpeedDto {

    @Id
    @ApiModelProperty(value = "Link id", required = true)
    @JsonProperty("id")
    private long linkNo;

    @ApiModelProperty(value = "Free flow speed [km/h]", required = true)
    private Double freeFlowSpeed;

    public long getLinkNo() {
        return linkNo;
    }

    public void setLinkNo(final long linkNo) {
        this.linkNo = linkNo;
    }

    public Double getFreeFlowSpeed() {
        return freeFlowSpeed;
    }

    public void setFreeFlowSpeed(final Double freeFlowSpeed) {
        this.freeFlowSpeed = freeFlowSpeed;
    }
}
