package fi.livi.digitraffic.tie.dto.weathercam.v1;

import java.time.Instant;

import fi.livi.digitraffic.tie.dto.roadstation.v1.StationMeasurementDataV1;
import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "Weathercam preset's latest image capture data")
public class WeathercamPresetDataV1 extends StationMeasurementDataV1<String> {

    public WeathercamPresetDataV1(final String id, final Instant measuredTime) {
        super(id, measuredTime);
    }

    @Schema(description = "Id of the weathercam preset", requiredMode = Schema.RequiredMode.REQUIRED)
    public String getId() {
        return id;
    }
}
