package fi.livi.digitraffic.tie.data.model.json.maintenance.converter;

import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.data.model.json.maintenance.Link;
import fi.livi.digitraffic.tie.harja.entities.LinkkisijaintiSchema;

@Component
public class LinkkiToLinkConverter extends AutoRegisteredConverter<LinkkisijaintiSchema, Link> {

    @Override
    public Link convert(final LinkkisijaintiSchema src) {
        return new Link(src.getId(), src.getMarvo());
    }
}
