package fi.livi.digitraffic.tie.dto.tms.v1;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.RoadStationPropertiesSimpleV1;
import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * GeoJSON CameraPresetFeature Object
 */
@Schema(description = "Tms GeoJSON Feature object base")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class TmsStationFeatureBaseV1<TmsStationPropertiesType extends RoadStationPropertiesSimpleV1<Long>> extends Feature<Point, TmsStationPropertiesType> {

    @Schema(description = "Id of the road station", required = true)
    public final Long id;

    public TmsStationFeatureBaseV1(final Point geometry, final TmsStationPropertiesType properties) {
        super(geometry, properties);
        this.id = properties.id;
    }

    @Schema(description = "GeoJSON Point Geometry Object. Point where station is located", required = true, allowableValues = "Point")
    @Override
    public Point getGeometry() {
        return super.getGeometry();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final TmsStationFeatureBaseV1<?> that = (TmsStationFeatureBaseV1<?>) o;

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
