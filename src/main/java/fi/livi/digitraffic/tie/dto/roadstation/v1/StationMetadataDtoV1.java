package fi.livi.digitraffic.tie.dto.roadstation.v1;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonPropertyOrder({ "dataUpdatedTime", "dataLastCheckedTime", "type", "features" })
public class StationMetadataDtoV1 {

    @Schema(description = "Data last updated date time", required = true)
    public final Instant dataUpdatedTime;

    @Schema(description = "Data last checked date time", required = true)
    public final Instant dataLastCheckedTime;

    public StationMetadataDtoV1(final Instant dataUpdatedTime,
                                final Instant dataLastCheckedTime) {
        this.dataUpdatedTime = dataUpdatedTime;
        this.dataLastCheckedTime = dataLastCheckedTime;
    }
}
