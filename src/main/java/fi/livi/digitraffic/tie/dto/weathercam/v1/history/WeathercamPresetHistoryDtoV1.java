package fi.livi.digitraffic.tie.dto.weathercam.v1.history;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Weather camera preset's image history.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeathercamPresetHistoryDtoV1 {

    @Schema(description = "Weathercam preset's id", required = true)
    public final String id;

    @Schema(description = "Time when data was last updated")
    public final Instant dataUpdatedTime;

    @Schema(description = "Weathercam preset's history", required = true)
    public final List<WeathercamPresetHistoryItemDtoV1> history;


    public WeathercamPresetHistoryDtoV1(final String presetId, final Instant dataUpdatedTime, final List<WeathercamPresetHistoryItemDtoV1> history) {
        this.id = presetId;
        this.dataUpdatedTime = dataUpdatedTime;
        this.history = history;
    }

}
