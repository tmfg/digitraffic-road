package fi.livi.digitraffic.tie.dto.geojson.v1;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import io.swagger.v3.oas.annotations.media.Schema;

public class FeatureWithIdV1<G extends Geometry<?>, P extends PropertiesWithIdV1<ID_TYPE>, ID_TYPE> extends FeatureV1<G, P>  {

    @Schema(description = "Id of the feature", requiredMode = Schema.RequiredMode.REQUIRED)
    public ID_TYPE getId() {
        return getProperties().id;
    }

    public FeatureWithIdV1(final G geometry, final P properties) {
        super(geometry, properties);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final FeatureWithIdV1<?,?,?> that = (FeatureWithIdV1<?,?,?>) o;

        return new EqualsBuilder()
            .append(getType(), that.getType())
            .append(getId(), that.getId())
            .append(getGeometry(), that.getGeometry())
            .append(getProperties(), that.getProperties())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getType())
            .append(getId())
            .append(getGeometry())
            .append(getProperties())
            .toHashCode();
    }
}
