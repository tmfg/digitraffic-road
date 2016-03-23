package fi.livi.digitraffic.tie.geojson;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * GeoJSON Feature Object
 */
@ApiModel(description = "GeoJSON Feature Object.")
@JsonTypeInfo(property = "type",  use = Id.NAME)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Feature /*extends GeoJsonObject*/ {

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @ApiModelProperty(value = "Unique identifier for lam station.", required = true, position = 1)
    private String id;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @ApiModelProperty(value = "GeoJSON Point Geometry Object. Point where station is located.", required = true, position = 1)
    private Point geometry;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @ApiModelProperty(value = "Lam station properties.", required = true, position = 3)
    private Properties properties = new Properties();

    public Point getGeometry() {
        return geometry;
    }

    public void setGeometry(Point geometry) {
        this.geometry = geometry;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

//    public <T> T accept(GeoJsonObjectVisitor<T> geoJsonObjectVisitor) {
//        return geoJsonObjectVisitor.visit(this);
//    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

//    @Override public boolean equals(Object o) {
//        if (this == o)
//            return true;
//        if (o == null || getClass() != o.getClass())
//            return false;
//        if (!super.equals(o))
//            return false;
//        Feature feature = (Feature)o;
//        if (properties != null ? !properties.equals(feature.properties) : feature.properties != null)
//            return false;
//        if (geometry != null ? !geometry.equals(feature.geometry) : feature.geometry != null)
//            return false;
//        return !(id != null ? !id.equals(feature.id) : feature.id != null);
//    }
//
//    @Override public int hashCode() {
//        int result = super.hashCode();
//        result = 31 * result + (properties != null ? properties.hashCode() : 0);
//        result = 31 * result + (geometry != null ? geometry.hashCode() : 0);
//        result = 31 * result + (id != null ? id.hashCode() : 0);
//        return result;
//    }
//
//    @Override
//    public String toString() {
//        return "Feature{properties=" + properties + ", geometry=" + geometry + ", id='" + id + "'}";
//    }
}
