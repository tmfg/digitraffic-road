package fi.livi.digitraffic.tie.dto.v1.camera;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CameraHistoryChanges", description = "Weather cameras history changes.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CameraHistoryChangesDto {

    @Schema(description = "Latest history change time. Use this value as parameter for next query in api.")
    public final Instant latestChange;

    @Schema(description = "Changes of weather camera history")
    public final List<PresetHistoryChangeDto> changes;

    public CameraHistoryChangesDto(final Instant latestChange, final List<PresetHistoryChangeDto> changes) {
        this.latestChange = latestChange;
        this.changes = changes;
    }
}
