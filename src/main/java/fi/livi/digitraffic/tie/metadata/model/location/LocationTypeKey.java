package fi.livi.digitraffic.tie.metadata.model.location;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class LocationTypeKey implements Serializable{
    private String typeCode;

    private String version;

    public LocationTypeKey() {}

    public LocationTypeKey(final String version, final String typeCode) {
        this.version = version;
        this.typeCode = typeCode;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean validate() {
        return isNotEmpty(typeCode) && isNotEmpty(version);
    }
}
