package fi.livi.digitraffic.tie.data.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;

import io.swagger.annotations.ApiModelProperty;

@Entity
@Immutable
public class LinkFreeFlowSpeed {
    @Id
    private long linkNo;

    @ApiModelProperty(value = "Free flow speed [km/h]")
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
