package fi.livi.digitraffic.tie.dto.v1.camera;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PresetHistoryChange", description = "Camera preset status change in history.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface PresetHistoryChangeDto {

    @Schema(description = "Camera id")
    String getCameraId();

    @Schema(description = "Camera preset id")
    String getPresetId();

    @JsonIgnore
    @Schema(description = "Previous state for publicity")
    Boolean getPublishableFrom();

    @Schema(description = "New state for publicity")
    Boolean getPublishableTo();

    /* For some reason ZonedDateTime is not working here. It gives error:
       "java.lang.IllegalArgumentException: Projection type must be an interface" when value is asked */
    @Schema(description = "The time when change takes place. Also the last modified date of the image in history.")
    Instant getLastModified();

    @Schema(description = "Modification time of the history.")
    Instant getModified();
}