package fi.livi.digitraffic.tie.data.model.json.maintenance.converter;

import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.data.model.json.maintenance.PerformedTask;
import fi.livi.digitraffic.tie.harja.SuoritettavatTehtavat;

@Component
public class SuoritettavatTehtavatToPerformedTaskConverter extends AutoRegisteredConverter<SuoritettavatTehtavat, PerformedTask> {

    @Override
    public PerformedTask convert(final SuoritettavatTehtavat src) {
        return PerformedTask.fromValue(src.value());
    }
}
