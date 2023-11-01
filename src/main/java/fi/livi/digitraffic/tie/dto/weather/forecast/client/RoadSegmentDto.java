package fi.livi.digitraffic.tie.dto.weather.forecast.client;

public class RoadSegmentDto {

    private Integer startDistance;

    private Integer endDistance;

    private Integer carriageway;

    public RoadSegmentDto() {
    }

    public Integer getStartDistance() {
        return startDistance;
    }

    public void setStartDistance(final Integer startDistance) {
        this.startDistance = startDistance;
    }

    public Integer getEndDistance() {
        return endDistance;
    }

    public void setEndDistance(final Integer endDistance) {
        this.endDistance = endDistance;
    }

    public Integer getCarriageway() {
        return carriageway;
    }

    public void setCarriageway(final Integer carriageway) {
        this.carriageway = carriageway;
    }
}
