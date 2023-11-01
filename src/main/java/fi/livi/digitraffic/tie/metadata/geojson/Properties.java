package fi.livi.digitraffic.tie.metadata.geojson;

import java.io.Serializable;

import fi.livi.digitraffic.tie.dto.JsonAdditionalProperties;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.region.RegionGeometryProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJSON Properties object", subTypes = { RegionGeometryProperties.class })
public abstract class Properties extends JsonAdditionalProperties implements Serializable {

}