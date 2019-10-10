package fi.livi.digitraffic.tie.data.dto.camera;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PresetHistoryDto {

    public final String presetId;
    public final List<PresetHistoryDataDto> history;

    public PresetHistoryDto(final String presetId, final List<PresetHistoryDataDto> history) {
        this.presetId = presetId;
        this.history = history;
    }

    public PresetHistoryDto(String presetId) {
        this(presetId, Collections.emptyList());
    }
}
