package fi.livi.digitraffic.tie.metadata.model;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import io.swagger.annotations.ApiModelProperty;

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


    @ApiModelProperty(value = "Timestamp in ISO 8601 format with time offsets from UTC (eg. 2016-04-20T12:38:16.328+03:00)", required = true)
    public String getLocalTime() {
        return ToStringHelpper.toString(updated, ToStringHelpper.TimestampFormat.ISO_8601_WITH_ZONE_OFFSET);
    }

    @ApiModelProperty(value = "Timestamp in ISO 8601 UTC format (eg. 2016-04-20T09:38:16.328Z)", required = true)
    public String getUtc() {
        return ToStringHelpper.toString(updated, ToStringHelpper.TimestampFormat.ISO_8601_UTC);
    }

}
