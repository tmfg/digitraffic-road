package fi.livi.digitraffic.tie.metadata.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Entity
public class TmsSensorConstantValue {

    @Id
    @GenericGenerator(name = "SEQ_TMS_SENSOR_CONSTANT", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_ROAD"))
    @GeneratedValue(generator = "SEQ_ROAD")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="ROAD_STATION_ID")
    @Fetch(FetchMode.SELECT)
    private TmsSensorConstant sensorConstant;

    private Integer value;

    private Integer validFrom;

    private Integer validTo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof TmsSensorConstantValue)) {
            return false;
        }

        TmsSensorConstantValue that = (TmsSensorConstantValue) o;

        return new EqualsBuilder()
            .append(getSensorConstant(), that.getSensorConstant())
            .append(getValue(), that.getValue())
            .append(getValidFrom(), that.getValidFrom())
            .append(getValidTo(), that.getValidTo())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getSensorConstant())
            .append(getValue())
            .append(getValidFrom())
            .append(getValidTo())
            .toHashCode();
    }
}
