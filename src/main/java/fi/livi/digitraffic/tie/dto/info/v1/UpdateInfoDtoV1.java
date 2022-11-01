package fi.livi.digitraffic.tie.dto.info.v1;

import java.time.Duration;
import java.time.Instant;

import fi.livi.digitraffic.tie.dto.data.v1.DataUpdatedSupportV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Info about API's data updates")
public class UpdateInfoDtoV1 implements DataUpdatedSupportV1 {

    @Schema(description = "Url of the API", required = true)
    final public String api;

    @Schema(description = "More specific info about API. Ie. domain info.")
    final public String subtype;

    @Schema(description = "Latest update of data")
    private final Instant dataUpdatedTime;

    @Schema(description = "Latest check for updates")
    public final Instant dataCheckedTime;

    @Schema(description = "Data update interval. " +
                          "If the interval is P0S that means that data is updated nearly in real time. " +
                          "If value is null then data is static and it is only updated when needed.")
    public final Duration dataUpdateInterval;

    public UpdateInfoDtoV1(final String api,
                           final String subtype,
                           final Instant dataUpdatedTime,
                           final Instant dataCheckedTime,
                           final Duration dataUpdateInterval) {
        this.api = api;
        this.subtype = subtype;
        this.dataUpdatedTime = dataUpdatedTime;
        this.dataCheckedTime = dataCheckedTime;
        this.dataUpdateInterval = dataUpdateInterval;
    }

    public UpdateInfoDtoV1(final String api,
                           final Instant dataUpdatedTime,
                           final Instant dataCheckedTime,
                           final Duration dataUpdateInterval) {
        this(api, null, dataUpdatedTime, dataCheckedTime, dataUpdateInterval);
    }

    public UpdateInfoDtoV1(final String api,
                           final Instant dataUpdatedTime,
                           final Duration dataUpdateInterval) {
        this(api, dataUpdatedTime, null, dataUpdateInterval);
    }

    @Override
    public Instant getDataUpdatedTime() {
        return dataUpdatedTime;
    }
}
