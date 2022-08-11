package fi.livi.digitraffic.tie.dto.tms.v1;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Detailed Tms Station Feature Object
 */
@Schema(description = " Tms station GeoJSON feature object with detailed information")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class TmsStationFeatureDetailedV1 extends TmsStationFeatureBaseV1<TmsStationPropertiesDetailedV1> {

    public TmsStationFeatureDetailedV1(final Point geometry, final TmsStationPropertiesDetailedV1 properties) {
        super(geometry, properties);
    }
}