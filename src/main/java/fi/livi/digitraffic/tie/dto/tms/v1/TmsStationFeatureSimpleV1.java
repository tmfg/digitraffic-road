package fi.livi.digitraffic.tie.dto.tms.v1;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tms station GeoJSON Feature object with basic information")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class TmsStationFeatureSimpleV1 extends TmsStationFeatureBaseV1<TmsStationPropertiesSimpleV1> {

    public TmsStationFeatureSimpleV1(final Point geometry, final TmsStationPropertiesSimpleV1 properties) {
        super(geometry, properties);
    }
}
