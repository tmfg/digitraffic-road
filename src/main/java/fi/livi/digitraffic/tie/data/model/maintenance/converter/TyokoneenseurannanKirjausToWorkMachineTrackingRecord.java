package fi.livi.digitraffic.tie.data.model.maintenance.converter;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.data.model.maintenance.Caption;
import fi.livi.digitraffic.tie.data.model.maintenance.ObservationFeature;
import fi.livi.digitraffic.tie.data.model.maintenance.ObservationFeatureCollection;
import fi.livi.digitraffic.tie.data.model.maintenance.WorkMachineTrackingRecord;
import fi.livi.digitraffic.tie.harja.TyokoneenseurannanKirjausRequestSchema;

@Component
public class TyokoneenseurannanKirjausToWorkMachineTrackingRecord
    extends AutoRegisteredConverter<TyokoneenseurannanKirjausRequestSchema, WorkMachineTrackingRecord> {

    @Override
    public WorkMachineTrackingRecord convert(final TyokoneenseurannanKirjausRequestSchema src) {
        final WorkMachineTrackingRecord tgt = new WorkMachineTrackingRecord();

        tgt.setCaption(conversionService.convert(src.getOtsikko(), Caption.class));
        tgt.setObservationFeatureCollection(
            new ObservationFeatureCollection(
                src.getHavainnot() == null ?
                null :
                src.getHavainnot().stream().map(havainnot -> conversionService.convert(havainnot.getHavainto(), ObservationFeature.class))
                    .collect(Collectors.toList())));

        return tgt;
    }
}
