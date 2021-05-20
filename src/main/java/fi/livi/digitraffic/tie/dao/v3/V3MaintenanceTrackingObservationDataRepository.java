package fi.livi.digitraffic.tie.dao.v3;

import java.time.Instant;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.v3.maintenance.V3MaintenanceTrackingObservationData;

@Repository
public interface V3MaintenanceTrackingObservationDataRepository extends JpaRepository<V3MaintenanceTrackingObservationData, Long> {

    @Query(value =  "SELECT t.*\n" +
                    "FROM MAINTENANCE_TRACKING_OBSERVATION_DATA t\n" +
                    "WHERE t.status = 'UNHANDLED'\n" +
                    "  AND t.observation_time <= (current_timestamp - (:olderThanMinutes * interval '1 MINUTE'))\n" +
                    "ORDER BY t.observation_time ASC, id ASC\n" +
                    "LIMIT :maxSize", nativeQuery = true)
    Stream<V3MaintenanceTrackingObservationData> findUnhandled(final int maxSize, final int olderThanMinutes);

    @Modifying
    @Query(value =  "DELETE FROM maintenance_tracking_observation_data\n" +
                    "WHERE id IN (\n" +
                    "    SELECT id\n" +
                    "    FROM maintenance_tracking_observation_data\n" +
                    "    WHERE observation_time <= :deleteDataBefore\n" +
                    "      AND status <>  'UNHANDLED'\n" +
                    "    ORDER BY observation_time ASC\n" +
                    "    LIMIT :maxToDelete\n" +
                    ")", nativeQuery = true)
    int deleteByObservationTimeIsBefore(final Instant deleteDataBefore, final int maxToDelete);
}
