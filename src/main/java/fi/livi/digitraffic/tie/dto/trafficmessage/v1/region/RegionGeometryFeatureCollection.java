package fi.livi.digitraffic.tie.dto.trafficmessage.v1.region;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.FeatureCollectionV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJSON Feature Collection of Region Geometries", name = "RegionGeometryFeatureCollectionV1")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "dataLastCheckedTime", "features" })
public class RegionGeometryFeatureCollection extends FeatureCollectionV1<RegionGeometryFeature> implements Serializable {

    public RegionGeometryFeatureCollection(final Instant lastUpdated,
                                           final List<RegionGeometryFeature> geometries) {
        super(lastUpdated, geometries);
    }
}