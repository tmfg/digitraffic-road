package fi.livi.digitraffic.tie.model.trafficmessage.location;

import java.time.Instant;

import fi.livi.digitraffic.tie.model.ReadOnlyCreatedAndModifiedFields;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
@Schema(description = "Location Version Object")
public class LocationVersion extends ReadOnlyCreatedAndModifiedFields {
    @Schema(description = "Location version string")
    @Id
    private String version;

    public LocationVersion() {}

    public LocationVersion(final String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    @Schema(description = "Version last updated date time")
    public Instant getUpdated() {
        return getModified();
    }
}
