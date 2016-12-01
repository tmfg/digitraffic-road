package fi.livi.digitraffic.tie.metadata.service.location;

import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.metadata.model.location.LocationType;

@Component
public class LocationTypeReader extends AbstractReader<LocationType> {
    @Override
    protected LocationType convert(final String[] components) {
        final LocationType newType = new LocationType();

        newType.setTypeCode(components[3]);
        newType.setDescriptionEn(components[2]);
        newType.setDescriptionFi(components[4]);

        if(!newType.validate()) {
            log.error("Could not validate new LocationType:" + components);

            return null;
        }

        return newType;
    }
}
