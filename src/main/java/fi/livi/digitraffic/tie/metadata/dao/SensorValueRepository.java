package fi.livi.digitraffic.tie.metadata.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.SensorValue;

@Repository
public interface SensorValueRepository extends JpaRepository<SensorValue, Long> {


}
