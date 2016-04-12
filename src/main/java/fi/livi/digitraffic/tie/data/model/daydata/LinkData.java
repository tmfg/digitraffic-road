package fi.livi.digitraffic.tie.data.model.daydata;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Entity
@Immutable
@JsonPropertyOrder({"fc", "m", "sp", "tt"})
public class LinkData {
    @Id
    @JsonIgnore
    private int rownum;

    @JsonProperty("m")
    private int minute;

    @JsonProperty("tt")
    private int medianTravelTime;

    @JsonProperty("sp")
    private double averageSpeed;

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
