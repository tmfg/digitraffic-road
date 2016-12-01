package fi.livi.digitraffic.tie.metadata.service.location;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import fi.livi.digitraffic.tie.metadata.model.MetadataType;

public class MetadataVersions {
    private final Map<MetadataType, MetadataVersion> versionMap = new HashMap<>();

    public void addVersion(MetadataType type, final String filename, final String version) {
        versionMap.put(type, new MetadataVersion(filename, version));
    }

    public MetadataVersion getLocationsVersion() {
        return versionMap.get(MetadataType.LOCATIONS);
    }

    public MetadataVersion getLocationTypeVersion() {
        return versionMap.get(MetadataType.LOCATION_TYPES);
    }

    public static class MetadataVersion {
        public final String filename;
        public final String version;

        public MetadataVersion(final String filename, final String version) {
            this.filename = filename;
            this.version = version;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
        }
    }
}
