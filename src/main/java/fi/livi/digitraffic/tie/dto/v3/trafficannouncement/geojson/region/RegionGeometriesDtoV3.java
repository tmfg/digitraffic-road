package fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.region;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v1.RootMetadataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "RegionGeometriesV3", description = "Area geometries versions")
@JsonPropertyOrder({ "dataUpdatedTime", "dataLastCheckedTime", "areaGeometries" })
public class RegionGeometriesDtoV3 extends RootMetadataObjectDto implements Serializable {

    @ApiModelProperty(value = "Area geometries", required = true)
    public final List<RegionGeometryDtoV3> geometries;

    public RegionGeometriesDtoV3(final List<RegionGeometryDtoV3> geometries, final ZonedDateTime lastUpdated, final ZonedDateTime dataLastCheckedTime) {
        super(lastUpdated, dataLastCheckedTime);
        this.geometries = geometries;
    }
}