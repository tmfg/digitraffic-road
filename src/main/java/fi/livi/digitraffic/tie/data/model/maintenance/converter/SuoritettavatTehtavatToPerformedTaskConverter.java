package fi.livi.digitraffic.tie.data.model.maintenance.converter;

import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.data.model.maintenance.PerformedTask;
import fi.livi.digitraffic.tie.harja.SuoritettavatTehtavat;

@Component
public class SuoritettavatTehtavatToPerformedTaskConverter extends AutoRegisteredConverter<SuoritettavatTehtavat, PerformedTask> {

    @Override
    public PerformedTask convert(final SuoritettavatTehtavat src) {
        return PerformedTask.fromValue(src.value());
    }
}
