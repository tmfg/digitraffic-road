package fi.livi.digitraffic.tie.metadata.model.location;

import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Entity
@ApiModel(description = "Location Version Object")
public class LocationVersion {
    @ApiModelProperty(value = "Location version string")
    @Id
    private String version;

    @ApiModelProperty(value = "Version last updated " + ToStringHelper.ISO_8601_OFFSET_TIMESTAMP_EXAMPLE, required = true)
    private ZonedDateTime updated;

    public LocationVersion() {}

    public LocationVersion(final String version) {
        this.version = version;
        this.updated = ZonedDateTime.now();
    }

    public ZonedDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(final ZonedDateTime updated) {
        this.updated = updated;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }
}
