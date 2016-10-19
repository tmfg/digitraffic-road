package fi.livi.digitraffic.tie.metadata.service.location;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.metadata.model.location.LocationType;

@Component
public class LocationTypeReader extends AbstractReader<LocationType> {
    @Override
    protected LocationType convert(final String line) {
        final String components[] = StringUtils.splitPreserveAllTokens(line, DELIMETER);

        final LocationType newType = new LocationType();

        newType.setTypeCode(components[3]);
        newType.setDescriptionEn(components[2]);
        newType.setDescriptionFi(components[4]);

        if(!newType.validate()) {
            log.error("Could not validate new LocationType:" + line);

            return null;
        }

        return newType;
    }
}
