package fi.livi.digitraffic.tie.dto.data.v1;

import java.time.Instant;

import fi.livi.digitraffic.common.dto.data.v1.DataUpdatedSupportV1;
import io.swagger.v3.oas.annotations.media.Schema;

public class DataDtoV1 implements DataUpdatedSupportV1 {

    @Schema(description = "Data last updated date time", required = true)
    public final Instant dataUpdatedTime;

    public DataDtoV1(final Instant dataUpdatedTime) {
        this.dataUpdatedTime = dataUpdatedTime;
    }

    @Override
    public Instant getDataUpdatedTime() {
        return dataUpdatedTime;
    }
}
