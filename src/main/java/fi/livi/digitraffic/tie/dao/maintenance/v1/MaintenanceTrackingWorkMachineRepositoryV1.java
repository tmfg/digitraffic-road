package fi.livi.digitraffic.tie.dao.maintenance.v1;

import fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingWorkMachine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceTrackingWorkMachineRepositoryV1 extends JpaRepository<MaintenanceTrackingWorkMachine, Long> {

    MaintenanceTrackingWorkMachine findByHarjaIdAndHarjaUrakkaId(final long harjaId, final long harjaUrakkaId);

}
