package fi.livi.digitraffic.tie.dto.weather.forecast.v1;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.PropertiesV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Forecast Section Properties")
@JsonPropertyOrder({ "id", "description" })
public class ForecastSectionPropertiesV1 extends PropertiesV1 {

    public static final String ID_DESC = "Forecast section identifier ie. 00004_342_01435_0_274.569: \n" +
                                         "1. Road number 5 characters ie. 00004, \n" +
                                         "2. Road section 3 characters ie. 342, \n" +
                                         "3. Start distance 5 characters ie. 000, \n" +
                                         "4. Carriageway 1 character, \n" +
                                         "5. Measure value of link start point. Varying number of characters ie. 274.569, \n" +
                                         "Refers to Digiroad content at https://aineistot.vayla.fi/digiroad/";

    @Schema(description = ID_DESC)
    public final String id;

    @Schema(description = "Forecast section description")
    public final String description;

    @Schema(description = "Road section number")
    public final int roadSectionNumber;

    @Schema(description = "Forecast section road number")
    public final int roadNumber;


    @Schema(description = "Forecast section length in meters")
    public final Integer length;

    @Schema(description = RoadSegmentDtoV1.API_DESCRIPTION)
    public final List<RoadSegmentDtoV1> roadSegments;

    @Schema(description = "Forecast section link indices. Refers to https://aineistot.vayla.fi/digiroad/")
    public final List<Long> linkIds;

    @Schema(description = "Data last updated date time", required = true)
    public final Instant dataUpdatedTime;

    public ForecastSectionPropertiesV1(final String id, final String description, final int roadSectionNumber,
                                       final int roadNumber, final Integer length,
                                       final List<RoadSegmentDtoV1> roadSegments, final List<Long> linkIds,
                                       final Instant dataUpdatedTime) {
        this.id = id;
        this.description = description;
        this.roadSectionNumber = roadSectionNumber;
        this.roadNumber = roadNumber;
        this.length = length;
        this.roadSegments = roadSegments;
        this.linkIds = linkIds;
        this.dataUpdatedTime = dataUpdatedTime;
    }

    @Override
    public Instant getLastModified() {
        return dataUpdatedTime;
    }
}
