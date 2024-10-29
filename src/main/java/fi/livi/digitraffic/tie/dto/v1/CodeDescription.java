package fi.livi.digitraffic.tie.dto.v1;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Description of code")
@JsonPropertyOrder({ "description", "descriptionEn", "code" })
public interface CodeDescription {
    @Schema(description = "Code", requiredMode = Schema.RequiredMode.REQUIRED)
    String getCode();
    @Schema(description = "Description of the code (Finnish)", requiredMode = Schema.RequiredMode.REQUIRED)
    String getDescription();
    @Schema(description = "Description of the code(English", requiredMode = Schema.RequiredMode.REQUIRED)
    String getDescriptionEn();
}
