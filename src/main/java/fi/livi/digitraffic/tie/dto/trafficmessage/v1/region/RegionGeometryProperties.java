package fi.livi.digitraffic.tie.dto.trafficmessage.v1.region;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.trafficmessage.v1.AreaType;
import fi.livi.digitraffic.tie.metadata.geojson.Properties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Region geometry properties", value = "RegionGeometryProperties_V1", parent = Properties.class)
@JsonPropertyOrder({ "locationCode", "name", "type", "effectiveDate" })
public class RegionGeometryProperties extends Properties {

    @ApiModelProperty(value = "The name of the region", required = true)
    public String name;
    @ApiModelProperty(value = "The Alert-C code of the region", required = true)
    public Integer locationCode;
    @ApiModelProperty(value = "The type of the region", required = true)
    public AreaType type;
    @ApiModelProperty(value = "The moment, when the data comes into effect", required = true)
    public Instant effectiveDate;

    public RegionGeometryProperties(final String name, final Integer locationCode, final AreaType type, final Instant effectiveDate) {
        this.name = name;
        this.locationCode = locationCode;
        this.type = type;
        this.effectiveDate = effectiveDate;
    }
}
