package fi.livi.digitraffic.tie.dao.maintenance;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingObservationData;

@Repository
public interface MaintenanceTrackingObservationDataRepository extends JpaRepository<MaintenanceTrackingObservationData, Long> {

    @Query(value = """
            SELECT t.id
                 , t.observation_time
                 , t.sending_time
                 , t.created
                 , t.modified
                 , t.json
                 , t.harja_workmachine_id
                 , t.harja_contract_id
                 , t.sending_system
                 , t.status
                 , t.handling_info
                 , t.hash
                 , t.s3_uri
            FROM MAINTENANCE_TRACKING_OBSERVATION_DATA t
            WHERE t.status = 'UNHANDLED'
              AND t.observation_time <= (current_timestamp - (:olderThanMinutes * interval '1 MINUTE'))
            ORDER BY t.observation_time, id
            LIMIT :maxSize""", nativeQuery = true)
    Stream<MaintenanceTrackingObservationData> findUnhandled(final int maxSize, final int olderThanMinutes);

    @Modifying
    @Query(value = """
            DELETE FROM maintenance_tracking_observation_data
            WHERE id IN (
                SELECT id
                FROM maintenance_tracking_observation_data
                WHERE observation_time <= :deleteDataBefore
                  AND status <>  'UNHANDLED'
                ORDER BY observation_time
                LIMIT :maxToDelete
            )""", nativeQuery = true)
    int deleteByObservationTimeIsBefore(final Instant deleteDataBefore, final int maxToDelete);

    @Query(value = """
            SELECT data.json
            FROM maintenance_tracking_observation_data_tracking tracking
            INNER JOIN maintenance_tracking_observation_data data on tracking.data_id = data.id
            WHERE tracking.tracking_id = :id
            ORDER BY data.id DESC""", nativeQuery = true)
    List<String> findJsonsByTrackingId(final long id);

}
