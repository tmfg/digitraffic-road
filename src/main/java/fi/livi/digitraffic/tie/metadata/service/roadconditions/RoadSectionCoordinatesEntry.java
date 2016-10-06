package fi.livi.digitraffic.tie.metadata.service.roadconditions;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public class RoadSectionCoordinatesEntry {

    @JsonProperty("name")
    private String name;

    @JsonProperty("coord")
    private List<List<BigDecimal>> coordinates;

    public String getName() {
        return name;
    }

    public List<List<BigDecimal>> getCoordinates() {
        return coordinates;
    }
}
