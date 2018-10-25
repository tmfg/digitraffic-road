package fi.livi.digitraffic.tie.data.model.maintenance.converter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.data.model.maintenance.WorkMachine;
import fi.livi.digitraffic.tie.harja.Tyokone;

@ConditionalOnWebApplication
@Component
public class TyokoneToWorkMachineConverter extends AutoRegisteredConverter<Tyokone, WorkMachine> {

    @Override
    public WorkMachine convert(final Tyokone src) {
        return new WorkMachine(src.getId(), src.getTyokonetyyppi());
    }
}
