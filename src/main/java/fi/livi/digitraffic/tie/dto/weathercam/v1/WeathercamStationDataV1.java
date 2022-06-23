package fi.livi.digitraffic.tie.dto.weathercam.v1;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import fi.livi.digitraffic.tie.dto.data.v1.StationDataV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Weathercam stations' data")
public class WeathercamStationDataV1 extends StationDataV1<String> {

    @Schema(description = "Id of the weathercam station", required = true)
    public final List<WeathercamPresetDataV1> presets;

    public WeathercamStationDataV1(final String id, List<WeathercamPresetDataV1> presetsData, final Instant dataUpdatedTime) {
        super(id, dataUpdatedTime);
        this.presets = presetsData;
    }

    public WeathercamStationDataV1(final String id) {
        super(id);
        this.presets = Collections.emptyList();
    }
}
