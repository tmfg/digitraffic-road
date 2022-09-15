package fi.livi.digitraffic.tie.dto.weather.v1;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.FeatureV1;
import fi.livi.digitraffic.tie.dto.geojson.v1.RoadStationPropertiesSimpleV1;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * GeoJSON weather station object
 */
@Schema(description = "Weather station GeoJSON feature object base")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class WeatherStationFeatureBaseV1<WeatherStationPropertiesType extends RoadStationPropertiesSimpleV1<Long>> extends FeatureV1<Point, WeatherStationPropertiesType> {

    @Schema(description = "Id of the road station", required = true)
    public final Long id;

    public WeatherStationFeatureBaseV1(final Point geometry, final WeatherStationPropertiesType properties) {
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

        final WeatherStationFeatureBaseV1<?> that = (WeatherStationFeatureBaseV1<?>) o;

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
