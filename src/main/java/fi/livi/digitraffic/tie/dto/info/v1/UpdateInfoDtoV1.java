package fi.livi.digitraffic.tie.dto.info.v1;

import java.time.Instant;

import fi.livi.digitraffic.tie.dto.data.v1.DataUpdatedSupportV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Info about API's data update times")
public class UpdateInfoDtoV1 implements DataUpdatedSupportV1 {

    @Schema(description = "Api url")
    final public String api;

    @Schema(description = "More spesific info about api. (ie domain info)")
    final public String subtype;

    @Schema(description = "Data last updated time", required = true)
    private final Instant dataUpdatedTime;

    @Schema(description = "Data last checked time for updates", required = true)
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
