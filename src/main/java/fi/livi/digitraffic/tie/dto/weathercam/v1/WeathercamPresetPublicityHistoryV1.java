package fi.livi.digitraffic.tie.dto.weathercam.v1;

import java.time.Instant;

import fi.livi.digitraffic.tie.dto.roadstation.v1.StationMeasurementDataV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Weathercam station preset's publicity changes")
public class WeathercamPresetPublicityHistoryV1 extends StationMeasurementDataV1<String> {

    @Schema(description = "New state for publicity")
    public boolean publishableTo;

    @Schema(description = "Modification time of the history.")
    public final Instant modifiedTime;

    public WeathercamPresetPublicityHistoryV1(final String id, final Instant measuredTime, final Instant modifiedTime, final boolean publishableTo) {
        super(id, measuredTime);
        this.modifiedTime = modifiedTime;
        this.publishableTo = publishableTo;
    }

    @Schema(description = "Id of the weathercam preset", required = true)
    public String getId() {
        return id;
    }

    @Schema(description = "The time when change took place. Same as the last modified date of the related image version.")
    public Instant getMeasuredTime() {
        return super.measuredTime;
    }
}
