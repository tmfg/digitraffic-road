package fi.livi.digitraffic.tie.dto.data.v1;

import java.time.Instant;

import fi.livi.digitraffic.tie.dto.LastModifiedSupport;
import io.swagger.v3.oas.annotations.media.Schema;

public interface DataUpdatedSupportV1 extends LastModifiedSupport {

    @Schema(description = "Data last updated time", required = true)
    Instant getDataUpdatedTime();

    @Override
    default Instant getLastModified() {
        return getDataUpdatedTime();
    }
}
