package fi.livi.digitraffic.tie.data.model.maintenance.converter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.data.model.maintenance.Organisation;
import fi.livi.digitraffic.tie.data.model.maintenance.Sender;
import fi.livi.digitraffic.tie.harja.entities.Lahettaja;

@ConditionalOnWebApplication
@Component
public class LahettajaToSenderConverter extends AutoRegisteredConverter<Lahettaja, Sender> {

    @Override
    public Sender convert(final Lahettaja src) {
        return new Sender(
            src.getJarjestelma(),
            convert(src.getOrganisaatio(), Organisation.class));
    }
}
