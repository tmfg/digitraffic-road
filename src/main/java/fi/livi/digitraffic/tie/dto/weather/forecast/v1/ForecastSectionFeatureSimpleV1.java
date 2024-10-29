package fi.livi.digitraffic.tie.dto.weather.forecast.v1;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.FeatureV1;
import fi.livi.digitraffic.tie.metadata.geojson.LineString;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * GeoJSON ForecastSectionFeature Object
 */
@Schema(description = "GeoJSON feature object of simple forecast section")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class ForecastSectionFeatureSimpleV1 extends FeatureV1<LineString, ForecastSectionPropertiesSimpleV1> {

    @Schema(description = "Forecast section identifier 15 characters ie. 00004_112_000_0, see properties id description.", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonPropertyOrder(value = "2")
    public final String id;

    public ForecastSectionFeatureSimpleV1(final LineString geometry, final ForecastSectionPropertiesSimpleV1 properties) {
        super(geometry, properties);
        this.id = properties.id;
    }
}
