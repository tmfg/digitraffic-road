package fi.livi.digitraffic.tie.metadata.geojson.forecastsection;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.MultiLineString;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJSON Feature Object", name = "ForecastSectionFeature_OldV2")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class ForecastSectionV2Feature {

    @Schema(description = "\"Feature\": GeoJSON Feature Object", required = true, allowableValues = "Feature")
    @JsonPropertyOrder(value = "1")
    private final String type = "Feature";

    // TODO: Remove this from next version as it is just db id and real id is in properties
    @Schema(description = "Forecast section id", required = true)
    @JsonPropertyOrder(value = "2")
    private long id;

    @Schema(description = "GeoJSON MultiLineString Geometry Object. Points represent the road.", required = true)
    @JsonPropertyOrder(value = "3")
    private MultiLineString geometry;

    @Schema(description = "Forecast section properties", required = true)
    @JsonPropertyOrder(value = "4")
    private ForecastSectionV2Properties properties = new ForecastSectionV2Properties();

    public ForecastSectionV2Feature(long id, final MultiLineString geometry, final ForecastSectionV2Properties properties) {
        this.id = id;
        this.geometry = geometry;
        this.properties = properties;
    }

    public String getType() {
        return type;
    }

    public MultiLineString getGeometry() {
        return geometry;
    }

    public void setGeometry(final MultiLineString geometry) {
        this.geometry = geometry;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public ForecastSectionV2Properties getProperties() {
        return properties;
    }

    public void setProperties(final ForecastSectionV2Properties properties) {
        this.properties = properties;
    }

}
