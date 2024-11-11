package fi.livi.digitraffic.tie.model.roadstation;

import java.time.Instant;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;

@Entity
@Immutable
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
    private Instant measuredTime;

    @Column(name = "time_window_start")
    private Instant timeWindowStart;

    @Column(name = "time_window_end")
    private Instant timeWindowEnd;

    @Enumerated(EnumType.STRING)
    private SensorValueReliability reliability;

    public SensorValueHistory() {}

    public SensorValueHistory(final long roadStationId,
                              final long sensorId,
                              final Double value,
                              final Instant measuredTime,
                              final SensorValueReliability reliability) {
        this.roadStationId = roadStationId;
        this.sensorId = sensorId;
        this.sensorValue = value;
        this.measuredTime = measuredTime;
        this.reliability = reliability;
    }

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

    public Instant getMeasuredTime() {
        return measuredTime;
    }

    public void setMeasuredTime(final Instant measuredTime) {
        this.measuredTime = measuredTime;
    }

    public Long getRoadStationId() {
        return roadStationId;
    }

    public void setRoadStationId(final Long roadStationId) {
        this.roadStationId = roadStationId;
    }

    public Instant getTimeWindowStart() {
        return timeWindowStart;
    }

    public void setTimeWindowStart(final Instant timeWindowStart) {
        this.timeWindowStart = timeWindowStart;
    }

    public Instant getTimeWindowEnd() {
        return timeWindowEnd;
    }

    public void setTimeWindowEnd(final Instant timeWindowEnd) {
        this.timeWindowEnd = timeWindowEnd;
    }

    public void setReliability(final SensorValueReliability reliability) {
        this.reliability = reliability;
    }

    public SensorValueReliability getReliability() {
        return reliability;
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
            ", reliability=" + reliability +
            '}';
    }
}
