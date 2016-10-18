package fi.livi.digitraffic.tie.metadata.service.location;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.metadata.model.location.LocationSubtype;

@Component
public class LocationSubtypeReader extends AbstractReader<LocationSubtype> {
    @Override protected LocationSubtype convert(final String line) {
        final String components[] = StringUtils.splitPreserveAllTokens(line, DELIMETER);

        final LocationSubtype newType = new LocationSubtype();

        try {
            newType.setSubtypeCode(components[4]);
            newType.setDescriptionEn(components[3]);
            newType.setDescriptionFi(components[5]);
        } catch(final Exception e) {
            log.info("exception when reading line " + line, e);

            return null;
        }

        return newType;
    }
}
