package fi.livi.digitraffic.tie.dto.geojson.v1;

import java.time.Instant;

import fi.livi.digitraffic.tie.metadata.geojson.Geometry;

public class FeatureWithIdV1<G extends Geometry<?>, P extends PropertiesV1> extends FeatureV1<G, P>  {

    private final Instant lastModified;
    public FeatureWithIdV1(final G geometry, final P properties) {
        super(geometry, properties);
        lastModified = properties.getLastModified();
    }

    @Override
    public Instant getLastModified() {
        return lastModified;
    }
}
