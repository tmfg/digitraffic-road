package fi.livi.digitraffic.tie.metadata.service.roadconditions;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class RoadSectionCoordinatesDto {

    private final String naturalId;

    private final String name;

    private final List<Coordinate> coordinates;

    public RoadSectionCoordinatesDto(String naturalId, String name, List<List<BigDecimal>> coordinates) {
        this.naturalId = naturalId;
        this.name = name;
        this.coordinates = coordinates.stream().map(c -> new Coordinate(c)).collect(Collectors.toList());
    }

    public String getNaturalId() {
        return naturalId;
    }

    public String getName() {
        return name;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }
}
