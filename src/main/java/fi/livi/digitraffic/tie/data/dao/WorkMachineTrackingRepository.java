package fi.livi.digitraffic.tie.data.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.data.model.maintenance.json.WorkMachineTracking;

@Repository
public interface WorkMachineTrackingRepository extends JpaRepository<WorkMachineTracking, Long> {

    @Modifying
    @Query(value = "UPDATE WORK_MACHINE_TRACKING\n" +
                   "SET type = record -> 'observationFeatureCollection' -> 'features' -> 0 -> 'geometry' ->> 'type'\n" +
                   "WHERE type IS NULL",
           nativeQuery = true)
    int updateWorkMachineTrackingType();
}
