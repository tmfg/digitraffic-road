package fi.livi.digitraffic.tie.data.model.maintenance.converter;

import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.data.model.maintenance.Caption;
import fi.livi.digitraffic.tie.data.model.maintenance.ObservationFeature;
import fi.livi.digitraffic.tie.data.model.maintenance.ObservationFeatureCollection;
import fi.livi.digitraffic.tie.data.model.maintenance.WorkMachineTrackingRecord;
import fi.livi.digitraffic.tie.harja.TyokoneenseurannanKirjausRequestSchema;

@Component
public class TyokoneenseurannanKirjausToWorkMachineTrackingRecordConverter
    extends AutoRegisteredConverter<TyokoneenseurannanKirjausRequestSchema, WorkMachineTrackingRecord> {

    @Override
    public WorkMachineTrackingRecord convert(final TyokoneenseurannanKirjausRequestSchema src) {

        final ObservationFeatureCollection ofc = new ObservationFeatureCollection(
            src.getHavainnot() == null ?
                Collections.emptyList() :
                src.getHavainnot().stream().map(havainnot -> convert(havainnot.getHavainto(), ObservationFeature.class))
                    .collect(Collectors.toList()));

        return new WorkMachineTrackingRecord(convert(src.getOtsikko(), Caption.class), ofc);
    }
}
