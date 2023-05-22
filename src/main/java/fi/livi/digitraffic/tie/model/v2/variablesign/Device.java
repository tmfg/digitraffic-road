package fi.livi.digitraffic.tie.model.v2.variablesign;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;

import fi.livi.digitraffic.tie.model.ReadOnlyCreatedAndModifiedFields;

@Entity
@Immutable
public class Device extends ReadOnlyCreatedAndModifiedFields {
    @Id
    private String id;

    private Instant deletedDate;

    private String type;

    private String roadAddress;

    private String direction;

    private String carriageway;

    @Column(name = "etrs_tm35fin_x")
    private Double etrsTm35FinX;

    @Column(name = "etrs_tm35fin_y")
    private Double etrsTm35FinY;

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

    public Double getEtrsTm35FinX() {
        return etrsTm35FinX;
    }

    public void setEtrsTm35FinX(final Double etrsTm35FinX) {
        this.etrsTm35FinX = etrsTm35FinX;
    }

    public Double getEtrsTm35FinY() {
        return etrsTm35FinY;
    }

    public void setEtrsTm35FinY(final Double etrsTm35FinY) {
        this.etrsTm35FinY = etrsTm35FinY;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(final String direction) {
        this.direction = direction;
    }

    public String getCarriageway() {
        return carriageway;
    }

    public void setCarriageway(final String carriageway) {
        this.carriageway = carriageway;
    }

    public Instant getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(Instant deletedDate) {
        this.deletedDate = deletedDate;
    }
}
