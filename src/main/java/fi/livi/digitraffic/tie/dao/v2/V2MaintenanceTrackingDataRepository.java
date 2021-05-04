package fi.livi.digitraffic.tie.dao.v2;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingData;

@Repository
public interface V2MaintenanceTrackingDataRepository extends JpaRepository<MaintenanceTrackingData, Long> {

    @Query(value =  "SELECT t.*\n" +
                    "FROM MAINTENANCE_TRACKING_DATA t\n" +
                    "WHERE t.status = 'UNHANDLED'\n" +
                    "ORDER BY t.id\n" +
                    "LIMIT :maxSize", nativeQuery = true)
    Stream<MaintenanceTrackingData> findUnhandled(final int maxSize);

    @Query(value =  "SELECT data.json\n" +
                    "FROM MAINTENANCE_TRACKING_DATA_TRACKING tracking\n" +
                    "INNER JOIN MAINTENANCE_TRACKING_DATA data on tracking.data_id = data.id\n" +
                    "WHERE tracking.tracking_id = :id\n" +
                    "ORDER BY data.id DESC", nativeQuery = true)
    List<String> findJsonsByTrackingId(final long id);

    @Modifying
    @Query(value =  "DELETE FROM maintenance_tracking_data\n" +
                    "WHERE id IN (\n" +
                    "    SELECT id\n" +
                    "    FROM maintenance_tracking_data\n" +
                    "    WHERE created < :olderThanDate\n" +
                    "      AND status <>  'UNHANDLED'\n" +
                    "    ORDER BY id ASC\n" +
                    "    LIMIT :maxToDelete\n" +
                    ")", nativeQuery = true)
    int deleteByCreatedIsBefore(final ZonedDateTime olderThanDate, final int maxToDelete);
}
