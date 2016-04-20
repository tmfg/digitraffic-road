package fi.livi.digitraffic.tie.data.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;

@Entity
@Immutable
@JsonPropertyOrder({ "localTime", "utc"})
public class LamMeasurement {

    @ApiModelProperty(value = "LAM station identifier (naturalId)", required = true)
    @Id
    private long lamId;

    @ApiModelProperty(value = "Traffic volume in 5 minutes to direction 1", required = true)
    private long trafficVolume1;

    @ApiModelProperty(value = "Traffic volume in 5 minutes to direction 2", required = true)
    private long trafficVolume2;

    @ApiModelProperty(value = "Average speed to direction 1", required = true)
    private long averageSpeed1;

    @ApiModelProperty(value = "Average speed to direction 2", required = true)
    private long averageSpeed2;

    @JsonIgnore
    private LocalDateTime measured;

    @JsonIgnore
    @Transient
    private ZonedDateTime measuredZonedDateTime;

    public long getLamId() {
        return lamId;
    }

    public void setLamId(final long lamId) {
        this.lamId = lamId;
    }

    public long getTrafficVolume1() {
        return trafficVolume1;
    }

    public void setTrafficVolume1(final long trafficVolume1) {
        this.trafficVolume1 = trafficVolume1;
    }

    public long getTrafficVolume2() {
        return trafficVolume2;
    }

    public void setTrafficVolume2(final long trafficVolume2) {
        this.trafficVolume2 = trafficVolume2;
    }

    public long getAverageSpeed1() {
        return averageSpeed1;
    }

    public void setAverageSpeed1(final long averageSpeed1) {
        this.averageSpeed1 = averageSpeed1;
    }

    public long getAverageSpeed2() {
        return averageSpeed2;
    }

    public void setAverageSpeed2(final long averageSpeed2) {
        this.averageSpeed2 = averageSpeed2;
    }

    public LocalDateTime getMeasured() {
        return measured;
    }

    public void setMeasured(final LocalDateTime measured) {
        this.measured = measured;
        if (measured != null) {
            measuredZonedDateTime = measured.atZone(ZoneId.systemDefault());
        }
    }

    @ApiModelProperty(value = "Timestamp in ISO 8601 format with time offsets from UTC (eg. 2016-04-20T12:38:16.328+03:00)", required = true)
    public String getLocalTime() {
        if(getMeasuredZonedDateTime() != null) {
            return measuredZonedDateTime.toOffsetDateTime().toString();
        }
        return null;
    }

    @ApiModelProperty(value = "Timestamp in ISO 8601 UTC format (eg. 2016-04-20T09:38:16.328Z)", required = true)
    public String getUtc() {
        if(getMeasuredZonedDateTime() != null) {
            return ZonedDateTime.ofInstant(measuredZonedDateTime.toInstant(), ZoneOffset.UTC).toString();
        }
        return null;
    }

    public ZonedDateTime getMeasuredZonedDateTime() {
        if (measuredZonedDateTime == null && measured != null) {
            measuredZonedDateTime = measured.atZone(ZoneId.systemDefault());
        }
        return measuredZonedDateTime;
    }
}
