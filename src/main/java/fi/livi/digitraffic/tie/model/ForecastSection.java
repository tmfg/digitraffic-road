package fi.livi.digitraffic.tie.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Immutable
public class ForecastSection {
    @Id
    @JsonProperty("id")
    private String naturalId;

    private int roadSectionNumber;
    private String description;
    private int roadNumber;
    private int startSectionNumber;
    private int startDistance;
    private int endSectionNumber;
    private int endDistance;
    private int length;

    public String getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(final String naturalId) {
        this.naturalId = naturalId;
    }

    public int getRoadSectionNumber() {
        return roadSectionNumber;
    }

    public void setRoadSectionNumber(final int roadSectionNumber) {
        this.roadSectionNumber = roadSectionNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public int getRoadNumber() {
        return roadNumber;
    }

    public void setRoadNumber(final int roadNumber) {
        this.roadNumber = roadNumber;
    }

    public int getStartSectionNumber() {
        return startSectionNumber;
    }

    public void setStartSectionNumber(final int startSectionNumber) {
        this.startSectionNumber = startSectionNumber;
    }

    public int getStartDistance() {
        return startDistance;
    }

    public void setStartDistance(final int startDistance) {
        this.startDistance = startDistance;
    }

    public int getEndSectionNumber() {
        return endSectionNumber;
    }

    public void setEndSectionNumber(final int endSectionNumber) {
        this.endSectionNumber = endSectionNumber;
    }

    public int getEndDistance() {
        return endDistance;
    }

    public void setEndDistance(final int endDistance) {
        this.endDistance = endDistance;
    }

    public int getLength() {
        return length;
    }

    public void setLength(final int length) {
        this.length = length;
    }
}
