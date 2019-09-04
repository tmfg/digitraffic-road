package fi.livi.digitraffic.tie.data.model.trafficsigns;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
public class Device {
    @Id
    private String id;

    private String type;

    private String roadAddress;

    @Column(name = "etrs_tm35fin_x")
    private BigDecimal etrsTm35FinX;

    @Column(name = "etrs_tm35fin_y")
    private BigDecimal etrsTm35FinY;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getRoadAddress() {
        return roadAddress;
    }

    public void setRoadAddress(final String roadAddress) {
        this.roadAddress = roadAddress;
    }

    public BigDecimal getEtrsTm35FinX() {
        return etrsTm35FinX;
    }

    public void setEtrsTm35FinX(final BigDecimal etrsTm35FinX) {
        this.etrsTm35FinX = etrsTm35FinX;
    }

    public BigDecimal getEtrsTm35FinY() {
        return etrsTm35FinY;
    }

    public void setEtrsTm35FinY(final BigDecimal etrsTm35FinY) {
        this.etrsTm35FinY = etrsTm35FinY;
    }
}
