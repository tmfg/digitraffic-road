package fi.livi.digitraffic.tie.dao.v2;

import java.time.ZonedDateTime;
import java.util.List;

import javax.persistence.QueryHint;

import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingForMqttV2;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.maintenance.v1.DomainDto;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTracking;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingDto;

@Repository
public interface V2MaintenanceTrackingRepository extends JpaRepository<MaintenanceTracking, Long> {

    double COORDINATE_PRECISION = 0.000001;
    double SIMPLIFY_DOUGLAS_PEUCKER_TOLERANCE = 0.00005;
    String STATE_ROADS_DOMAIN = "state-roads";
    String GENERIC_ALL_DOMAINS = "all";
    String GENERIC_MUNICIPALITY_DOMAINS = "municipalities";

    String DTO_SELECT_FIELDS_WITHOUT_LINE_STRING =
        "SELECT tracking.id\n" +
        "     , tracking.previous_tracking_id AS previousId\n" +
        "     , tracking.sending_time AS sendingTime\n" +
        "     , tracking.start_time AS startTime\n" +
        "     , tracking.end_time AS endTime\n" +
        "     , ST_AsGeoJSON(ST_Snaptogrid(tracking.last_point, " + COORDINATE_PRECISION + ")) AS lastPointJson\n" +
        "     , tracking.direction\n" +
        "     , tracking.work_machine_id AS workMachineId\n" +
        "     , STRING_AGG(tasks.task, ',') AS tasksAsString\n" +
        "     , tracking.domain\n" +
        "     , COALESCE(contract.source, domain.source) AS source\n";

    String DTO_SELECT_FIELDS_WITH_LINE_STRING =
        DTO_SELECT_FIELDS_WITHOUT_LINE_STRING +
        // ST_Snaptogrid will convert linestring with only same locations ie. [ [a,b], [a,b]] to null -> returns only valid linestrings
        "     , ST_AsGeoJSON(ST_Simplify(ST_Snaptogrid(tracking.line_string, " + COORDINATE_PRECISION + "), " + SIMPLIFY_DOUGLAS_PEUCKER_TOLERANCE + ", true)) AS lineStringJson\n";

    String DTO_TABLES =
        "FROM maintenance_tracking tracking\n" +
        "INNER JOIN maintenance_tracking_work_machine machine ON tracking.work_machine_id = machine.id\n" +
        "INNER JOIN maintenance_tracking_task tasks ON tracking.id = tasks.maintenance_tracking_id\n" +
        "LEFT OUTER JOIN maintenance_tracking_domain_contract contract on (tracking.domain = contract.domain AND tracking.contract = contract.contract)\n" +
        "LEFT OUTER JOIN maintenance_tracking_domain domain on tracking.domain = domain.name\n";

    String DTO_LINESTRING_SQL =
        DTO_SELECT_FIELDS_WITH_LINE_STRING +
        DTO_TABLES;

    String DTO_LAST_POINT_SQL =
        DTO_SELECT_FIELDS_WITHOUT_LINE_STRING +
        DTO_TABLES;

    /**
     * EntityGraph causes HHH000104: firstResult/maxResults specified with collection fetch; applying in memory! warnings
     * @EntityGraph(attributePaths = { "tasks" }, type = EntityGraph.EntityGraphType.LOAD)
    */
    MaintenanceTracking findFirstByWorkMachine_HarjaIdAndWorkMachine_HarjaUrakkaIdAndFinishedFalseOrderByModifiedDescIdDesc(final long workMachineHarjaId, final long contractHarjaId);

    @Modifying
    @Query(nativeQuery = true,
           value = "INSERT INTO maintenance_tracking_observation_data_tracking(data_id, tracking_id) VALUES (:dataId, :trackingId) ON CONFLICT (data_id, tracking_id) DO NOTHING")
    void addTrackingObservationData(final long dataId, final long trackingId);


    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="10000"))
    @Query(value = DTO_LINESTRING_SQL +
                   "WHERE (tracking.end_time BETWEEN :from AND :to)\n" +
                   "  AND (ST_INTERSECTS(:area, tracking.last_point) = true OR ST_INTERSECTS(:area, tracking.line_string) = true)\n" +
                   "  AND tracking.domain in (:domains) \n" +
                   "  AND domain.source is not null\n" +
                   "GROUP BY tracking.id, contract.source, domain.source\n" +
                   "ORDER BY tracking.id",
           nativeQuery = true)
    List<MaintenanceTrackingDto> findByAgeAndBoundingBox(final ZonedDateTime from, final ZonedDateTime to, final Geometry area,
                                                         final List<String> domains);

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
                   "  AND tracking.domain in (:domains) \n" +
                   "  AND domain.source is not null\n" +
                   "GROUP BY tracking.id, contract.source, domain.source\n" +
                   "ORDER BY tracking.id",
           nativeQuery = true)
    List<MaintenanceTrackingDto> findByAgeAndBoundingBoxAndTasks(final ZonedDateTime from, final ZonedDateTime to, final Geometry area,
                                                                 final List<String> tasks, final List<String> domains);

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="10000"))
    @Query(value = DTO_LAST_POINT_SQL +
                   "WHERE tracking.id IN (\n" +
                   "    SELECT MAX(t.id)\n" + // select latest id per machine
                   "    FROM maintenance_tracking t\n" +
                   "    WHERE (t.end_time BETWEEN :from AND :to)\n" +
                   "      AND ST_INTERSECTS(:area, t.last_point) = true\n" +
                   "    GROUP BY t.work_machine_id\n" +
                   ")\n" +
                   "  AND tracking.domain in (:domains) \n" +
                   "  AND domain.source is not null\n" +
                   "GROUP BY tracking.id, contract.source, domain.source\n" +
                   "ORDER by tracking.id",
           nativeQuery = true)
    List<MaintenanceTrackingDto> findLatestByAgeAndBoundingBox(final ZonedDateTime from, final ZonedDateTime to, final Geometry area,
                                                               final List<String> domains);

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
                   "  AND tracking.domain in (:domains) \n" +
                   "  AND domain.source is not null\n" +
                   "GROUP BY tracking.id, contract.source, domain.source\n" +
                   "ORDER by tracking.id",
           nativeQuery = true)
    List<MaintenanceTrackingDto> findLatestByAgeAndBoundingBoxAndTasks(final ZonedDateTime from, final ZonedDateTime to, final Geometry area,
                                                                       final List<String> tasks, final List<String> domains);

    @Query(value = DTO_LINESTRING_SQL +
                   "WHERE tracking.id = :id\n" +
                   "  AND domain.source is not null\n" +
                   "GROUP BY tracking.id, contract.source, domain.source",
           nativeQuery = true)
    MaintenanceTrackingDto getDto(long id);

    @Query(value =
        "select name\n" +
        "     , source\n" +
        "     , case when name = '" + STATE_ROADS_DOMAIN + "' then 0 else ROW_NUMBER() OVER (ORDER BY name) end AS rnum\n" +
        "from maintenance_tracking_domain\n" +
        "where source is not null\n" +
        "UNION\n" +
        "SELECT '" + GENERIC_ALL_DOMAINS + "', 'All domains', -2\n" +
        "UNION\n" +
        "SELECT '" + GENERIC_MUNICIPALITY_DOMAINS + "', 'All municipality domains', -1\n" +
        "order by rnum",
        nativeQuery = true)
    List<DomainDto> getDomainsWithGenerics();

    @Query(value =
        "select name\n" +
        "from maintenance_tracking_domain\n" +
        "where source is not null\n" +
        "order by name",
        nativeQuery = true)
    List<String> getRealDomainNames();

    @Query(value = "select tracking.id, tracking.domain, tracking.end_time, ST_X(last_point) as x, ST_Y(last_point) as y" +
        ", STRING_AGG(tasks.task, ',') AS tasksAsString" +
        ", COALESCE(contract.source, domain.source) AS source\n" +
        DTO_TABLES +
        "WHERE tracking.created between :from and :to\n" +
        "AND tracking.domain != '" + STATE_ROADS_DOMAIN + "'\n" +
        "GROUP BY tracking.id, contract.source, domain.source",
        nativeQuery = true)
    List<MaintenanceTrackingForMqttV2> findTrackingsForNonStateRoads(final ZonedDateTime from, final ZonedDateTime to);
}
