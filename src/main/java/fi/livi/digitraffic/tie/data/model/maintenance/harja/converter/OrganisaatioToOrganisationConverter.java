package fi.livi.digitraffic.tie.data.model.maintenance.harja.converter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.AutoRegisteredConverter;
import fi.livi.digitraffic.tie.data.model.maintenance.harja.Organisation;
import fi.livi.digitraffic.tie.harja.entities.OrganisaatioSchema;

@ConditionalOnWebApplication
@Component
public class OrganisaatioToOrganisationConverter extends AutoRegisteredConverter<OrganisaatioSchema, Organisation> {

    @Override
    public Organisation convert(final OrganisaatioSchema src) {
        return new Organisation(
            src.getNimi(),
            src.getYtunnus());
    }
}
