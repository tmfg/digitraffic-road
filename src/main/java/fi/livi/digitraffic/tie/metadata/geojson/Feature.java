package fi.livi.digitraffic.tie.metadata.geojson;

import io.swagger.v3.oas.annotations.media.Schema;

public abstract class Feature<G extends Geometry<?>, P extends Properties>  extends GeoJsonObject {

    @Schema(description = "\"Feature\": GeoJSON Feature Object", required = true, allowableValues = "Feature")
    private final String type = "Feature";

    @Schema(description = "GeoJSON Geometry Object",
                      required = true)
    private G geometry;

    @Schema(description = "GeoJSON Properties Object",
                      required = true)
    private P properties;

    public Feature() {
    }

    public Feature(final P properties) {
        this.properties = properties;
    }

    public Feature(final G geometry, final P properties) {
        this.geometry = geometry;
        this.properties = properties;
    }

    @Override
    public String getType() {
        return type;
    }

    public G getGeometry() {
        return geometry;
    }

    public void setGeometry(final G geometry) {
        this.geometry = geometry;
    }

    public P getProperties() {
        return properties;
    }

    public void setProperties(final P properties) {
        this.properties = properties;
    }
}