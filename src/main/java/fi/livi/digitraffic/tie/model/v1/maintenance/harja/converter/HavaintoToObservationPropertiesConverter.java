package fi.livi.digitraffic.tie.model.v1.maintenance.harja.converter;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.AutoRegisteredConverter;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.Link;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.ObservationProperties;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.PerformedTask;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.Road;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.WorkMachine;
import fi.livi.digitraffic.tie.external.harja.Havainto;
import fi.livi.digitraffic.tie.external.harja.entities.SijaintiSchema;
import fi.livi.digitraffic.tie.helper.DateHelper;

@ConditionalOnWebApplication
@Component
public class HavaintoToObservationPropertiesConverter extends AutoRegisteredConverter<Havainto, ObservationProperties> {

    @Override
    public ObservationProperties convert(final Havainto src) {

        final SijaintiSchema sijainti = src.getSijainti();

        final Road road = sijainti != null ? convert(sijainti.getTie(), Road.class) : null;
        final Link link = sijainti != null ? convert(sijainti.getLinkki(), Link.class) : null;

        final List<PerformedTask> performedTasks =
            src.getSuoritettavatTehtavat() == null ?
                null :
                src.getSuoritettavatTehtavat().stream().map(tehtava -> convert(tehtava, PerformedTask.class))
                    .collect(Collectors.toList());

        ObservationProperties tgt =
            new ObservationProperties(
                convert(src.getTyokone(), WorkMachine.class),
                road,
                link,
                src.getSuunta(),
                src.getUrakkaid(),
                DateHelper.toZonedDateTimeAtUtc(src.getHavaintoaika()),
                performedTasks,
                src.getAdditionalProperties());

        return tgt;
    }
}
