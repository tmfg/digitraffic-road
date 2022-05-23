package fi.livi.digitraffic.tie.dto.trafficmessage.v1.region;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.trafficmessage.v1.AreaType;
import fi.livi.digitraffic.tie.metadata.geojson.Properties;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Region geometry properties", name = "RegionGeometryProperties_V1")
@JsonPropertyOrder({ "locationCode", "name", "type", "effectiveDate" })
public class RegionGeometryProperties extends Properties {

    @Schema(description = "The name of the region", required = true)
    public String name;
    @Schema(description = "The Alert-C code of the region", required = true)
    public Integer locationCode;
    @Schema(description = "The type of the region", required = true)
    public AreaType type;
    @Schema(description = "The moment, when the data comes into effect", required = true)
    public Instant effectiveDate;

    public RegionGeometryProperties(final String name, final Integer locationCode, final AreaType type, final Instant effectiveDate) {
        this.name = name;
        this.locationCode = locationCode;
        this.type = type;
        this.effectiveDate = effectiveDate;
    }
}
