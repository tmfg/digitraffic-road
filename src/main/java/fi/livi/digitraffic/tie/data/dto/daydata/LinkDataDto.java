package fi.livi.digitraffic.tie.data.dto.daydata;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;

@Entity
@Immutable
@JsonPropertyOrder({"fc", "m", "sp", "tt"})
public class LinkDataDto {
    @Id
    @JsonIgnore
    private int rownum;

    @ApiModelProperty(value = "Index of the minute. 0 = 00:00, 60 = 01:00, 1439 = 23:59", required = true)
    @JsonProperty("m")
    private int minute;

    @ApiModelProperty(value = "Median travel time, in seconds", required = true)
    @JsonProperty("tt")
    private int medianTravelTime;

    @ApiModelProperty(value = "Average speed, km/h", required = true)
    @JsonProperty("sp")
    private double averageSpeed;

    @ApiModelProperty(value = "Fluency class", required = true)
    private int fc;

    @JsonIgnore
    private int linkId;

    public int getMedianTravelTime() {
        return medianTravelTime;
    }

    public void setMedianTravelTime(int medianTravelTime) {
        this.medianTravelTime = medianTravelTime;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public int getLinkId() {
        return linkId;
    }

    public void setLinkId(int linkId) {
        this.linkId = linkId;
    }

    public int getRownum() {
        return rownum;
    }

    public void setRownum(int rownum) {
        this.rownum = rownum;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getFc() {
        return fc;
    }

    public void setFc(int fc) {
        this.fc = fc;
    }
}
