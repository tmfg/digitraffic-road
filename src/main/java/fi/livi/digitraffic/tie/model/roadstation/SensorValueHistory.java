package fi.livi.digitraffic.tie.model.roadstation;

import java.time.ZonedDateTime;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;

@Entity
@Immutable
@DynamicUpdate(false)
public class SensorValueHistory {
    @Id
    @SequenceGenerator(name = "SEQ_SENSOR_VALUE_HISTORY", sequenceName = "SEQ_SENSOR_VALUE_HISTORY", allocationSize = 1)
    @GeneratedValue(generator = "SEQ_SENSOR_VALUE_HISTORY")
    private Long id;

    // NOTE! roadStationId = id from road_station-table
    @Column(name = "road_station_id")
    private Long roadStationId;

    @Column(name = "road_station_sensor_id")
    private Long sensorId;

    @Column(name = "value")
    private Double sensorValue;

    @Column(name = "measured")
    private ZonedDateTime measuredTime;

    @Column(name = "time_window_start")
    private ZonedDateTime timeWindowStart;

    @Column(name = "time_window_end")
    private ZonedDateTime timeWindowEnd;

    public SensorValueHistory() {}

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getSensorId() {
        return sensorId;
    }

    public void setSensorId(final Long sensorId) {
        this.sensorId = sensorId;
    }

    public Double getSensorValue() {
        return sensorValue;
    }

    public void setSensorValue(final Double sensorValue) {
        this.sensorValue = sensorValue;
    }

    public ZonedDateTime getMeasuredTime() {
        return measuredTime;
    }

    public void setMeasuredTime(final ZonedDateTime measuredTime) {
        this.measuredTime = measuredTime;
    }

    public Long getRoadStationId() {
        return roadStationId;
    }

    public void setRoadStationId(final Long roadStationId) {
        this.roadStationId = roadStationId;
    }

    public ZonedDateTime getTimeWindowStart() {
        return timeWindowStart;
    }

    public void setTimeWindowStart(final ZonedDateTime timeWindowStart) {
        this.timeWindowStart = timeWindowStart;
    }

    public ZonedDateTime getTimeWindowEnd() {
        return timeWindowEnd;
    }

    public void setTimeWindowEnd(final ZonedDateTime timeWindowEnd) {
        this.timeWindowEnd = timeWindowEnd;
    }

    @Override
    public String toString() {
        return "SensorValueHistory{" +
            "id=" + id +
            ", roadStationId=" + roadStationId +
            ", sensorId=" + sensorId +
            ", sensorValue=" + sensorValue +
            ", measuredTime=" + measuredTime +
            ", timeWindowStart=" + timeWindowStart +
            ", timeWindowEnd=" + timeWindowEnd +
            '}';
    }
}
