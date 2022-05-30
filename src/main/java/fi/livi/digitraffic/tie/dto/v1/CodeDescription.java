package fi.livi.digitraffic.tie.dto.v1;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Description of code")
public interface CodeDescription {
    @Schema(description = "Code", required = true)
    String getCode();
    @Schema(description = "Description of the code (Finnish)", required = true)
    String getDescription();
}
