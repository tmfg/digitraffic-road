package fi.livi.digitraffic.tie.dto.weather.v1.forecast;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.PropertiesV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Simple forecast section properties")
@JsonPropertyOrder({ "id", "description", "roadSectionNumber", "roadNumber", "roadSectionVersionNumber", "startDistance",
                     "endDistance", "length", "road", "startRoadSection", "endRoadSection" })
public class ForecastSectionPropertiesSimpleV1 extends PropertiesV1 {

    @Schema(description =
            "Forecast section identifier 15 characters ie. 00004_112_000_0: \n" +
            "1. Road number 5 characters ie. 00004, \n" +
            "2. Road section 3 characters ie. 112, \n" +
            "3. Road section version 3 characters ie. 000, \n" +
            "4. Reserved for future needs 1 characters default 0")
    public final String id;

    @Schema(description = "Forecast section description")
    public final String description;

    @Schema(description = "Road section number")
    public final int roadSectionNumber;

    @Schema(description = "Forecast section road number")
    public final int roadNumber;

    @Schema(description = "Road section version number")
    public final int roadSectionVersionNumber;

    @Schema(description = "Data last updated date time", required = true)
    public final Instant dataUpdatedTime;

    public ForecastSectionPropertiesSimpleV1(final String id, final String description, final int roadSectionNumber,
                                             final int roadSectionVersionNumber, final int roadNumber, final Instant dataUpdatedTime) {
        this.id = id;
        this.description = description;
        this.roadSectionNumber = roadSectionNumber;
        this.roadSectionVersionNumber = roadSectionVersionNumber;
        this.roadNumber = roadNumber;
        this.dataUpdatedTime = dataUpdatedTime;
    }


    @Override
    public Instant getLastModified() {
        return dataUpdatedTime;
    }
}
