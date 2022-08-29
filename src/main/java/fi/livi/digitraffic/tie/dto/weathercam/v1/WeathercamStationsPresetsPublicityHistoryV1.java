package fi.livi.digitraffic.tie.dto.weathercam.v1;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.roadstation.v1.StationsDataV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Weathercam stations presets publicity changes")
@JsonPropertyOrder({ "dataUpdatedTime", "stations"})
public class WeathercamStationsPresetsPublicityHistoryV1 extends StationsDataV1<String, WeathercamStationPresetsPublicityHistoryV1> {

    public WeathercamStationsPresetsPublicityHistoryV1(final Instant measuredTime, final List<WeathercamStationPresetsPublicityHistoryV1> stationsData) {
        super(measuredTime, stationsData);
    }

    @Schema(description = "Latest history change time. Use this value as parameter for next query in api.")
    public Instant getDataUpdatedTime() {
        return dataUpdatedTime;
    }
}
