package fi.livi.digitraffic.tie.data.dto.camera;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PresetHistoryDto {

    public final String presetId;
    public final List<PresetHistoryDataDto> presetHistory;

    public PresetHistoryDto(final String presetId, final List<PresetHistoryDataDto> presetHistory) {
        this.presetId = presetId;
        this.presetHistory = presetHistory;
    }

    public PresetHistoryDto(String presetId) {
        this(presetId, Collections.emptyList());
    }

    public String getPresetId() {
        return presetId;
    }
}
