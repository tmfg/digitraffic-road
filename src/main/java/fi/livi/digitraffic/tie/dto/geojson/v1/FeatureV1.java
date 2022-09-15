package fi.livi.digitraffic.tie.dto.geojson.v1;

import java.time.Instant;

import fi.livi.digitraffic.tie.dto.LastModifiedSupport;
import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.metadata.geojson.PropertiesWithId;

public class FeatureV1<G extends Geometry<?>, P extends PropertiesWithId<?>> extends Feature<G, P> implements LastModifiedSupport {

    private final Instant lastModified;
    public FeatureV1(final G geometry, final P properties) {
        super(geometry, properties);
        lastModified = properties.getLastModified();
    }

    @Override
    public Instant getLastModified() {
        return lastModified;
    }
}
