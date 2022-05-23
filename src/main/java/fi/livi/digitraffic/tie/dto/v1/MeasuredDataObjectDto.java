package fi.livi.digitraffic.tie.dto.v1;

import java.time.Instant;

import fi.livi.digitraffic.tie.dto.v1.camera.CameraPresetDataDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(subTypes = CameraPresetDataDto.class)
public interface MeasuredDataObjectDto {

    @Schema(description = "Value measured date time")
    Instant getMeasuredTime();
}
