package fi.livi.digitraffic.tie.dao.roadstation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.roadstation.SensorValueHistory;

@Repository
public interface SensorValueHistoryRepository extends JpaRepository<SensorValueHistory, Long> {

}
