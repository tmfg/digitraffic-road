package fi.livi.digitraffic.tie.service.trafficmessage.location;

import fi.livi.digitraffic.common.util.StringUtil;
import fi.livi.digitraffic.tie.model.trafficmessage.location.LocationSubtype;
import fi.livi.digitraffic.tie.model.trafficmessage.location.LocationSubtypeKey;

public class LocationSubtypeReader extends AbstractReader<LocationSubtype> {
    protected LocationSubtypeReader(final String version) {
        super(version);
    }

    @Override
    protected LocationSubtype convert(final String[] components, final String filename) {
        final LocationSubtype newType = new LocationSubtype();

        try {
            newType.setId(new LocationSubtypeKey(version, components[4]));
            newType.setDescriptionEn(components[3]);
            newType.setDescriptionFi(components[5]);
        } catch (final Exception e) {
            // Some rows are structurally incomplete and expected to be skipped silently
            log.info("method=convert Skipping incomplete LocationSubtype row file={} cause={}", filename, e.getMessage());
            return null;
        }

        if (!newType.validate()) {
            throw new IllegalArgumentException(
                    StringUtil.format("Could not validate LocationSubtype subtypeCode={}", components[4]));
        }

        return newType;
    }
}
