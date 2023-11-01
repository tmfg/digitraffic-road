package fi.livi.digitraffic.tie.dao.maintenance;

import org.springframework.data.jpa.repository.JpaRepository;

import fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingWorkMachine;

public interface MaintenanceTrackingWorkMachineRepository extends JpaRepository<MaintenanceTrackingWorkMachine, Long> {

    MaintenanceTrackingWorkMachine findByHarjaIdAndHarjaUrakkaId(final long harjaId, final long harjaUrakkaId);

}
