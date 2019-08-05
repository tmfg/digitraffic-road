package fi.livi.digitraffic.tie.metadata.geojson;

import io.swagger.annotations.ApiModelProperty;

public interface Feature<T extends Geometry> {

    @ApiModelProperty(value = "GeoJSON Object type", example = "Feature")
    String getType();

    T getGeometry();

    void setGeometry(T geometry);
}
