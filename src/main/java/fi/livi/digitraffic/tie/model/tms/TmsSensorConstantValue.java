package fi.livi.digitraffic.tie.model.tms;

import java.time.LocalDate;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

@Entity
public class TmsSensorConstantValue {

    @Id
    private Long lotjuId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="SENSOR_CONSTANT_LOTJU_ID")
    @Fetch(FetchMode.SELECT)
    @NotNull
    private TmsSensorConstant sensorConstant;

    @NotNull
    private Integer value;

    @NotNull
    private Integer validFrom;

    @NotNull
    private Integer validTo;

    @NotNull
    private LocalDate updated;

    private LocalDate obsoleteDate;

    public Long getLotjuId() {
        return lotjuId;
    }

    public void setLotjuId(final Long id) {
        this.lotjuId = id;
    }

    public TmsSensorConstant getSensorConstant() {
        return sensorConstant;
    }

    public void setSensorConstant(final TmsSensorConstant sensorConstant) {
        this.sensorConstant = sensorConstant;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(final Integer value) {
        this.value = value;
    }

    public Integer getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(final Integer validFrom) {
        this.validFrom = validFrom;
    }

    public Integer getValidTo() {
        return validTo;
    }

    public void setValidTo(final Integer validTo) {
        this.validTo = validTo;
    }

    public LocalDate getUpdated() {
        return updated;
    }

    public LocalDate getObsoleteDate() {
        return obsoleteDate;
    }

    @Override
    public String toString() {
        return ToStringHelper.toString(this);
    }
}
