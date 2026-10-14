package fi.livi.digitraffic.tie.dto.weather.forecast.v2;

import java.time.Instant;
import java.util.List;

import fi.livi.digitraffic.tie.dto.data.v1.DataDtoV1;
import io.swagger.v3.oas.annotations.media.Schema;

public class ForecastSectionsWeatherDtoV2 extends DataDtoV1 {

    public static final String ID_DESCRIPTION = """
            Forecast section identifier. Detailed forecast sections ie. 00004_342_01435_0_274.569: <br>
            1. Road number 5 characters ie. 00004, <br>
            2. Road section 3 characters ie. 342, <br>
            3. Start distance 5 characters ie. 01435, <br>
            4. Carriageway 1 character, <br>
            5. Measure value of link start point. Varying number of characters ie. 274.569, <br>
            Refers to Digiroad content at https://aineistot.vayla.fi/digiroad/ <br>
            Simple forecast sections 15 characters ie. 00004_112_000_0: <br>
            1. Road number 5 characters ie. 00004, <br>
            2. Road section 3 characters ie. 112, <br>
            3. Road section version 3 characters ie. 000, <br>
            4. Reserved for future needs 1 character default 0""";

    @Schema(description = "Forecast sections")
    public final List<ForecastSectionWeatherDataDtoV2> forecastSections;

    public ForecastSectionsWeatherDtoV2(final Instant dataUpdatedTime) {
        this(dataUpdatedTime, null);
    }

    public ForecastSectionsWeatherDtoV2(final Instant dataUpdatedTime,
                                        final List<ForecastSectionWeatherDataDtoV2> forecastSections) {
        super(dataUpdatedTime);
        this.forecastSections = forecastSections;
    }
}
