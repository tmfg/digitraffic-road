package fi.livi.digitraffic.tie.metadata.geojson;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJson Coordinate Reference System Object")
@JsonTypeInfo(property = "type",  use = JsonTypeInfo.Id.NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "type", "properties" })
public class Crs implements Serializable {

    @ApiModelProperty(value = "CRS type (always \"name\")", required = true, example = "name", position = 1)
    private CrsType type = CrsType.name;

    @ApiModelProperty(value = "CRS properties", required = true, position = 2)
    private CrsProperties properties = new CrsProperties();

    public CrsType getType() {
        return type;
    }

    public void setType(final CrsType type) {
        this.type = type;
    }

    public CrsProperties getProperties() {
        return properties;
    }

    public void setProperties(final CrsProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Crs)) {
            return false;
        }
        final Crs crs = (Crs) o;
        if (properties != null ? !properties.equals(crs.properties) : crs.properties != null) {
            return false;
        }
        return !(type != null ? !type.equals(crs.type) : crs.type != null);
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Crs{" + "type='" + type + '\'' + ", properties=" + properties + '}';
    }
}
