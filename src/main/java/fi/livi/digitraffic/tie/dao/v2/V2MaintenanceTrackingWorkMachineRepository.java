package fi.livi.digitraffic.tie.dao.v2;

import org.springframework.data.jpa.repository.JpaRepository;

import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingWorkMachine;

public interface V2MaintenanceTrackingWorkMachineRepository extends JpaRepository<MaintenanceTrackingWorkMachine, Long> {

    MaintenanceTrackingWorkMachine findByHarjaIdAndHarjaUrakkaId(final long harjaId, final long harjaUrakkaId);

}
