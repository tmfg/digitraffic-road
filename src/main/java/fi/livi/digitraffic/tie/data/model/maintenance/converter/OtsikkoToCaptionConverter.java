package fi.livi.digitraffic.tie.data.model.maintenance.converter;

import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.data.model.maintenance.Caption;
import fi.livi.digitraffic.tie.data.model.maintenance.Sender;
import fi.livi.digitraffic.tie.harja.entities.OtsikkoSchema;
import fi.livi.digitraffic.tie.helper.DateHelper;

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
