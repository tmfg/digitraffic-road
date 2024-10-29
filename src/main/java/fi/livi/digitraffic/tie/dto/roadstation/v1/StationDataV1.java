package fi.livi.digitraffic.tie.dto.roadstation.v1;

import java.io.Serializable;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.common.dto.LastModifiedSupport;
import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "Data from road station")
@JsonPropertyOrder({ "id", "dataUpdatedTime" })
public abstract class StationDataV1<ID_TYPE> implements Serializable, LastModifiedSupport {

    @Schema(description = "Id of the road station", requiredMode = Schema.RequiredMode.REQUIRED)
    public final ID_TYPE id;

    @Schema(description = "Time when data was last updated")
    public final Instant dataUpdatedTime;

    public StationDataV1(final ID_TYPE id, final Instant dataUpdatedTime) {
        this.id = id;
        this.dataUpdatedTime = dataUpdatedTime;
    }

    @Override
    public Instant getLastModified() {
        return dataUpdatedTime;
    }
}