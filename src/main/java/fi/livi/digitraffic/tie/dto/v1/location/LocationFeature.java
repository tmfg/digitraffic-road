package fi.livi.digitraffic.tie.dto.v1.location;

import java.util.Arrays;
import java.util.List;

import fi.livi.digitraffic.tie.dto.trafficmessage.v1.location.LocationDtoV1;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public final class LocationFeature implements Comparable<LocationFeature> {

    @Schema(description = "\"Feature\": GeoJSON Feature Object", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "Feature")
    public final String type = "Feature";

    @Schema(description = "Unique locationCode for this location", requiredMode = Schema.RequiredMode.REQUIRED)
    public final int id;

    @Schema(description = "GeoJSON Point Geometry Object. Point where station is located", requiredMode = Schema.RequiredMode.REQUIRED)
    public final Point geometry;

    @Schema(description = "Location properties.", requiredMode = Schema.RequiredMode.REQUIRED)
    public final LocationProperties properties;

    public LocationFeature(final LocationDtoV1 l) {
        this.id = l.getLocationCode();
        this.geometry = getGeometry(l);
        this.properties = new LocationProperties(
                l.getLocationCode(),
                l.getSubtypeCode(),
                l.getRoadJunction(),
                l.getRoadName(), l.getFirstName(), l.getSecondName(), l.getAreaRef(),
                l.getLinearRef(), l.getNegOffset(), l.getPosOffset(), l.getUrban(),
                getEtrsGeometry(l),
                l.getNegDirection(), l.getPosDirection(),
                l.getGeocode(), l.getOrderOfPoint());
    }

    private static Point getGeometry(final LocationDtoV1 l) {
        return l.getWgs84Lat() == null ? null : new Point(l.getWgs84Long(), l.getWgs84Lat());
    }

    private static List<Double> getEtrsGeometry(final LocationDtoV1 l) {
        return l.getEtrsTm35FinX() == null ? null : Arrays.asList(l.getEtrsTm35FinX(), l.getEtrsTm35FixY());
    }

    @Override
    public int compareTo(final LocationFeature o) {
        return Integer.compare(this.id, o.id);
    }
}
