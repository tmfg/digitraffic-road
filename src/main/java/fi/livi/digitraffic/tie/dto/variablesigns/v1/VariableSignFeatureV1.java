package fi.livi.digitraffic.tie.dto.variablesigns.v1;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.FeatureV1;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJSON Feature Object of variable sign")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class VariableSignFeatureV1 extends FeatureV1<Point, VariableSignPropertiesV1> {

    public VariableSignFeatureV1(final Point geometry, final VariableSignPropertiesV1 properties) {
        super(geometry,properties);
    }
}
