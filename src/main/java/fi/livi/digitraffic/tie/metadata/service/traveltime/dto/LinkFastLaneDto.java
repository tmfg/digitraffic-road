package fi.livi.digitraffic.tie.metadata.service.traveltime.dto;

public class LinkFastLaneDto {

    public final Long naturalId;

    public final Long linkId;

    public final Long length;

    public final Double summerFreeFlowSpeed;

    public final Double winterFreeFlowSpeed;

    public final boolean winterSpeeds;

    public LinkFastLaneDto(Long naturalId, Long linkId, Long length, Double summerFreeFlowSpeed, Double winterFreeFlowSpeed, boolean winterSpeeds) {
        this.naturalId = naturalId;
        this.linkId = linkId;
        this.length = length;
        this.summerFreeFlowSpeed = summerFreeFlowSpeed;
        this.winterFreeFlowSpeed = winterFreeFlowSpeed;
        this.winterSpeeds = winterSpeeds;
    }

    public Long getNaturalId() {
        return naturalId;
    }

    public Double getFreeFlowSpeed() {
        if (winterSpeeds) {
            return winterFreeFlowSpeed;
        } else {
            return summerFreeFlowSpeed;
        }
    }

    public String toString() {
        return this.getClass().getSimpleName() + ", linkId=" + linkId
                + ", summer=" + summerFreeFlowSpeed + ", winter="
                + winterFreeFlowSpeed + ", freespeed=" + getFreeFlowSpeed()
                + ", length=" + length + ", winterSpeeds=" + winterSpeeds;
    }
}