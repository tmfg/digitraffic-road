package fi.livi.digitraffic.tie.dto.weather;

import java.time.Instant;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;

import fi.livi.digitraffic.tie.dto.v1.SensorValueHistoryDto;

public class WeatherSensorValueHistoryDto implements SensorValueHistoryDto {

    @CsvBindByPosition(position = 0, required = true)
    private long roadStationId;

    @CsvBindByPosition(position = 1, required = true)
    private long sensorId;

    @CsvBindByPosition(position = 2, required = true)
    private double sensorValue;

    @CsvBindByPosition(position = 3, required = true)
    @CsvDate("yyyy-MM-dd\'T\'HH:mm:ss\'Z\'")
    private Instant measured;

    public WeatherSensorValueHistoryDto(final long roadStationId, final long sensorId, final double sensorValue, final Instant measured) {
        this.roadStationId = roadStationId;
        this.sensorId = sensorId;
        this.sensorValue = sensorValue;
        this.measured = measured;
    }

    @Override
    public Instant getMeasuredTime() {
        return measured;
    }

    @Override
    public long getRoadStationId() {
        return roadStationId;
    }

    @Override
    public long getSensorId() {
        return sensorId;
    }

    @Override
    public double getSensorValue() {
        return sensorValue;
    }

    public void setRoadStationId(final long roadStationId) {
        this.roadStationId = roadStationId;
    }

    public void setSensorId(final long sensorId) {
        this.sensorId = sensorId;
    }

    public void setSensorValue(final double sensorValue) {
        this.sensorValue = sensorValue;
    }

    public void setMeasured(final Instant measured) {
        this.measured = measured;
    }
}
