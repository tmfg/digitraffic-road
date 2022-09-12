package fi.livi.digitraffic.tie.dto.weather.v1;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.roadstation.v1.StationsDataV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Latest measurement data from Weather stations")
@JsonPropertyOrder({ "dataUpdatedTime", "stations"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeatherStationsDataDtoV1 extends StationsDataV1<Long, WeatherStationDataDtoV1> {

    public WeatherStationsDataDtoV1(final List<WeatherStationDataDtoV1> stations, final Instant updated) {
        super(updated, stations);
    }

    public WeatherStationsDataDtoV1(final Instant updated) {
        this(null, updated);
    }
}