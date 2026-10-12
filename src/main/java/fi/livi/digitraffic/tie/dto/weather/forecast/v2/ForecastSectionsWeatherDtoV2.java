package fi.livi.digitraffic.tie.dto.weather.forecast.v2;

import java.time.Instant;
import java.util.List;

import fi.livi.digitraffic.tie.dto.data.v1.DataDtoV1;
import io.swagger.v3.oas.annotations.media.Schema;

public class ForecastSectionsWeatherDtoV2 extends DataDtoV1 {

    public static final String ID_DESCRIPTION =
        "Forecast section identifier ie. 00004_342_01435_0_274.569: <br>\n" +
        "1. Road number 5 characters ie. 00004, <br>\n" +
        "2. Road section 3 characters ie. 342, <br>\n" +
        "3. Start distance 5 characters ie. 01435, <br>\n" +
        "4. Carriageway 1 character, <br>\n" +
        "5. Measure value of link start point. Varying number of characters ie. 274.569, <br>\n" +
        "Refers to Digiroad content at https://aineistot.vayla.fi/digiroad/";

    @Schema(description = "Forecast sections")
    public final List<ForecastSectionWeatherDetailedDtoV2> forecastSections;

    public ForecastSectionsWeatherDtoV2(final Instant dataUpdatedTime) {
        this(dataUpdatedTime, null);
    }

    public ForecastSectionsWeatherDtoV2(final Instant dataUpdatedTime, final List<ForecastSectionWeatherDetailedDtoV2> forecastSections) {
        super(dataUpdatedTime);
        this.forecastSections = forecastSections;
    }
}
