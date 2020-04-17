package fi.livi.digitraffic.tie.model;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Parameter;

@Entity
@Immutable
@DynamicUpdate(false)
public class SensorValueHistory {
    @Id
    @GenericGenerator(name = "SEQ_SENSOR_VALUE_HISTORY", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_SENSOR_VALUE_HISTORY"))
    @GeneratedValue(generator = "SEQ_SENSOR_VALUE_HISTORY")
    private Long id;

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

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSensorId() {
        return sensorId;
    }

    public void setSensorId(Long sensorId) {
        this.sensorId = sensorId;
    }

    public Double getSensorValue() {
        return sensorValue;
    }

    public void setSensorValue(Double sensorValue) {
        this.sensorValue = sensorValue;
    }

    public ZonedDateTime getMeasuredTime() {
        return measuredTime;
    }

    public void setMeasuredTime(ZonedDateTime measuredTime) {
        this.measuredTime = measuredTime;
    }

    public Long getRoadStationId() {
        return roadStationId;
    }

    public void setRoadStationId(Long roadStationId) {
        this.roadStationId = roadStationId;
    }

    public ZonedDateTime getTimeWindowStart() {
        return timeWindowStart;
    }

    public void setTimeWindowStart(ZonedDateTime timeWindowStart) {
        this.timeWindowStart = timeWindowStart;
    }

    public ZonedDateTime getTimeWindowEnd() {
        return timeWindowEnd;
    }

    public void setTimeWindowEnd(ZonedDateTime timeWindowEnd) {
        this.timeWindowEnd = timeWindowEnd;
    }
}
