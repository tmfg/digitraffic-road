package fi.livi.digitraffic.tie.dto.maintenance.v1;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonPropertyOrder({ "name", "source" })
@Schema(description = "Maintenance tracking domain", name = "DomainV1")
public interface DomainDto {

    @Schema(description = "Name of the maintenance tracking domain", required = true)
    @NotNull
    String getName();

    @Schema(description = "Source and owner of the data", required = true)
    @NotNull
    String getSource();
}
