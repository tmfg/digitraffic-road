package fi.livi.digitraffic.tie.metadata.service.roadconditions;

import java.math.BigDecimal;
import java.util.List;

public class Coordinate {

    public final BigDecimal longitude;

    public final BigDecimal latitude;

    public Coordinate(List<BigDecimal> coordinate) {
        if (coordinate.size() >= 2) {
            this.longitude = coordinate.get(0);
            this.latitude = coordinate.get(1);
        } else if (coordinate.size() == 1) {
            this.longitude = coordinate.get(0);
            this.latitude = null;
        } else {
            this.longitude = null;
            this.latitude = null;
        }
    }

    public Coordinate(BigDecimal longitude, BigDecimal latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public boolean isValid() {
        return this.longitude != null && this.latitude != null;
    }

    @Override
    public String toString() {
        return "Coordinate{" +
               "longitude=" + longitude +
               ", latitude=" + latitude +
               '}';
    }
}
