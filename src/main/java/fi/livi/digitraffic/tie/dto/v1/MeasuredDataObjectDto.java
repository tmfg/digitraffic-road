package fi.livi.digitraffic.tie.dto.v1;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(subTypes = CameraPresetDataDto.class)
public interface MeasuredDataObjectDto {

    @Schema(description = "Value measured date time")
    Instant getMeasuredTime();
}
