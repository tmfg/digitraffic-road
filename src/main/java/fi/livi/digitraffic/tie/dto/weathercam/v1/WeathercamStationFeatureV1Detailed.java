package fi.livi.digitraffic.tie.dto.weathercam.v1;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * GeoJSON CameraPresetFeature Object
 */
@Schema(description = " Weathercam station GeoJSON feature object with detailed information")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class WeathercamStationFeatureV1Detailed extends WeathercamStationFeatureBaseV1<WeathercamStationPropertiesDetailedV1> {

    public WeathercamStationFeatureV1Detailed(final Point geometry, final WeathercamStationPropertiesDetailedV1 properties) {
        super(geometry, properties);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final WeathercamStationFeatureV1Detailed that = (WeathercamStationFeatureV1Detailed) o;

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
