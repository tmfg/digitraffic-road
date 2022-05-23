package fi.livi.digitraffic.tie.metadata.geojson.forecastsection;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.LineString;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * GeoJSON ForecastSectionFeature Object
 */
@Schema(description = "GeoJSON Feature Object", name = "ForecastSectionFeatureV1")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class ForecastSectionFeature {

    @Schema(description = "\"Feature\": GeoJSON Feature Object", required = true)
    @JsonPropertyOrder(value = "1")
    private final String type = "Feature";

    // TODO: Remove this from next version as it is just db id and real id is in properties
    @Schema(description = "Forecast section id", required = true)
    @JsonPropertyOrder(value = "2")
    private long id;

    @Schema(description = "GeoJSON LineString Geometry Object. Points represent the road.", required = true)
    @JsonPropertyOrder(value = "3")
    private LineString geometry;

    @Schema(description = "Forecast section properties", required = true)
    @JsonPropertyOrder(value = "4")
    private ForecastSectionProperties properties = new ForecastSectionProperties();

    public ForecastSectionFeature(long id, LineString geometry, ForecastSectionProperties properties) {
        this.id = id;
        this.geometry = geometry;
        this.properties = properties;
    }

    public String getType() {
        return type;
    }

    public LineString getGeometry() {
        return geometry;
    }

    public void setGeometry(final LineString geometry) {
        this.geometry = geometry;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public ForecastSectionProperties getProperties() {
        return properties;
    }

    public void setProperties(final ForecastSectionProperties properties) {
        this.properties = properties;
    }

}
