package fi.livi.digitraffic.tie.metadata.model;

import java.time.LocalDate;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Immutable
public class RoadStationSensor {
    @JsonIgnore
    @Id
    private long id;

    @JsonProperty("id")
    private long naturalId;

    private String name;

    private String unit;

    @JsonIgnore
    private boolean obsolete;

    @JsonIgnore
    private LocalDate obsoleteDate;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public long getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(final long naturalId) {
        this.naturalId = naturalId;
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

    public String getUnit() {
        return unit;
    }

    public void setUnit(final String unit) {
        this.unit = unit;
    }
}
