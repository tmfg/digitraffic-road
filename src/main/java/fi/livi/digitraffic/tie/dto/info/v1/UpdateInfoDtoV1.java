package fi.livi.digitraffic.tie.dto.info.v1;

import java.time.Instant;

import fi.livi.digitraffic.tie.dto.data.v1.DataUpdatedSupportV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Info about API's data updates")
public class UpdateInfoDtoV1 implements DataUpdatedSupportV1 {

    @Schema(description = "Url of the API", required = true)
    final public String api;

    @Schema(description = "More specific info about API. Ie. domain info.")
    final public String subtype;

    @Schema(description = "Latest update of data", required = false) // Override super required=true
    private final Instant dataUpdatedTime;

    @Schema(description = "Latest check for data updates.  <br>\n" +
                          "`null` value indicates data being pushed to our platform or data is static and is only updated when needed.")
    public final Instant dataCheckedTime;

    @Schema(description = "Data update interval in ISO-8601 duration format `PnDTnHnMn.nS`. <br>\n" +
                          "If the interval is `P0S` that means that data is updated nearly in real time. <br>\n" +
                          "If value is `null` then data is static and it is only updated when needed.",
            type = "string",
            example = "[PT5M, P1H]")
    public final String dataUpdateInterval;

    @Schema(description = "Recommended fetch interval for clients in ISO-8601 duration format `PnDTnHnMn.nS`",
            type = "string",
            example = "[PT5M, P1H]")
    public final String recommendedFetchInterval;

    public UpdateInfoDtoV1(final String api,
                           final String subtype,
                           final Instant dataUpdatedTime,
                           final Instant dataCheckedTime,
                           final String dataUpdateInterval,
                           final String recommendedFetchInterval) {
        this.api = api;
        this.subtype = subtype;
        this.dataUpdatedTime = dataUpdatedTime;
        this.dataCheckedTime = dataCheckedTime;
        this.dataUpdateInterval = dataUpdateInterval;
        this.recommendedFetchInterval = recommendedFetchInterval;
    }

    public UpdateInfoDtoV1(final String api,
                           final Instant dataUpdatedTime,
                           final Instant dataCheckedTime,
                           final String dataUpdateInterval,
                           final String recommendedFetchInterval) {
        this(api, null, dataUpdatedTime, dataCheckedTime, dataUpdateInterval, recommendedFetchInterval);
    }

    public UpdateInfoDtoV1(final String api,
                           final Instant dataUpdatedTime,
                           final String dataUpdateInterval,
                           final String recommendedFetchInterval) {
        this(api, dataUpdatedTime, null, dataUpdateInterval, recommendedFetchInterval);
    }

    @Override
    public Instant getDataUpdatedTime() {
        return dataUpdatedTime;
    }
}
