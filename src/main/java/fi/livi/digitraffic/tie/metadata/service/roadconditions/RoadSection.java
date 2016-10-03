package fi.livi.digitraffic.tie.metadata.service.roadconditions;

import java.util.List;

public class RoadSection {

    private String naturalId;

    private String name;

    private List<List<Double>> coordinates;

    public RoadSection(String naturalId, String name, List<List<Double>> coordinates) {
        this.naturalId = naturalId;
        this.name = name;
        this.coordinates = coordinates;
    }

    public String getNaturalId() {
        return naturalId;
    }

    public String getName() {
        return name;
    }

    public List<List<Double>> getCoordinates() {
        return coordinates;
    }
}
