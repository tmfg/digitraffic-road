package fi.livi.digitraffic.tie.metadata.service.roadconditions;

import java.math.BigDecimal;
import java.util.List;

public class RoadSectionCoordinatesDto {

    private String naturalId;

    private String name;

    private List<List<BigDecimal>> coordinates;

    public RoadSectionCoordinatesDto(String naturalId, String name, List<List<BigDecimal>> coordinates) {
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

    public List<List<BigDecimal>> getCoordinates() {
        return coordinates;
    }
}
