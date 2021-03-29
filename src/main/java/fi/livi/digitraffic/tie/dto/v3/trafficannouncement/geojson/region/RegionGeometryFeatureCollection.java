package fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.region;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v1.RootFeatureCollectionDto;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "GeoJSON Feature Collection of Region Geometries", value = "RegionGeometryFeatureCollectionV3")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "dataLastCheckedTime", "features" })
public class RegionGeometryFeatureCollection extends RootFeatureCollectionDto<RegionGeometryFeature> implements Serializable {

    public RegionGeometryFeatureCollection(final ZonedDateTime lastUpdated, final ZonedDateTime dataLastCheckedTime,
                                           final List<RegionGeometryFeature> geometries) {
        super(lastUpdated, dataLastCheckedTime, geometries);
    }
}