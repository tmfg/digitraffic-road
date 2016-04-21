package fi.livi.digitraffic.tie.metadata.model;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import io.swagger.annotations.ApiModelProperty;

@Entity
@Immutable
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"roadStationId", "conditionCode", "condition", "conditionUpdatedLocalTime", "conditionUpdatedUtc",
                    "collectionStatusCode", "collectionStatus", "collectionStatusUpdatedLocalTime", "collectionStatusUpdatedUtc"})
public class RoadStationStatus {

    @ApiModelProperty(required = true, value = "Road station id", position = 1)
    @Id
    private int roadStationId;

    @ApiModelProperty(value = "Road station condition code", position = 2)
    private Integer conditionCode;

    @ApiModelProperty(value = "Road station collection status code", position = 7)
    private Integer collectionStatusCode;

    @JsonIgnore
    private LocalDateTime conditionUpdated;

    @JsonIgnore
    private LocalDateTime collectionStatusUpdated;

    public int getRoadStationId() {
        return roadStationId;
    }

    public void setRoadStationId(final int roadStationId) {
        this.roadStationId = roadStationId;
    }

    public Integer getConditionCode() {
        return conditionCode;
    }

    public void setConditionCode(Integer conditionCode) {
        this.conditionCode = conditionCode;
    }

    @ApiModelProperty(value = "Road station condition", position = 4)
    public RoadStationCondition getCondition() {
        return RoadStationCondition.fromConditionCode(conditionCode);
    }

    @ApiModelProperty(value = "Condition updated timestamp in ISO 8601 format with time offsets from UTC (eg. 2016-04-20T12:38:16.328+03:00)", required = true, position = 5)
    public String getConditionUpdatedLocalTime() {
        return ToStringHelpper.toString(conditionUpdated, ToStringHelpper.TimestampFormat.ISO_8601_WITH_ZONE_OFFSET);
    }

    @ApiModelProperty(value = "Condition updated timestamp in ISO 8601 UTC format (eg. 2016-04-20T09:38:16.328Z)", required = true, position = 6)
    public String getConditionUpdatedUtc() {
        return ToStringHelpper.toString(conditionUpdated, ToStringHelpper.TimestampFormat.ISO_8601_UTC);
    }

    public void setCollectionStatusCode(Integer collectionStatusCode) {
        this.collectionStatusCode = collectionStatusCode;
    }

    public Integer getCollectionStatusCode() {
        return collectionStatusCode;
    }

    @ApiModelProperty(value = "Road station collection status", position = 8)
    public RoadStationCollectionStatus getCollectionStatus() {
        return RoadStationCollectionStatus.fromCollectionStatusCode(collectionStatusCode);
    }

    public void setCollectionStatusUpdated(LocalDateTime collectionStatusUpdated) {
        this.collectionStatusUpdated = collectionStatusUpdated;
    }

    public void setConditionUpdated(LocalDateTime conditionUpdated) {
        this.conditionUpdated = conditionUpdated;
    }


    @ApiModelProperty(value = "Collection status updated timestamp in ISO 8601 format with time offsets from UTC (eg. 2016-04-20T12:38:16.328+03:00)", required = true, position = 9)
    public String getCollectionStatusUpdatedLocalTime() {
        return ToStringHelpper.toString(collectionStatusUpdated, ToStringHelpper.TimestampFormat.ISO_8601_WITH_ZONE_OFFSET);
    }

    @ApiModelProperty(value = "Collection status updated timestamp in ISO 8601 UTC format (eg. 2016-04-20T09:38:16.328Z)", required = true, position = 10)
    public String getCollectionStatusUpdatedUtc() {
        return ToStringHelpper.toString(collectionStatusUpdated, ToStringHelpper.TimestampFormat.ISO_8601_UTC);
    }
}
