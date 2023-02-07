package fi.livi.digitraffic.tie.dao.v2;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

import javax.persistence.QueryHint;

import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingDomainDtoV1;
import fi.livi.digitraffic.tie.helper.GeometryConstants;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTracking;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingDto;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingForMqttV2;

@Repository
public interface V2MaintenanceTrackingRepository extends JpaRepository<MaintenanceTracking, Long> {

    String STATE_ROADS_DOMAIN = "state-roads";
    String GENERIC_ALL_DOMAINS = "all";
    String GENERIC_MUNICIPALITY_DOMAINS = "municipalities";
    String MIN_TIMESTAMP = "1971-01-01T00:00Z";
    String MAX_TIMESTAMP = "2300-01-01T00:00Z";

    String DTO_SELECT_FIELDS_WITHOUT_LINE_STRING =
        "SELECT tracking.id\n" +
        "     , tracking.previous_tracking_id AS previousId\n" +
        "     , tracking.sending_time AS sendingTime\n" +
        "     , tracking.start_time AS startTime\n" +
        "     , tracking.end_time AS endTime\n" +
        "     , tracking.created AS created\n" +
        "     , ST_AsGeoJSON(ST_Snaptogrid(tracking.last_point, " + GeometryConstants.COORDINATE_SCALE_6_DIGITS_POSTGIS + ")) AS lastPointJson\n" +
        "     , tracking.direction\n" +
        "     , tracking.work_machine_id AS workMachineId\n" +
        "     , STRING_AGG(tasks.task, ',') AS tasksAsString\n" +
        "     , tracking.domain\n" +
        "     , COALESCE(contract.source, domain.source) AS source\n" +
        "     , tracking.modified\n";

    String DTO_SELECT_FIELDS_WITH_LINE_STRING =
        DTO_SELECT_FIELDS_WITHOUT_LINE_STRING +
        "     , ST_AsGeoJSON(tracking.geometry) AS geometryStringJson\n";

    String DTO_TABLES =
        "FROM maintenance_tracking tracking\n" +
        "INNER JOIN maintenance_tracking_task tasks ON tracking.id = tasks.maintenance_tracking_id\n" +
        "LEFT OUTER JOIN maintenance_tracking_domain_contract contract on (tracking.domain = contract.domain AND tracking.contract = contract.contract)\n" +
        "LEFT OUTER JOIN maintenance_tracking_domain domain on tracking.domain = domain.name\n";

    String DTO_LINESTRING_SQL =
        DTO_SELECT_FIELDS_WITH_LINE_STRING +
        DTO_TABLES;

    String DTO_LAST_POINT_SQL =
        DTO_SELECT_FIELDS_WITHOUT_LINE_STRING +
        DTO_TABLES;

    /*
     * EntityGraph causes HHH000104: firstResult/maxResults specified with collection fetch; applying IN memory! warnings
     * @EntityGraph(attributePaths = { "tasks" }, type = EntityGraph.EntityGraphType.LOAD)
    */
    MaintenanceTracking findFirstByWorkMachine_HarjaIdAndWorkMachine_HarjaUrakkaIdAndFinishedFalseOrderByModifiedDescIdDesc(final long workMachineHarjaId, final long contractHarjaId);

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="10000"))
    @Query(value = DTO_LINESTRING_SQL +
        "WHERE cast(coalesce(cast(:endFrom AS TEXT), '" + MIN_TIMESTAMP + "') as TIMESTAMP) <= tracking.end_time\n" + // inclusive
        "  AND tracking.end_time < cast(coalesce(cast(:endBefore AS TEXT), '" + MAX_TIMESTAMP + "') as TIMESTAMP)\n" + // exclusive
        "  AND cast(coalesce(cast(:createdAfter AS TEXT), '" + MIN_TIMESTAMP + "') as TIMESTAMP) < tracking.created \n" + // exclusive
        "  AND tracking.created < cast(coalesce(cast(:createdBefore AS TEXT), '" + MAX_TIMESTAMP + "') as TIMESTAMP)\n" + // exclusive
        "  AND (:area IS NULL OR ST_INTERSECTS(:area, tracking.geometry) = TRUE)\n" +
        "  AND ( coalesce(array_length(cast('{' || :tasks || '}' as varchar[]), 1), 0) = 0 OR \n" +
        "    EXISTS (\n" +
        "      SELECT 1\n" +
        "      FROM maintenance_tracking_task t\n" +
        "      WHERE t.maintenance_tracking_id = tracking.id\n" +
        "        AND t.task IN (:tasks)\n" +
        "    )\n" +
        "  )\n" +
        "  AND tracking.domain IN (:domains)\n" +
        "  AND domain.source IS NOT NULL\n" +
        "GROUP BY tracking.id, contract.source, domain.source\n" +
        "ORDER BY tracking.id",
           nativeQuery = true)
    List<MaintenanceTrackingDto> findByAgeAndBoundingBoxAndTasks(final ZonedDateTime endFrom, final ZonedDateTime endBefore,
                                                                 final ZonedDateTime createdAfter, final ZonedDateTime createdBefore,
                                                                 final Geometry area, final List<String> tasks, final List<String> domains);


    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="10000"))
    @Query(value = DTO_LAST_POINT_SQL +
                   "WHERE tracking.id IN (\n" +
                   "    SELECT max(t.id)\n" + // select latest id per machine
                   "    FROM maintenance_tracking t\n" +
                   "    WHERE (t.end_time BETWEEN :from AND :to)\n" +
                   "      AND ST_INTERSECTS(:area, t.last_point) = TRUE\n" +
                   "    GROUP BY t.work_machine_id\n" +
                   "  )\n" +
                   "  AND ( coalesce(array_length(cast('{' || :tasks || '}' as varchar[]), 1), 0) = 0 OR \n" +
                   "    EXISTS (\n" +
                   "      SELECT 1\n" +
                   "      FROM maintenance_tracking_task t\n" +
                   "      WHERE t.maintenance_tracking_id = tracking.id\n" +
                   "        AND t.task IN (:tasks)" +
                   "    )\n" +
                   "  )\n" +
                   "  AND tracking.domain IN (:domains) \n" +
                   "  AND domain.source IS NOT NULL\n" +
                   "GROUP BY tracking.id, contract.source, domain.source\n" +
                   "ORDER by tracking.id",
           nativeQuery = true)
    List<MaintenanceTrackingDto> findLatestByAgeAndBoundingBoxAndTasks(final ZonedDateTime from, final ZonedDateTime to, final Geometry area,
                                                                       final List<String> tasks, final List<String> domains);

    @Query(value = DTO_LINESTRING_SQL +
                   "WHERE tracking.id = :id\n" +
                   "  AND domain.source IS NOT NULL\n" +
                   "GROUP BY tracking.id, contract.source, domain.source",
           nativeQuery = true)
    MaintenanceTrackingDto getDto(long id);

    @Query(value =
        "select name\n" +
        "     , source\n" +
        "from maintenance_tracking_domain\n" +
        "where source IS NOT NULL\n" +
        "order by name", nativeQuery = true)
    List<MaintenanceTrackingDomainDtoV1> getDomains();

    @Query(value =
       "select name\n" +
       "     , source\n" +
       "     , case when name = '" + STATE_ROADS_DOMAIN + "' then 0 else ROW_NUMBER() OVER (ORDER BY name) end AS rnum\n" +
       "from maintenance_tracking_domain\n" +
       "where source IS NOT NULL\n" +
       "UNION\n" +
       "SELECT '" + GENERIC_ALL_DOMAINS + "', 'All domains', -2\n" +
       "UNION\n" +
       "SELECT '" + GENERIC_MUNICIPALITY_DOMAINS + "', 'All municipality domains', -1\n" +
       "order by rnum", nativeQuery = true)
    List<MaintenanceTrackingDomainDtoV1> getDomainsWithGenerics();

    @Query(value =
        "select name\n" +
        "from maintenance_tracking_domain\n" +
        "where source IS NOT NULL\n" +
        "order by name",
        nativeQuery = true)
    List<String> getRealDomainNames();

    @Query(value =
        "select tracking.id, tracking.domain, tracking.end_time as endTime, tracking.created as createdTime, ST_X(last_point) as x, ST_Y(last_point) as y" +
        ", STRING_AGG(tasks.task, ',') AS tasksAsString" +
        ", COALESCE(contract.source, domain.source) AS source\n" +
        DTO_TABLES +
        "WHERE tracking.created > :createdFromExclusive\n" +
        "  AND domain.source IS NOT NULL\n" +
        "GROUP BY tracking.id, contract.source, domain.source",
        nativeQuery = true)
    List<MaintenanceTrackingForMqttV2> findTrackingsCreatedAfter(final Instant createdFromExclusive);

    @Query(value =
        DTO_LAST_POINT_SQL +
        "WHERE tracking.created > :createdFromExclusive\n" +
        "  AND domain.source IS NOT NULL\n" +
        "GROUP BY tracking.id, contract.source, domain.source",
        nativeQuery = true)
    List<MaintenanceTrackingDto> findTrackingsLatestPointsCreatedAfter(final Instant createdFromExclusive);

    @Query(value =
           "select max(created)\n" +
           "from maintenance_tracking t\n" +
           "where t.domain IN (:domains)",
           nativeQuery = true)
    Instant findLastUpdatedForDomain(final List<String> domains);
}
