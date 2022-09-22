package fi.livi.digitraffic.tie.dto.geojson.v1;

import java.time.Instant;

import fi.livi.digitraffic.tie.dto.LastModifiedSupport;
import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;

public class FeatureV1<G extends Geometry<?>, P extends PropertiesV1> extends Feature<G, P> implements LastModifiedSupport {

    public FeatureV1(final G geometry, final P properties) {
        super(geometry, properties);
    }

    @Override
    public Instant getLastModified() {
        return getProperties().getLastModified();
    }
}
