package fi.livi.digitraffic.tie.dao.maintenance;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.maintenance.mqtt.MaintenanceTrackingForMqttV2;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingDomainDtoV1;
import fi.livi.digitraffic.tie.model.maintenance.MaintenanceTracking;

@Repository
public interface MaintenanceTrackingRepository extends JpaRepository<MaintenanceTracking, Long> {

    String DTO_TABLES = """
            FROM maintenance_tracking tracking
            LEFT OUTER JOIN maintenance_tracking_domain_contract contract on (tracking.domain = contract.domain AND tracking.contract = contract.contract)
            LEFT OUTER JOIN maintenance_tracking_domain domain on tracking.domain = domain.name
            """;

    /*
     * EntityGraph causes HHH000104: firstResult/maxResults specified with collection fetch; applying IN memory! warnings
     * @EntityGraph(attributePaths = { "tasks" }, type = EntityGraph.EntityGraphType.LOAD)
    */
    MaintenanceTracking findFirstByWorkMachine_HarjaIdAndWorkMachine_HarjaUrakkaIdAndFinishedFalseOrderByModifiedDescIdDesc(final long workMachineHarjaId, final long contractHarjaId);

    @Query(value = """
            select name
                 , source
            from maintenance_tracking_domain
            where source IS NOT NULL
            order by name""", nativeQuery = true)
    List<MaintenanceTrackingDomainDtoV1> getDomains();

    @Query(value =
            "select name\n" +
            "     , source\n" +
            "     , modified as dataUpdatedTime\n" +
            "     , case when name = '" + MaintenanceTrackingDao.STATE_ROADS_DOMAIN + "' then 0 else ROW_NUMBER() OVER (ORDER BY name) end AS rnum\n" +
            "from maintenance_tracking_domain\n" +
            "where source IS NOT NULL\n" +
            "UNION\n" +
            "SELECT '" + MaintenanceTrackingDao.GENERIC_ALL_DOMAINS + "', 'All domains', null, -2\n" +
            "UNION\n" +
            "SELECT '" + MaintenanceTrackingDao.GENERIC_MUNICIPALITY_DOMAINS + "', 'All municipality domains', null, -1\n" +
            "order by rnum", nativeQuery = true)
    List<MaintenanceTrackingDomainDtoV1> getDomainsWithGenerics();

    @Query(value = """
            select name
            from maintenance_tracking_domain
            where source IS NOT NULL
            order by name""",
            nativeQuery = true)
    Set<String> getRealDomainNames();

    @Query(value =
            "select tracking.id, tracking.domain, tracking.end_time as endTime, tracking.created as createdTime, ST_X(last_point) as x, ST_Y(last_point) as y" +
            ", ARRAY_TO_STRING(tracking.tasks, ',') AS tasksAsString\n" +
            ", COALESCE(contract.source, domain.source) AS source\n" +
            DTO_TABLES +
            "WHERE tracking.created > :createdFromExclusive\n" +
            "  AND domain.source IS NOT NULL\n",
            nativeQuery = true)
    List<MaintenanceTrackingForMqttV2> findTrackingsCreatedAfter(final Instant createdFromExclusive);

    @Query(value = """
            select max(created)
            from maintenance_tracking t
            where t.domain IN (:domains)""",
            nativeQuery = true)
    Instant findLastUpdatedForDomain(final Set<String> domains);
}
