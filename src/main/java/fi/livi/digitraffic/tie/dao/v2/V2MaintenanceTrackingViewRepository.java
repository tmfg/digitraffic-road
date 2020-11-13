package fi.livi.digitraffic.tie.dao.v2;

import java.time.ZonedDateTime;
import java.util.List;

import javax.persistence.QueryHint;

import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingViewDto;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingDto;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;

@Repository
public interface V2MaintenanceTrackingViewRepository extends JpaRepository<MaintenanceTrackingViewDto, Long> {

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    @Query(value =
               "SELECT tracking \n" +
               "FROM #{#entityName} tracking\n" +
               "WHERE tracking.id IN (\n" +
               "    SELECT max(t.id)\n" + // select latest id per machine
               "    FROM #{#entityName} t\n" +
               "    WHERE t.endTime BETWEEN :from AND :to\n" +
               "      AND intersects(t.lastPoint, :area) = true\n" +
               "    group by t.workMachine\n" +
               ")\n" +
               "ORDER by tracking.id")
    @EntityGraph(attributePaths = { "tasks", "workMachine" }, type = EntityGraph.EntityGraphType.LOAD)
    List<MaintenanceTrackingDto> findLatestByAgeAndBoundingBox(final ZonedDateTime from, final ZonedDateTime to, final Geometry area);

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    @Query(value =
                "SELECT tracking \n" +
                "FROM #{#entityName} tracking\n" +
                "WHERE tracking.id IN (\n" +
                "    SELECT max(t.id)\n" + // select latest id per machine
                "    FROM #{#entityName} t\n" +
                "    WHERE t.endTime BETWEEN :from AND :to\n" +
                "      AND intersects(t.lastPoint, :area) = true\n" +
                "    GROUP BY t.workMachine\n" +
                ")\n" +
                "  AND exists (\n" +
                "    SELECT t " +
                "    FROM #{#entityName} t\n" +
                "    JOIN t.tasks task\n" +
                "    WHERE t = tracking\n" +
                "      AND task in (:tasks)" +
                "  )\n" +
                "ORDER by tracking.id")
    @EntityGraph(attributePaths = { "tasks", "workMachine" }, type = EntityGraph.EntityGraphType.LOAD)
    List<MaintenanceTrackingDto> findLatestByAgeAndBoundingBoxAndTasks(final ZonedDateTime from, final ZonedDateTime to, final Geometry area, final List<MaintenanceTrackingTask> tasks);

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    @Query(value =
               "SELECT tracking \n" +
               "FROM #{#entityName} tracking\n" +
               "WHERE tracking.endTime BETWEEN :from AND :to\n" +
               "  AND intersects( coalesce(tracking.lineString, tracking.lastPoint), :area ) = true\n" +
               "ORDER by tracking.id")
    @EntityGraph(attributePaths = { "tasks", "workMachine" }, type = EntityGraph.EntityGraphType.LOAD)
    List<MaintenanceTrackingDto> findByAgeAndBoundingBox(final ZonedDateTime from, final ZonedDateTime to, final Geometry area);

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    @Query(value =
               "SELECT tracking \n" +
               "FROM #{#entityName} tracking\n" +
               "WHERE tracking.endTime BETWEEN :from AND :to\n" +
               "  AND intersects( coalesce(tracking.lineString, tracking.lastPoint), :area ) = true\n" +
               "  AND exists (\n" +
               "    SELECT t " +
               "    FROM #{#entityName} t\n" +
               "    JOIN t.tasks task\n" +
               "    WHERE t = tracking\n" +
               "      AND task in (:tasks)" +
               " )\n" +
               "ORDER by tracking.id")
    @EntityGraph(attributePaths = { "tasks", "workMachine" }, type = EntityGraph.EntityGraphType.LOAD)
    List<MaintenanceTrackingDto> findByAgeAndBoundingBoxAndTasks(final ZonedDateTime from, final ZonedDateTime to, final Geometry area, final List<MaintenanceTrackingTask> tasks);

}
