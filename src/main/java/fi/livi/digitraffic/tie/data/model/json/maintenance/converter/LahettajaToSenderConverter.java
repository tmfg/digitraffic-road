package fi.livi.digitraffic.tie.data.model.json.maintenance.converter;

import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.data.model.json.maintenance.Organisation;
import fi.livi.digitraffic.tie.data.model.json.maintenance.Sender;
import fi.livi.digitraffic.tie.harja.entities.Lahettaja;

@Component
public class LahettajaToSenderConverter extends AutoRegisteredConverter<Lahettaja, Sender> {

    @Override
    public Sender convert(final Lahettaja src) {
        return new Sender(
            src.getJarjestelma(),
            conversionService.convert(src.getOrganisaatio(), Organisation.class));
    }
}
