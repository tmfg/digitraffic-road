package fi.livi.digitraffic.tie.model.v1.location;

import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Schema(description = "Location Version Object")
public class LocationVersion {
    @Schema(description = "Location version string")
    @Id
    private String version;

    @Schema(description = "Version last updated date time", required = true)
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
