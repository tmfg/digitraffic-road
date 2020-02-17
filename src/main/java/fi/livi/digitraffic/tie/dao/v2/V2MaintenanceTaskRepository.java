package fi.livi.digitraffic.tie.dao.v2;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationTask;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTask;

@Repository
public interface V2MaintenanceTaskRepository extends JpaRepository<MaintenanceTask, Long> {

    List<MaintenanceRealizationTask> findAllByOrderById();
}
