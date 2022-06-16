package fi.livi.digitraffic.tie.dto.trafficmessage.v1.region;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Region area GeoJSON Feature object", name = "RegionGeometryFeatureV1")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class RegionGeometryFeature extends Feature<Geometry<?>, RegionGeometryProperties> {

    public RegionGeometryFeature(final Geometry<?> geometry, final RegionGeometryProperties properties) {
        super(geometry, properties);
    }
}
