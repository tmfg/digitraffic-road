package fi.livi.digitraffic.tie.model;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;

@Entity
public class SensorValueHistory {
    @Id
    @GenericGenerator(name = "SEQ_SENSOR_VALUE_HISTORY", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_SENSOR_VALUE_HISTORY"))
    @GeneratedValue(generator = "SEQ_SENSOR_VALUE_HISTORY")
    private Long id;

    @CsvBindByPosition(position = 0, required = true)
    @Column(name = "road_station_id")
    private Long roadStationId;

    @CsvBindByPosition(position = 1, required = true)
    @Column(name = "road_station_sensor_id")
    private Long sensorId;

    @CsvBindByPosition(position = 2, required = true)
    @Column(name = "value")
    private Double sensorValue;

    @CsvBindByPosition(position = 3, required = true)
    @CsvDate("yyyy-MM-dd HH:mm:ss\'Z\'")
    @Column(name = "measured")
    private ZonedDateTime measuredTime;

    @CsvBindByPosition(position = 4)
    @CsvDate("yyyy-MM-dd HH:mm:ss\'Z\'")
    @Column(name = "time_window_start")
    private ZonedDateTime timeWindowStart;

    @CsvBindByPosition(position = 5)
    @CsvDate("yyyy-MM-dd HH:mm:ss\'Z\'")
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
