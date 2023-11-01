package fi.livi.digitraffic.tie.dto.weather.forecast.v1;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.data.v1.DataUpdatedSupportV1;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonPropertyOrder({ "id", "forecasts" })
@Schema(description = "Forecast section weather forecasts")
public class ForecastSectionWeatherDtoV1 implements DataUpdatedSupportV1 {

    @Schema(description =
        "VERSION 1: Forecast section identifier 15 characters ie. 00004_112_000_0: <br>\n" +
        "1. Road number 5 characters ie. 00004, <br>\n" +
        "2. Road section 3 characters ie. 112, <br>\n" +
        "3. Road section version 3 characters ie. 000, <br>\n" +
        "4. Reserved for future needs 1 characters default 0 <br>\n<br>\n" +
        "VERSION 2: Forecast section identifier ie. 00004_342_01435_0_274.569: <br>\n" +
        "1. Road number 5 characters ie. 00004, <br>\n" +
        "2. Road section 3 characters ie. 342, <br>\n" +
        "3. Start distance 5 characters ie. 000, <br>\n" +
        "4. Carriageway 1 character, <br>\n" +
        "5. Measure value of link start point. Varying number of characters ie. 274.569, <br>\n" +
        "Refers to Digiroad content at https://aineistot.vayla.fi/digiroad/")
    @JsonProperty("id")
    public final String id;

    @Schema(description = "Forecast section's weather forecasts")
    public final List<ForecastSectionWeatherForecastDtoV1> forecasts;

    private final Instant lastModifiedFallBack;

    public ForecastSectionWeatherDtoV1(final String naturalId, final List<ForecastSectionWeatherForecastDtoV1> forecasts, final Instant lastModifiedFallBack) {
        this.id = naturalId;
        this.forecasts = forecasts;
        this.lastModifiedFallBack = lastModifiedFallBack;
    }

    @Override
    public Instant getDataUpdatedTime() {
        return forecasts.stream().map(ForecastSectionWeatherForecastDtoV1::getLastModified).filter(Objects::nonNull).max(Comparator.naturalOrder()).orElse(lastModifiedFallBack);
    }
}
