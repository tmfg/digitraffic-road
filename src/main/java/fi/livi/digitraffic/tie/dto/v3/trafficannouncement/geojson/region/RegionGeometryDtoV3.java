package fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.region;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.AreaType;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "RegionGeometryV3", description = "Region area data")
@JsonPropertyOrder({ "dataUpdatedTime", "dataLastCheckedTime", "areaGeometries" })
public class RegionGeometryDtoV3 {

    @ApiModelProperty(value = "The name of the region", required = true)
    public String name;
    @ApiModelProperty(value = "The Alert-C code of the region", required = true)
    public Integer locationCode;
    @ApiModelProperty(value = "The type of the region", required = true)
    public AreaType type;
    @ApiModelProperty(value = "The moment, when the data comes into effect", required = true)
    public Instant effectiveDate;
    @ApiModelProperty(value = "The geometry of the region in GeoJSON Geometry format.", required = true)
    public Geometry<?> geometry;

    public RegionGeometryDtoV3(final String name, final Integer locationCode, final AreaType type, final Instant effectiveDate, final Geometry<?> geometry) {

        this.name = name;
        this.locationCode = locationCode;
        this.type = type;
        this.effectiveDate = effectiveDate;
        this.geometry = geometry;
    }
}
