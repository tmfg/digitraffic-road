package fi.livi.digitraffic.tie.model.weather.forecast;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoadSegment {

    @EmbeddedId
    @JsonIgnore
    private RoadSegmentPK roadSegmentPK;

    @Schema(description = "Road segment start distance")
    private Integer startDistance;

    @Schema(description = "Road segment end distance")
    private Integer endDistance;

    @Schema(description = "Road segment carriageway")
    private Integer carriageway;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="forecast_section_id", nullable = false, referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    private ForecastSection forecastSection;

    public RoadSegment() {
    }

    public RoadSegment(final RoadSegmentPK roadSegmentPK, final Integer startDistance, final Integer endDistance,
                       final Integer carriageway, final ForecastSection forecastSection) {
        this.roadSegmentPK = roadSegmentPK;
        this.startDistance = startDistance;
        this.endDistance = endDistance;
        this.carriageway = carriageway;
        this.forecastSection = forecastSection;
    }

    public RoadSegmentPK getRoadSegmentPK() {
        return roadSegmentPK;
    }

    public void setRoadSegmentPK(final RoadSegmentPK roadSegmentPK) {
        this.roadSegmentPK = roadSegmentPK;
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

    public ForecastSection getForecastSection() {
        return forecastSection;
    }

    public void setForecastSection(final ForecastSection forecastSection) {
        this.forecastSection = forecastSection;
    }
}
