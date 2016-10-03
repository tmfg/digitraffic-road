package fi.livi.digitraffic.tie.metadata.service.roadconditions;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class RoadSectionEntry {

    @JsonProperty("name")
    private String name;

    @JsonProperty("coord")
    private List<List<Double>> coordinates;

    public String getName() {
        return name;
    }

    public List<List<Double>> getCoordinates() {
        return coordinates;
    }
}
