package fi.livi.digitraffic.tie.metadata.service.roadconditions;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.util.List;

public class RoadSectionCoordinatesDto {

    private String naturalId;

    private String name;

    private List<Pair<BigDecimal, BigDecimal>> coordinates;

    public RoadSectionCoordinatesDto(String naturalId, String name, List<Pair<BigDecimal, BigDecimal>> coordinates) {
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

    public List<Pair<BigDecimal, BigDecimal>> getCoordinates() {
        return coordinates;
    }
}
