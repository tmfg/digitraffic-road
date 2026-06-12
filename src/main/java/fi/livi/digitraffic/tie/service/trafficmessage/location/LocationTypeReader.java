package fi.livi.digitraffic.tie.service.trafficmessage.location;

import fi.livi.digitraffic.common.util.StringUtil;
import fi.livi.digitraffic.tie.model.trafficmessage.location.LocationType;
import fi.livi.digitraffic.tie.model.trafficmessage.location.LocationTypeKey;

public class LocationTypeReader extends AbstractReader<LocationType> {
    protected LocationTypeReader(final String version) {
        super(version);
    }

    @Override
    protected LocationType convert(final String[] components, final String filename) {
        final LocationType newType = new LocationType();

        newType.setId(new LocationTypeKey(version, components[3]));
        newType.setDescriptionEn(components[2]);
        newType.setDescriptionFi(components[4]);

        if (!newType.validate()) {
            throw new IllegalArgumentException(
                    StringUtil.format("Could not validate LocationType typeCode={}", components[3]));
        }

        return newType;
    }
}
