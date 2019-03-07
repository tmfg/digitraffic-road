package fi.livi.digitraffic.tie.data.model.maintenance.json.converter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.data.model.maintenance.json.Caption;
import fi.livi.digitraffic.tie.data.model.maintenance.json.Sender;
import fi.livi.digitraffic.tie.harja.entities.OtsikkoSchema;
import fi.livi.digitraffic.tie.helper.DateHelper;

@ConditionalOnWebApplication
@Component
public class OtsikkoToCaptionConverter extends AutoRegisteredConverter<OtsikkoSchema, Caption> {

    @Override
    public Caption convert(final OtsikkoSchema src) {
        return new Caption(
            convert(src.getLahettaja(), Sender.class),
            src.getViestintunniste() != null ? src.getViestintunniste().getId() : null,
            DateHelper.toZonedDateTime(src.getLahetysaika()));
    }
}
