package fi.livi.digitraffic.tie.dto.roadstation.v1;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.common.dto.LastModifiedSupport;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonPropertyOrder({ "dataUpdatedTime", "dataLastCheckedTime", "type", "features" })
public class StationMetadataDtoV1 implements LastModifiedSupport {

    @Schema(description = "Data last updated date time", requiredMode = Schema.RequiredMode.REQUIRED)
    public final Instant dataUpdatedTime;

    @Schema(description = "Data last checked date time", requiredMode = Schema.RequiredMode.REQUIRED)
    public final Instant dataLastCheckedTime;

    public StationMetadataDtoV1(final Instant dataUpdatedTime,
                                final Instant dataLastCheckedTime) {
        this.dataUpdatedTime = dataUpdatedTime;
        this.dataLastCheckedTime = dataLastCheckedTime;
    }

    @Override
    public Instant getLastModified() {
        return dataUpdatedTime;
    }
}
