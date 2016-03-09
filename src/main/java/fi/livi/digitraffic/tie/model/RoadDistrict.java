package fi.livi.digitraffic.tie.model;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
public class RoadDistrict {

    @Id
    private long id;
    private int naturalId;
    private String name;
    private boolean obsolete;
    private LocalDate obsoleteDate;
    private Integer speedLimitSeason;

    public int getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(final int naturalId) {
        this.naturalId = naturalId;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }

    public LocalDate getObsoleteDate() {
        return obsoleteDate;
    }

    public void setObsoleteDate(LocalDate obsoleteDate) {
        this.obsoleteDate = obsoleteDate;
    }

    public Integer getSpeedLimitSeason() {
        return speedLimitSeason;
    }

    public void setSpeedLimitSeason(Integer speedLimitSeason) {
        this.speedLimitSeason = speedLimitSeason;
    }
}
