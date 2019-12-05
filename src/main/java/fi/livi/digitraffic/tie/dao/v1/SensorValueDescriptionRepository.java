package fi.livi.digitraffic.tie.dao.v1;

import org.springframework.data.jpa.repository.JpaRepository;

import fi.livi.digitraffic.tie.metadata.model.SensorValueDescription;
import fi.livi.digitraffic.tie.metadata.model.SensorValueDescriptionPK;

public interface SensorValueDescriptionRepository extends JpaRepository<SensorValueDescription, SensorValueDescriptionPK> {

}
