package fi.livi.digitraffic.tie.dto.weathercam.v1.history;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import fi.livi.digitraffic.tie.dto.roadstation.v1.StationDataV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PresetHistory", description = "Weather camera preset's image history.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeathercamPresetsHistoryDtoV1 extends StationDataV1<String> {

    @Schema(description = "Weathercam presets histories", required = true)
    public final List<WeathercamPresetHistoryDtoV1> presets;

    public WeathercamPresetsHistoryDtoV1(final String cameraId, final Instant dataUpdatedTime, final List<WeathercamPresetHistoryDtoV1> presetsHistory) {
        super(cameraId, dataUpdatedTime);
        this.presets = presetsHistory;
    }

    @Override
    public boolean shouldContainLastModified() {
        return presets != null && !presets.isEmpty();
    }
}
