package fi.livi.digitraffic.tie.dto.trafficmessage.old.region;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v1.RootFeatureCollectionDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJSON Feature Collection of Region Geometries", name = "RegionGeometryFeatureCollectionV1")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "dataLastCheckedTime", "features" })
public class RegionGeometryFeatureCollection extends RootFeatureCollectionDto<RegionGeometryFeature> implements Serializable {

    public RegionGeometryFeatureCollection(final ZonedDateTime lastUpdated, final ZonedDateTime dataLastCheckedTime,
                                           final List<RegionGeometryFeature> geometries) {
        super(lastUpdated, dataLastCheckedTime, geometries);
    }
}