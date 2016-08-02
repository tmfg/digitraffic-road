package fi.livi.digitraffic.tie.data.dto.daydata;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Entity
@Immutable
@ApiModel(value = "LinkMeasurementData")
@JsonPropertyOrder({"fluencyClass", "minute", "averageSpeed", "medianTravelTime", "nobs"})
public class LinkMeasurementDataDto {
    @Id
    @JsonIgnore
    private int rownum;

    @ApiModelProperty(value = "Index of the minute. 0 = 00:00, 60 = 01:00, 1439 = 23:59", required = true)
    private int minute;

    @ApiModelProperty(value = "Median travel time, in seconds", required = true)
    private int medianTravelTime;

    @ApiModelProperty(value = "Average speed, km/h", required = true)
    private double averageSpeed;

    @ApiModelProperty(value = "Fluency class", required = true)
    private int fluencyClass;

    @JsonIgnore
    private int linkId;

    @JsonIgnore
    private LocalDateTime measured;

    public int getMedianTravelTime() {
        return medianTravelTime;
    }

    public void setMedianTravelTime(final int medianTravelTime) {
        this.medianTravelTime = medianTravelTime;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(final double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public int getLinkId() {
        return linkId;
    }

    public void setLinkId(final int linkId) {
        this.linkId = linkId;
    }

    public int getRownum() {
        return rownum;
    }

    public void setRownum(final int rownum) {
        this.rownum = rownum;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(final int minute) {
        this.minute = minute;
    }

    public int getFluencyClass() {
        return fluencyClass;
    }

    public void setFluencyClass(final int fluencyClass) {
        this.fluencyClass = fluencyClass;
    }

    @ApiModelProperty(value = "Number of observations that were used to calculate the median journey time. (-1 = unknown)", required = true)
    int getNobs() {
        return -1;
    }

    public LocalDateTime getMeasured() {
        return measured;
    }

    public void setMeasured(final LocalDateTime measured) {
        this.measured = measured;
    }
}
