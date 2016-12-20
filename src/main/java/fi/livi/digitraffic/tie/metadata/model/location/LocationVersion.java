package fi.livi.digitraffic.tie.metadata.model.location;

import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class LocationVersion {
    @Id
    private String version;

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
