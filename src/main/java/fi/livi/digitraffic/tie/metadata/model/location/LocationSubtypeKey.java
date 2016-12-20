package fi.livi.digitraffic.tie.metadata.model.location;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class LocationSubtypeKey implements Serializable {
    private String subtypeCode;

    private String version;

    public LocationSubtypeKey() {}

    public LocationSubtypeKey(final String version, final String subtypeCode) {
        this.version = version;
        this.subtypeCode = subtypeCode;
    }

    public String getSubtypeCode() {
        return subtypeCode;
    }

    public void setSubtypeCode(String subtypeCode) {
        this.subtypeCode = subtypeCode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean validate() {
        return isNotEmpty(subtypeCode) && isNotEmpty(version);
    }
}
