package fi.livi.digitraffic.tie.dto;

import java.time.ZonedDateTime;

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
    private ZonedDateTime measured;

    public WeatherSensorValueHistoryDto(final long roadStationId, final long sensorId, final double sensorValue, final ZonedDateTime measured) {
        this.roadStationId = roadStationId;
        this.sensorId = sensorId;
        this.sensorValue = sensorValue;
        this.measured = measured;
    }

    @Override
    public ZonedDateTime getMeasuredTime() {
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

    public void setRoadStationId(long roadStationId) {
        this.roadStationId = roadStationId;
    }

    public void setSensorId(long sensorId) {
        this.sensorId = sensorId;
    }

    public void setSensorValue(double sensorValue) {
        this.sensorValue = sensorValue;
    }

    public void setMeasured(ZonedDateTime measured) {
        this.measured = measured;
    }
}
