package fi.livi.digitraffic.tie.data.model.maintenance;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * GeoJSON ObservationFeature Object
 */
@ApiModel(description = "GeoJSON Feature Object", value = "ObservationFeature")
@JsonPropertyOrder({ "type", "geometry", "properties" })
public class ObservationFeature implements Feature, Serializable {

    @ApiModelProperty(value = "\"Feature\": GeoJSON Feature Object", required = true, position = 1)
    @JsonPropertyOrder(value = "1")
    private final String type = "Feature";

    @ApiModelProperty(value = "GeoJSON Point Geometry Object. Point where the machine was located", required = true, position = 3)
    @JsonPropertyOrder(value = "3")
    private Point geometry;

    @ApiModelProperty(value = "TMS station properties", required = true, position = 4)
    @JsonPropertyOrder(value = "4")
    private ObservationProperties properties;

    @JsonCreator
    public ObservationFeature(final Point geometry, final ObservationProperties properties) {
        this.geometry = geometry;
        if (properties == null) {
            throw new IllegalArgumentException("ObservationProperties cannot be null");
        }
        this.properties = properties;
    }

    public String getType() {
        return type;
    }

    public Point getGeometry() {
        return geometry;
    }

    public void setGeometry(final Point geometry) {
        this.geometry = geometry;
    }

    public ObservationProperties getProperties() {
        return properties;
    }

    public void setProperties(final ObservationProperties properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ObservationFeature that = (ObservationFeature) o;

        return new EqualsBuilder()
            .append(type, that.type)
            .append(geometry, that.geometry)
            .append(properties, that.properties)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(type)
            .append(geometry)
            .append(properties)
            .toHashCode();
    }
}
