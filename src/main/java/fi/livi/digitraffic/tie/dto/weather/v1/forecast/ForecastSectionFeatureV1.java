package fi.livi.digitraffic.tie.dto.weather.v1.forecast;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.FeatureV1;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJSON feature object of forecast section")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class ForecastSectionFeatureV1 extends FeatureV1<Geometry<?>, ForecastSectionPropertiesV1> {

    @Schema(description = ForecastSectionPropertiesV1.ID_DESC)
    public final String id;

    public ForecastSectionFeatureV1(final Geometry<?> geometry, final ForecastSectionPropertiesV1 properties) {
        super(geometry, properties);
        this.id = properties.id;
    }
}
