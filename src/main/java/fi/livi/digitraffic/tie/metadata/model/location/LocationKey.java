package fi.livi.digitraffic.tie.metadata.model.location;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class LocationKey implements Serializable {
    private Integer locationCode;

    private String version;

    public LocationKey() {}

    public LocationKey(final String version, final Integer locationCode) {
        this.version = version;
        this.locationCode = locationCode;
    }

    public Integer getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(Integer locationCode) {
        this.locationCode = locationCode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
