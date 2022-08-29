package fi.livi.digitraffic.tie.dto.weathercam.v1;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.roadstation.v1.StationDataV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Weathercam stations' data")
@JsonPropertyOrder({ "id", "dataUpdatedTime" })
public class WeathercamStationDataV1 extends StationDataV1<String> {

    @Schema(description = "Weathercam presets data", required = true)
    public final List<WeathercamPresetDataV1> presets;

    public WeathercamStationDataV1(final String id, List<WeathercamPresetDataV1> presetsData, final Instant dataUpdatedTime) {
        super(id, dataUpdatedTime);
        this.presets = presetsData;
    }
}
