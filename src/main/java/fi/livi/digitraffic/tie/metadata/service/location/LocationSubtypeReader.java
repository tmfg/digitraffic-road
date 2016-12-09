package fi.livi.digitraffic.tie.metadata.service.location;

import java.util.Arrays;

import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.metadata.model.location.LocationSubtype;

@Component
public class LocationSubtypeReader extends AbstractReader<LocationSubtype> {
    @Override protected LocationSubtype convert(final String[] components) {
        final LocationSubtype newType = new LocationSubtype();

        try {
            newType.setSubtypeCode(components[4]);
            newType.setDescriptionEn(components[3]);
            newType.setDescriptionFi(components[5]);
        } catch(final Exception e) {
            log.info("Exception when reading line " + Arrays.toString(components), e);

            return null;
        }

        if(!newType.validate()) {
            log.error("Could not validate new LocationSubType:" + Arrays.toString(components));

            return null;
        }

        return newType;
    }
}