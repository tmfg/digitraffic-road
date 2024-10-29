package fi.livi.digitraffic.tie.dto.weather.v1;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.RoadStationPropertiesDetailedV1;
import fi.livi.digitraffic.tie.model.weather.WeatherStationType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Weather station properties object with basic information")
@JsonPropertyOrder({ "id", "name" })
public class WeatherStationPropertiesDetailedV1 extends RoadStationPropertiesDetailedV1<Long> {

    @Schema(description = "Type of weather station")
    public final WeatherStationType stationType;

    @Schema(description = "Is station master or slave station", requiredMode = Schema.RequiredMode.REQUIRED)
    public final boolean master;

    /** Sensors natural ids */
    @Schema(description = "Weather station sensors ids")
    public final List<Long> sensors;

    public WeatherStationPropertiesDetailedV1(final long id,
                                              final WeatherStationType stationType,
                                              final boolean master, final List<Long> sensors) {
        super(id);
        this.stationType = stationType;
        this.master = master;
        this.sensors = sensors;
    }
}
