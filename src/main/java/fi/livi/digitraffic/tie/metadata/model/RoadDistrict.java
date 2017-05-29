package fi.livi.digitraffic.tie.metadata.model;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Parameter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.annotations.ApiModelProperty;

@Entity
@Immutable
@JsonPropertyOrder({ "id", "name" })
public class RoadDistrict {

    @Id
    @GenericGenerator(name = "SEQ_ROAD_DISTRICT", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_ROAD_DISTRICT"))
    @GeneratedValue(generator = "SEQ_ROAD_DISTRICT")
    @JsonIgnore
    private Long id;

    @ApiModelProperty(value = "Road district id (ELY)")
    @JsonProperty(value = "id")
    private int naturalId;

    @ApiModelProperty(value = "Road district name")
    private String name;

    @JsonIgnore
    private boolean obsolete;

    @JsonIgnore
    private LocalDate obsoleteDate;

    @JsonIgnore
    private Integer speedLimitSeason;

    @ApiModelProperty("Road district speed limit season")
    @JsonProperty("speedLimitSeason")
    public SpeedLimitSeason getSpeedLimitSeason() {
        return speedLimitSeason.equals(SpeedLimitSeason.SUMMER.getCode()) ? SpeedLimitSeason.SUMMER : SpeedLimitSeason.WINTER;
    }

    public int getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(final int naturalId) {
        this.naturalId = naturalId;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(final boolean obsolete) {
        this.obsolete = obsolete;
    }

    public LocalDate getObsoleteDate() {
        return obsoleteDate;
    }

    public void setObsoleteDate(final LocalDate obsoleteDate) {
        this.obsoleteDate = obsoleteDate;
    }

    @JsonIgnore
    public Integer getSpeedLimitSeasonCode() {
        return speedLimitSeason;
    }

    public void setSpeedLimitSeason(final Integer speedLimitSeason) {
        this.speedLimitSeason = speedLimitSeason;
    }

    @Override
    public String toString() {
        return new ToStringHelper(this)
            .appendField("id", getId())
            .appendField("naturalId", getNaturalId())
            .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RoadDistrict that = (RoadDistrict) o;

        return new EqualsBuilder()
            .append(naturalId, that.naturalId)
            .append(obsolete, that.obsolete)
            .append(id, that.id)
            .append(name, that.name)
            .append(obsoleteDate, that.obsoleteDate)
            .append(speedLimitSeason, that.speedLimitSeason)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(id)
            .append(naturalId)
            .append(name)
            .append(obsolete)
            .append(obsoleteDate)
            .append(speedLimitSeason)
            .toHashCode();
    }
}
