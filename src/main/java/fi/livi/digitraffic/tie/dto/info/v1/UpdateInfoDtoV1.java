package fi.livi.digitraffic.tie.dto.info.v1;

import java.time.Instant;

import fi.livi.digitraffic.tie.dto.data.v1.DataUpdatedSupportV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Info about API's data updates")
public class UpdateInfoDtoV1 implements DataUpdatedSupportV1 {

    @Schema(description = "Url of the API")
    final public String api;

    @Schema(description = "More specific info about API. Ie. domain info.")
    final public String subtype;

    @Schema(description = "Latest update of data", required = true)
    private final Instant dataUpdatedTime;

    @Schema(description = "Latest check for updates", required = true)
    public final Instant dataCheckedTime;

    public UpdateInfoDtoV1(final String api,
                           final String subtype,
                           final Instant dataUpdatedTime,
                           final Instant dataCheckedTime) {
        this.api = api;
        this.subtype = subtype;
        this.dataUpdatedTime = dataUpdatedTime;
        this.dataCheckedTime = dataCheckedTime;
    }
    public UpdateInfoDtoV1(final String api,
                           final Instant dataUpdatedTime,
                           final Instant dataCheckedTime) {
        this(api, null, dataUpdatedTime, dataCheckedTime);
    }

    public UpdateInfoDtoV1(final String api,
                           final Instant dataUpdatedTime) {
        this(api, null, dataUpdatedTime, null);
    }

    @Override
    public Instant getDataUpdatedTime() {
        return dataUpdatedTime;
    }
}
