package fi.livi.digitraffic.tie.dao.v2;

import java.time.ZonedDateTime;
import java.util.List;

import javax.persistence.QueryHint;

import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTracking;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingDto;

@Repository
public interface V2MaintenanceTrackingRepository extends JpaRepository<MaintenanceTracking, Long> {

    String DTO_SELECT_FIELDS_WITHOUT_LINE_STRING =
        "SELECT tracking.id\n" +
        "     , tracking.sending_time AS sendingTime\n" +
        "     , tracking.start_time AS startTime\n" +
        "     , tracking.end_time AS endTime\n" +
        "     , ST_AsGeoJSON(tracking.last_point) AS lastPointJson\n" +
        "     , tracking.direction\n" +
        "     , tracking.work_machine_id AS workMachineId\n" +
        "     , STRING_AGG(tasks.task, ',') AS tasksAsString\n";

    String DTO_SELECT_FIELDS_WITH_LINE_STRING =
        DTO_SELECT_FIELDS_WITHOUT_LINE_STRING +
        "     , ST_AsGeoJSON(ST_Simplify(tracking.line_string, 0.00005, true)) AS lineStringJson\n";

    String DTO_TABLES =
        "FROM maintenance_tracking tracking\n" +
        "INNER JOIN maintenance_tracking_work_machine machine ON tracking.work_machine_id = machine.id\n" +
        "INNER JOIN maintenance_tracking_task tasks ON tracking.id = tasks.maintenance_tracking_id\n";

    String DTO_LINESTRING_SQL =
        DTO_SELECT_FIELDS_WITH_LINE_STRING +
        DTO_TABLES;

    String DTO_LAST_POINT_SQL =
        DTO_SELECT_FIELDS_WITHOUT_LINE_STRING +
        DTO_TABLES;


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

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="10000"))
    @Query(value = DTO_LINESTRING_SQL +
                   "WHERE (tracking.end_time BETWEEN :from AND :to)\n" +
                   "  AND (ST_INTERSECTS(:area, tracking.last_point) = true OR ST_INTERSECTS(:area, tracking.line_string) = true)\n" +
                   "GROUP BY tracking.id\n" +
                   "ORDER BY tracking.id",
           nativeQuery = true)
    List<MaintenanceTrackingDto> findByAgeAndBoundingBox(final ZonedDateTime from, final ZonedDateTime to, final Geometry area);

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="10000"))
    @Query(value = DTO_LINESTRING_SQL +
                   "WHERE (tracking.end_time BETWEEN :from AND :to)\n" +
                   "  AND (ST_INTERSECTS(:area, tracking.last_point) = true or ST_INTERSECTS(:area, tracking.line_string) = true)\n" +
                   "  AND EXISTS (\n" +
                   "    SELECT 1\n" +
                   "    FROM maintenance_tracking_task t\n" +
                   "    WHERE t.maintenance_tracking_id = tracking.id\n" +
                   "      AND t.task IN (:tasks)" +
                   "  )\n" +
                   "GROUP BY tracking.id\n" +
                   "ORDER BY tracking.id",
           nativeQuery = true)
    List<MaintenanceTrackingDto> findByAgeAndBoundingBoxAndTasks(final ZonedDateTime from, final ZonedDateTime to, final Geometry area, final List<String> tasks);

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="10000"))
    @Query(value = DTO_LAST_POINT_SQL +
                   "WHERE tracking.id IN (\n" +
                   "    SELECT MAX(t.id)\n" + // select latest id per machine
                   "    FROM maintenance_tracking t\n" +
                   "    WHERE (t.end_time BETWEEN :from AND :to)\n" +
                   "      AND ST_INTERSECTS(:area, t.last_point) = true\n" +
                   "    GROUP BY t.work_machine_id\n" +
                   ")\n" +
                   "GROUP BY tracking.id\n" +
                   "ORDER by tracking.id",
           nativeQuery = true)
    List<MaintenanceTrackingDto> findLatestByAgeAndBoundingBox(final ZonedDateTime from, final ZonedDateTime to, final Geometry area);

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="10000"))
    @Query(value = DTO_LAST_POINT_SQL +
                   "WHERE tracking.id IN (\n" +
                   "    SELECT max(t.id)\n" + // select latest id per machine
                   "    FROM maintenance_tracking t\n" +
                   "    WHERE (t.end_time BETWEEN :from AND :to)\n" +
                   "      AND ST_INTERSECTS(:area, t.last_point) = true\n" +
                   "    GROUP BY t.work_machine_id\n" +
                   ")\n" +
                   "  AND EXISTS (\n" +
                   "    SELECT 1\n" +
                   "    FROM maintenance_tracking_task t\n" +
                   "    WHERE t.maintenance_tracking_id = tracking.id\n" +
                   "      AND t.task IN (:tasks)" +
                   "  )\n" +
                   "GROUP BY tracking.id\n" +
                   "ORDER by tracking.id",
           nativeQuery = true)
    List<MaintenanceTrackingDto> findLatestByAgeAndBoundingBoxAndTasks(final ZonedDateTime from, final ZonedDateTime to, final Geometry area, final List<String> tasks);

    @Query(value = DTO_LINESTRING_SQL +
                   "WHERE tracking.id = :id\n" +
                   "GROUP BY tracking.id\n",
           nativeQuery = true)
    MaintenanceTrackingDto getDto(long id);
}
