package fi.livi.digitraffic.tie.dto.weather;

import java.time.Instant;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;

import fi.livi.digitraffic.tie.dto.v1.SensorValueHistoryDto;
import fi.livi.digitraffic.tie.model.roadstation.SensorValueReliability;

public class WeatherSensorValueHistoryDto implements SensorValueHistoryDto {

    @CsvBindByPosition(position = 0, required = true)
    private long roadStationId;

    @CsvBindByPosition(position = 1, required = true)
    private long sensorId;

    @CsvBindByPosition(position = 2, required = true)
    private double sensorValue;

    @CsvBindByPosition(position = 3, required = true)
    // See https://stackoverflow.com/questions/71771439/parsing-instant-using-opencsv
    @CsvDate(
        value = "yyyy-MM-dd'T'HH:mm:ssX", // For reading csv file
        writeFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'",
        writeFormatEqualsReadFormat = false)
    private Instant measured;

    private SensorValueReliability reliability;

    public WeatherSensorValueHistoryDto() {
        // For open csv
    }

    public WeatherSensorValueHistoryDto(final long roadStationId, final long sensorId, final double sensorValue,
                                        final Instant measured, final SensorValueReliability reliability) {
        this.roadStationId = roadStationId;
        this.sensorId = sensorId;
        this.sensorValue = sensorValue;
        this.measured = measured;
        this.reliability = reliability;
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

    @Override
    public SensorValueReliability getReliability() {
        return reliability;
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

    @Override
    public String toString() {
        return "WeatherSensorValueHistoryDto{" +
            "roadStationId=" + roadStationId +
            ", sensorId=" + sensorId +
            ", sensorValue=" + sensorValue +
            ", measured=" + measured +
            '}';
    }
}
