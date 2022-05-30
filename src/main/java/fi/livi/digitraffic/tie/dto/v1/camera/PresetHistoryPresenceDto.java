package fi.livi.digitraffic.tie.dto.v1.camera;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PresetHistoryPresence", description = "Image history status for preset at given time interval.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface PresetHistoryPresenceDto {

    @JsonIgnore
    String getCameraId();

    @Schema(description = "Camera preset id")
    String getPresetId();

    @Schema(description = "If true then there is image history present for the preset at given time interval.")
    boolean isHistoryPresent();

}