package fi.livi.digitraffic.tie.geojson;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import fi.livi.digitraffic.tie.geojson.jackson.CrsType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJson Coordinate Reference System Object")
@JsonTypeInfo(property = "type",  use = JsonTypeInfo.Id.NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Crs implements Serializable {

    @ApiModel(description = "GeoJson Named CRS properties")
    @JsonTypeInfo(property = "type",  use = JsonTypeInfo.Id.NONE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public class CrsProperties implements Serializable {

        @ApiModelProperty(value = "Named CRS name", required = true)
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
    @ApiModelProperty(value = "CRS type (always CrsType.name}", required = true)
    private CrsType type = CrsType.name;

    @ApiModelProperty(value = "CRS properties. Has only name", required = true)
    private CrsProperties properties = new CrsProperties();

    public CrsType getType() {
        return type;
    }

    public void setType(CrsType type) {
        this.type = type;
    }

    public CrsProperties getProperties() {
        return properties;
    }

    public void setProperties(CrsProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Crs)) {
            return false;
        }
        Crs crs = (Crs) o;
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
