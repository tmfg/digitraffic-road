package fi.livi.digitraffic.tie.data.dto.freeflowspeed;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;

import io.swagger.annotations.ApiModelProperty;

@Entity
@Immutable
public class LinkFreeFlowSpeedDto {

    @Id
    @ApiModelProperty(value = "Link id", required = true)
    private long linkNo;

    @ApiModelProperty(value = "Free flow speed [km/h]", required = true)
    private double freeFlowSpeed;

    public long getLinkNo() {
        return linkNo;
    }

    public void setLinkNo(final long linkNo) {
        this.linkNo = linkNo;
    }

    public double getFreeFlowSpeed() {
        return freeFlowSpeed;
    }

    public void setFreeFlowSpeed(final double freeFlowSpeed) {
        this.freeFlowSpeed = freeFlowSpeed;
    }
}
