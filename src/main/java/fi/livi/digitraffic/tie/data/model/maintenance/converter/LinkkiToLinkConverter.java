package fi.livi.digitraffic.tie.data.model.maintenance.converter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.data.model.maintenance.Link;
import fi.livi.digitraffic.tie.harja.entities.LinkkisijaintiSchema;

@ConditionalOnWebApplication
@Component
public class LinkkiToLinkConverter extends AutoRegisteredConverter<LinkkisijaintiSchema, Link> {

    @Override
    public Link convert(final LinkkisijaintiSchema src) {
        return new Link(src.getId(), src.getMarvo());
    }
}
