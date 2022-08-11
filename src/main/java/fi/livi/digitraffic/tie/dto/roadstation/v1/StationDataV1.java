package fi.livi.digitraffic.tie.dto.roadstation.v1;

import java.io.Serializable;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "Data from road station")
@JsonPropertyOrder({ "id", "dataUpdatedTime" })
public abstract class StationDataV1<ID_TYPE> implements Serializable {

    @Schema(description = "Id of the road station", required = true)
    public final ID_TYPE id;

    @Schema(description = "Time when data was last updated")
    public final Instant dataUpdatedTime;

    public StationDataV1(final ID_TYPE id, final Instant dataUpdatedTime) {
        this.id = id;
        this.dataUpdatedTime = dataUpdatedTime;
    }

    public StationDataV1(final ID_TYPE id) {
        this.id = id;
        this.dataUpdatedTime = null;
    }
}