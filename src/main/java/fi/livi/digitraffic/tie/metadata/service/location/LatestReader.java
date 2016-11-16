package fi.livi.digitraffic.tie.metadata.service.location;

import org.apache.commons.codec.binary.StringUtils;

import fi.livi.digitraffic.tie.metadata.model.MetadataType;

public class LatestReader extends AbstractReader<Void> {
    private final MetadataVersions latestMetadataVersions = new MetadataVersions();

    @Override
    protected Void convert(final String[] line) {
        final MetadataType type = getMetadataType(line[0]);

        latestMetadataVersions.addVersion(type, line[1], line[2]);

        return null;
    }

    private MetadataType getMetadataType(final String type) {
        if(StringUtils.equals(type, "csv")) {
            return MetadataType.LOCATIONS;
        } else if(StringUtils.equals(type, "dat")) {
            return MetadataType.LOCATION_TYPES;
        }

        throw new IllegalArgumentException("Unknown metadatatype " + type);
    }

    public MetadataVersions getLatestMetadataVersions()  {
        return latestMetadataVersions;
    }
}
