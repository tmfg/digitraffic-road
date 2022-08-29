package fi.livi.digitraffic.tie.dto.weathercam.v1;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.roadstation.v1.StationDataV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Weathercam station presets publicity changes")
@JsonPropertyOrder({ "id", "dataUpdatedTime" })
public class WeathercamStationPresetsPublicityHistoryV1 extends StationDataV1<String> {

    @Schema(description = "Id of the weathercam station", required = true)
    public final List<WeathercamPresetPublicityHistoryV1> presets;

    public WeathercamStationPresetsPublicityHistoryV1(final String id, final List<WeathercamPresetPublicityHistoryV1> presetsData, final Instant dataUpdatedTime) {
        super(id, dataUpdatedTime);
        this.presets = presetsData;
    }
}
