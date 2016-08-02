package fi.livi.digitraffic.tie.metadata.geojson;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJson Named CRS properties")
@JsonTypeInfo(property = "type", use = JsonTypeInfo.Id.NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrsProperties implements Serializable {

    @ApiModelProperty(value = "Named CRS name", required = true)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CrsProperties)) {
            return false;
        }
        final CrsProperties crsProperties = (CrsProperties) o;
        return new EqualsBuilder().append(name, crsProperties.getName()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(name).hashCode();
    }
}
