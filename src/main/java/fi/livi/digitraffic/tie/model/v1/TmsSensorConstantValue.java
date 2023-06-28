package fi.livi.digitraffic.tie.model.v1;

import java.time.LocalDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

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

    public void setLotjuId(Long id) {
        this.lotjuId = id;
    }

    public TmsSensorConstant getSensorConstant() {
        return sensorConstant;
    }

    public void setSensorConstant(TmsSensorConstant sensorConstant) {
        this.sensorConstant = sensorConstant;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Integer getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Integer validFrom) {
        this.validFrom = validFrom;
    }

    public Integer getValidTo() {
        return validTo;
    }

    public void setValidTo(Integer validTo) {
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
