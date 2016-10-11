package fi.livi.digitraffic.tie.metadata.service.roadconditions;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
public class RoadSectionCoordinatesEntry {

    private final String name;

    private final List<List<BigDecimal>> coordinates;

    public RoadSectionCoordinatesEntry(@JsonProperty("name") String name,
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
