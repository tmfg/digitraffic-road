package fi.livi.digitraffic.tie.metadata.service.location;

import fi.livi.digitraffic.tie.metadata.model.location.LocationType;
import fi.livi.digitraffic.tie.metadata.model.location.LocationTypeKey;

public class LocationTypeReader extends AbstractReader<LocationType> {
    protected LocationTypeReader(final String version) {
        super(version);
    }

    @Override
    protected LocationType convert(final String[] components) {
        final LocationType newType = new LocationType();

        newType.setId(new LocationTypeKey(version, components[3]));
        newType.setDescriptionEn(components[2]);
        newType.setDescriptionFi(components[4]);

        if(!newType.validate()) {
            log.error("Could not validate new LocationType:" + components);

            return null;
        }

        return newType;
    }
}
