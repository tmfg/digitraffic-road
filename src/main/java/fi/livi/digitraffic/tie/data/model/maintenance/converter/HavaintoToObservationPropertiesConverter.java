package fi.livi.digitraffic.tie.data.model.maintenance.converter;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.data.model.maintenance.Link;
import fi.livi.digitraffic.tie.data.model.maintenance.ObservationProperties;
import fi.livi.digitraffic.tie.data.model.maintenance.PerformedTask;
import fi.livi.digitraffic.tie.data.model.maintenance.Road;
import fi.livi.digitraffic.tie.data.model.maintenance.WorkMachine;
import fi.livi.digitraffic.tie.harja.Havainto;
import fi.livi.digitraffic.tie.harja.entities.SijaintiSchema;
import fi.livi.digitraffic.tie.helper.DateHelper;

@Component
public class HavaintoToObservationPropertiesConverter extends AutoRegisteredConverter<Havainto, ObservationProperties> {

    @Override
    public ObservationProperties convert(final Havainto src) {
        ObservationProperties tgt = new ObservationProperties();

        tgt.setContractId(src.getUrakkaid());
        tgt.setDirection(src.getSuunta());
        tgt.setObservationTime(DateHelper.toZonedDateTime(src.getHavaintoaika()));
        tgt.setAdditionalProperties(src.getAdditionalProperties());

        tgt.setWorkMachine(conversionService.convert(src.getTyokone(), WorkMachine.class));

        final SijaintiSchema sijainti = src.getSijainti();
        if (sijainti != null) {
            tgt.setRoad(conversionService.convert(src.getSijainti().getTie(), Road.class));
            tgt.setLink(conversionService.convert(src.getSijainti().getLinkki(), Link.class));
        }

        tgt.setPerformedTasks(
            src.getSuoritettavatTehtavat() == null ?
                null :
                src.getSuoritettavatTehtavat().stream().map(tehtava -> conversionService.convert(tehtava, PerformedTask.class))
                    .collect(Collectors.toList()));

        return tgt;
    }
}
