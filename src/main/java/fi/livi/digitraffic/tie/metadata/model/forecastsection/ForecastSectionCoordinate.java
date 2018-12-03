package fi.livi.digitraffic.tie.metadata.model.forecastsection;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
public class ForecastSectionCoordinate {

    @Id
    @GenericGenerator(name = "seq_forecast_section_coordinate", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "seq_forecast_section_coordinate"))
    @GeneratedValue(generator = "seq_forecast_section_coordinate")
    private Long id;

    @NotNull
    private Long orderNumber;

    @NotNull
    private BigDecimal longitude;

    @NotNull
    private BigDecimal latitude;

    public Long getId() {
        return id;
    }

    public Long getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Long orderNumber) {
        this.orderNumber = orderNumber;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }
}
