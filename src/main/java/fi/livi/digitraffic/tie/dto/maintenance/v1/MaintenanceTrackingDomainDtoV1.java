package fi.livi.digitraffic.tie.dto.maintenance.v1;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.LastModifiedSupport;
import fi.livi.digitraffic.tie.dto.data.v1.DataUpdatedSupportV1;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonPropertyOrder({ "name", "source" })
@Schema(description = "Maintenance tracking domain")
public interface MaintenanceTrackingDomainDtoV1 extends DataUpdatedSupportV1 {

    @Schema(description = "Name of the maintenance tracking domain", required = true)
    @NotNull
    String getName();

    @Schema(description = "Source and owner of the data", required = true)
    @NotNull
    String getSource();
//    @Schema(description = "Data last updated time", required = true)
//    Instant getDataUpdatedTime();
}
