package fi.livi.digitraffic.tie.data.model.json.maintenance.converter;

import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.data.model.json.maintenance.Organisation;
import fi.livi.digitraffic.tie.harja.entities.OrganisaatioSchema;

@Component
public class OrganisaatioToOrganisationConverter extends AutoRegisteredConverter<OrganisaatioSchema, Organisation> {

    @Override
    public Organisation convert(final OrganisaatioSchema src) {
        return new Organisation(
            src.getNimi(),
            src.getYtunnus());
    }
}
