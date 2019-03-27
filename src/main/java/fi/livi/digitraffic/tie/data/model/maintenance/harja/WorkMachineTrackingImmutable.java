package fi.livi.digitraffic.tie.data.model.maintenance.harja;

import java.time.ZonedDateTime;

import javax.persistence.Table;

import org.hibernate.annotations.TypeDef;

import fi.livi.digitraffic.tie.conf.postgres.WorkMachineTrackingRecordUserType;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;

@TypeDef(name = "WorkMachineTrackingRecordUserType", typeClass = WorkMachineTrackingRecordUserType.class)
@Table(name = "WORK_MACHINE_TRACKING")
public interface WorkMachineTrackingImmutable {

    Long getId();

    WorkMachineTrackingRecord getRecord();

    Geometry.Type getType();

    ZonedDateTime getCreated();

    ZonedDateTime getHandled();
}
