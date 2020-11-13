package fi.livi.digitraffic.tie.dao.v2;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTracking;

@Repository
public interface V2MaintenanceTrackingRepository extends JpaRepository<MaintenanceTracking, Long> {

    @EntityGraph(attributePaths = { "tasks" }, type = EntityGraph.EntityGraphType.LOAD)
    List<MaintenanceTracking> findAllByWorkMachine_HarjaIdAndWorkMachine_HarjaUrakkaIdOrderByModifiedAscIdAsc(final long workMachineHarjaId, final long contractHarjaId);

    /**
     * EntityGraph causes HHH000104: firstResult/maxResults specified with collection fetch; applying in memory! warnings
     * @EntityGraph(attributePaths = { "tasks" }, type = EntityGraph.EntityGraphType.LOAD)
    */
    MaintenanceTracking findFirstByWorkMachine_HarjaIdAndWorkMachine_HarjaUrakkaIdAndFinishedFalseOrderByModifiedDescIdDesc(final long workMachineHarjaId, final long contractHarjaId);

    @Modifying
    @Query(nativeQuery = true,
           value = "INSERT INTO maintenance_tracking_data_tracking(data_id, tracking_id) VALUES (:dataId, :trackingId) ON CONFLICT (data_id, tracking_id) DO NOTHING")
    void addTrackingData(final long dataId, final long trackingId);
}
