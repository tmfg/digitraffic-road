package fi.livi.digitraffic.tie.metadata.service.forecastsection.dto.v2;

public class RoadSegmentDto {

    private Integer startDistance;

    private Integer endDistance;

    public RoadSegmentDto() {
    }

    public Integer getStartDistance() {
        return startDistance;
    }

    public void setStartDistance(Integer startDistance) {
        this.startDistance = startDistance;
    }

    public Integer getEndDistance() {
        return endDistance;
    }

    public void setEndDistance(Integer endDistance) {
        this.endDistance = endDistance;
    }
}
