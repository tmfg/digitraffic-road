package fi.livi.digitraffic.tie.dto.weathercam.v1;

import java.time.Instant;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.FeatureCollectionDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJSON FeatureCollection of camera stations with camera presets", name = "WeathercamFeatureCollectionV1")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "dataLastCheckedTime", "features" })
public class WeathercamFeatureCollectionV1 extends FeatureCollectionDto<WeathercamFeatureV1> {

    public WeathercamFeatureCollectionV1(final Instant updatedTime, final Instant checkedTime,
                                         final List<WeathercamFeatureV1> features) {
        super(updatedTime, checkedTime, features);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final WeathercamFeatureCollectionV1 that = (WeathercamFeatureCollectionV1) o;

        return new EqualsBuilder()
                .append(getType(), that.getType())
                .append(getFeatures(), that.getFeatures())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getType())
                .append(getFeatures())
                .toHashCode();
    }
}
