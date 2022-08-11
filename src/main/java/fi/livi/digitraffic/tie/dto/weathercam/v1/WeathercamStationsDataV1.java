package fi.livi.digitraffic.tie.dto.weathercam.v1;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.roadstation.v1.StationsDataV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Weathercam stations' data")
@JsonPropertyOrder({ "dataUpdatedTime", "stations"})
public class WeathercamStationsDataV1 extends StationsDataV1<String, WeathercamStationDataV1> {

    public WeathercamStationsDataV1(final Instant measuredTime, final List<WeathercamStationDataV1> stationsData) {
        super(measuredTime, stationsData);
    }

    public WeathercamStationsDataV1(final Instant measuredTime) {
        super(measuredTime);
    }
}
