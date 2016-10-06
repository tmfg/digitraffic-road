package fi.livi.digitraffic.tie.metadata.model;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
public class RoadSectionCoordinates {

    @Id
    @GenericGenerator(name = "SEQ_ROAD_SECTION_COORDINATES", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_ROAD_STATION"))
    @GeneratedValue(generator = "SEQ_ROAD_SECTION_COORDINATES")
    private Long id;

    @ManyToOne
    @JoinColumn(name="forecast_section_id", nullable = false, referencedColumnName = "id")
    @Fetch(FetchMode.JOIN)
    private ForecastSection forecastSection;

    @NotNull
    private Long orderNumber;

    @NotNull
    private BigDecimal longitude;

    @NotNull
    private BigDecimal latitude;

    public RoadSectionCoordinates() {
    }

    public RoadSectionCoordinates(ForecastSection forecastSection, Long orderNumber, BigDecimal longitude, BigDecimal latitude) {
        this.forecastSection = forecastSection;
        this.orderNumber = orderNumber;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Long getId() {
        return id;
    }

    public ForecastSection getForecastSection() {
        return forecastSection;
    }

    public Long getOrderNumber() {
        return orderNumber;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }
}
