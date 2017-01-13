package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
public class ForecastSectionCoordinatesEntry {

    private final String name;

    private final List<List<BigDecimal>> coordinates;

    public ForecastSectionCoordinatesEntry(@JsonProperty("name") String name,
                                           @JsonProperty("coord") List<List<BigDecimal>> coordinates) {
        this.name = name;
        this.coordinates = coordinates;
    }

    public String getName() {
        return name;
    }

    public List<List<BigDecimal>> getCoordinates() {
        return coordinates;
    }
}
