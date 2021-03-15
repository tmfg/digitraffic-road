package fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.region;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "RegionGeometryFeatureV3", description = "Region area GeoJSON Feature object")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class RegionGeometryFeature extends Feature<Geometry<?>, RegionGeometryProperties> {

    public RegionGeometryFeature(final Geometry<?> geometry, final RegionGeometryProperties properties) {
        super(geometry, properties);
    }
}
