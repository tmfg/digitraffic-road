package fi.livi.digitraffic.tie.dto.trafficmessage.v1.location;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.FeatureV1;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

@JsonPropertyOrder({ "type", "id"})
@Schema(description = "Location GeoJSON feature object")
public final class LocationFeatureV1 extends FeatureV1<Point, LocationPropertiesV1> implements Comparable<LocationFeatureV1> {


    @Schema(description = "Unique locationCode for this location", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    public final int id;

    public LocationFeatureV1(final LocationDtoV1 l, final Instant dataUpdatedTime, final String locationVersion) {
        super(getGeoJSONGeometry(l),
              new LocationPropertiesV1(l.getLocationCode(), l.getSubtypeCode(), l.getRoadJunction(),
                                     l.getRoadName(), l.getFirstName(), l.getSecondName(), l.getAreaRef(),
                                     l.getLinearRef(), l.getNegOffset(), l.getPosOffset(), l.getUrban(),
                                     getEtrsGeometry(l),
                                     l.getNegDirection(), l.getPosDirection(),
                                     l.getGeocode(), l.getOrderOfPoint(), dataUpdatedTime, locationVersion));
        this.id = l.getLocationCode();
    }

    private static Point getGeoJSONGeometry(final LocationDtoV1 l) {
        return l.getWgs84Long() == null || l.getWgs84Lat() == null ? null : new Point(l.getWgs84Long(), l.getWgs84Lat());
    }

    private static List<Double> getEtrsGeometry(final LocationDtoV1 l) {
        return l.getEtrsTm35FinX() == null ? null : Arrays.asList(l.getEtrsTm35FinX(), l.getEtrsTm35FixY());
    }

    @Override
    public int compareTo(final LocationFeatureV1 o) {
        if (equals(o)) {
            return 0;
        }
        return Integer.compare(this.id, o.id);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof LocationFeatureV1)) {
            return false;
        }

        final LocationFeatureV1 that = (LocationFeatureV1) o;

        return new EqualsBuilder().append(id, that.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }
}
