package fi.livi.digitraffic.tie.dto.weathercam.v1;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * GeoJSON CameraPresetFeature Object
 */
@Schema(description = "GeoJSON Feature Object.", name = "CameraStationFeature")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class WeathercamFeatureV1 extends Feature<Point, WeathercamPropertiesV1> {

    /** Camera id ie. C01234 */
    @Schema(description = "Station id", required = true)
    private String id;

    public WeathercamFeatureV1(final Point geometry, final WeathercamPropertiesV1 properties) {
        super(geometry, properties);
        this.id = properties.getId();
    }

    @Schema(description = "GeoJSON Point Geometry Object. Point where station is located", required = true, allowableValues = "Point")
    @Override
    public Point getGeometry() {
        return super.getGeometry();
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final WeathercamFeatureV1 that = (WeathercamFeatureV1) o;

        return new EqualsBuilder()
            .append(getType(), that.getType())
            .append(id, that.id)
            .append(getGeometry(), that.getGeometry())
            .append(getProperties(), that.getProperties())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getType())
            .append(id)
            .append(getGeometry())
            .append(getProperties())
            .toHashCode();
    }
}
