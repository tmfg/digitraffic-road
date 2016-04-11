package fi.livi.digitraffic.tie.metadata.model;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Entity
@Immutable
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"stationId", "updated", "roadStationStatus", "stationDataCollectionStatus"})
public class RoadStationStatus {
    @Id
    private int stationId;
    private Integer roadStationStatus;
    private Integer stationDataCollectionStatus;

    private LocalDateTime updated;

    public int getStationId() {
        return stationId;
    }

    public void setStationId(final int stationId) {
        this.stationId = stationId;
    }

    public Integer getRoadStationStatus() {
        return roadStationStatus;
    }

    public void setRoadStationStatus(Integer roadStationStatus) {
        this.roadStationStatus = roadStationStatus;
    }

    public Integer getStationDataCollectionStatus() {
        return stationDataCollectionStatus;
    }

    public void setStationDataCollectionStatus(Integer stationDataCollectionStatus) {
        this.stationDataCollectionStatus = stationDataCollectionStatus;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }
}
