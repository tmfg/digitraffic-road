package fi.livi.digitraffic.tie.service.trafficmessage.location;

import java.util.EnumMap;
import java.util.Map;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.DataType;

public class MetadataVersions {
    private final Map<DataType, MetadataVersion> versionMap = new EnumMap<>(DataType.class);

    public void addVersion(final DataType type, final String filename, final String version) {
        versionMap.put(type, new MetadataVersion(filename, version));
    }

    public MetadataVersion getLocationsVersion() {
        return versionMap.get(DataType.LOCATIONS_METADATA);
    }

    public MetadataVersion getLocationTypeVersion() {
        return versionMap.get(DataType.LOCATION_TYPES_METADATA);
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
            return ToStringHelper.toStringFull(this);
        }
    }
}
