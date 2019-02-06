package fi.livi.digitraffic.tie.metadata.model.forecastsection;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModelProperty;

@Entity
public class RoadSegment {

    @EmbeddedId
    @JsonIgnore
    private RoadSegmentPK roadSegmentPK;

    @ApiModelProperty(value = "Road segment start distance")
    private Integer startDistance;

    @ApiModelProperty(value = "Road segment end distance")
    private Integer endDistance;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="forecast_section_id", nullable = false, referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    private ForecastSection forecastSection;

    public RoadSegment() {
    }

    public RoadSegment(RoadSegmentPK roadSegmentPK, Integer startDistance, Integer endDistance,
                       ForecastSection forecastSection) {
        this.roadSegmentPK = roadSegmentPK;
        this.startDistance = startDistance;
        this.endDistance = endDistance;
        this.forecastSection = forecastSection;
    }

    public RoadSegmentPK getRoadSegmentPK() {
        return roadSegmentPK;
    }

    public void setRoadSegmentPK(RoadSegmentPK roadSegmentPK) {
        this.roadSegmentPK = roadSegmentPK;
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

    public ForecastSection getForecastSection() {
        return forecastSection;
    }

    public void setForecastSection(ForecastSection forecastSection) {
        this.forecastSection = forecastSection;
    }
}
