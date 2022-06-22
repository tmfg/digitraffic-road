package fi.livi.digitraffic.tie.dto.weathercam.v1;

import java.time.Instant;
import java.util.List;

import fi.livi.digitraffic.tie.dto.data.v1.StationsDatasV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Weathercam stations' data")
public class WeathercamStationsDatasV1 extends StationsDatasV1<String, WeathercamStationDataV1> {

    public WeathercamStationsDatasV1(final Instant measuredTime, List<WeathercamStationDataV1> stationsData) {
        super(measuredTime, stationsData);
    }

    public WeathercamStationsDatasV1(final Instant measuredTime) {
        super(measuredTime);
    }
}
