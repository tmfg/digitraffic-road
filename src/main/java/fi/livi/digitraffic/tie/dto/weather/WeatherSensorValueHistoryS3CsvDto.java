package fi.livi.digitraffic.tie.dto.weather;

import java.time.Instant;

import org.apache.commons.lang3.NotImplementedException;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;

import fi.livi.digitraffic.tie.dto.v1.SensorValueHistoryDtoV1;
import fi.livi.digitraffic.tie.model.roadstation.SensorValueReliability;

public class WeatherSensorValueHistoryS3CsvDto implements SensorValueHistoryDtoV1 {

    @CsvBindByPosition(position = 0,
                       required = true)
    private long roadStationNaturalId;

    @CsvBindByPosition(position = 1,
                       required = true)
    private long sensorNaturalId;

    @CsvBindByPosition(position = 2,
                       required = true)
    private double value;

    @CsvBindByPosition(position = 3,
                       required = true)
    // See https://stackoverflow.com/questions/71771439/parsing-instant-using-opencsv
    @CsvDate(
            value = "yyyy-MM-dd'T'HH:mm:ssX",
            // For reading csv file
            writeFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'",
            writeFormatEqualsReadFormat = false)
    private Instant measured;

    @CsvBindByPosition(position = 4)
    private SensorValueReliability reliability;

    public WeatherSensorValueHistoryS3CsvDto() {
        // For open csv
    }

    public WeatherSensorValueHistoryS3CsvDto(final long roadStationNaturalId, final long sensorNaturalId, final double value,
                                             final Instant measured, final SensorValueReliability reliability) {
        this.roadStationNaturalId = roadStationNaturalId;
        this.sensorNaturalId = sensorNaturalId;
        this.value = value;
        this.measured = measured;
        this.reliability = reliability;
    }

    @Override
    public Instant getMeasuredTime() {
        return measured;
    }

    @Override
    public SensorValueReliability getReliability() {
        return reliability;
    }

    @Override
    public long getRoadStationNaturalId() {
        return roadStationNaturalId;
    }

    @Override
    public long getSensorNaturalId() {
        return sensorNaturalId;
    }

    @Override
    public Instant getStationLatestMeasuredTime() {
        throw new NotImplementedException();
    }

    @Override
    public Instant getStationLatestModifiedTime() {
        throw new NotImplementedException();
    }

    @Override
    public Instant getModified() {
        throw new NotImplementedException();
    }

    @Override
    public Long getSensorValueId() {
        throw new NotImplementedException();
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "WeatherSensorValueHistoryDto{" +
                "roadStationNaturalId=" + roadStationNaturalId +
                ", sensorNaturalId=" + sensorNaturalId +
                ", value=" + value +
                ", measured=" + measured +
                '}';
    }
}
