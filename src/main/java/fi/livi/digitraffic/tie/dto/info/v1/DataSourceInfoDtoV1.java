package fi.livi.digitraffic.tie.dto.info.v1;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.model.DataSource;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonPropertyOrder({ "id", "source" })
@Schema(description = "Data source information")
public interface DataSourceInfoDtoV1 {

    @Schema(description = "Identifier of source")
    @NotNull
    @Enumerated(EnumType.STRING)
    DataSource getId();

    @Schema(description = "Source information like system and owner")
    String getSource();

    @Schema(description = "Data update interval")
    String getUpdateInterval();

    @Schema(description = "Recommended fetch interval for clients")
    String getRecommendedFetchInterval();

}
