package fi.livi.digitraffic.tie.dao.data;

import fi.livi.digitraffic.tie.model.data.DataDatex2Situation;

import fi.livi.digitraffic.tie.model.trafficmessage.datex2.SituationType;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface DataDatex2SituationRepository extends JpaRepository<DataDatex2Situation, Long> {
    @Query(value = """
        select distinct on (situation_id) datex2_id
        from data_datex2_situation
        where situation_id = :situationId
        order by situation_id, situation_version desc""", nativeQuery = true)
    Optional<Long> findLatestSituation(final String situationId);

    @Query(value = """
        select distinct on (situation_id) datex2_id
        from data_datex2_situation
        where situation_type = :situationType
        and (cast (:bbox as text) is null or ST_INTERSECTS(:bbox, geometry))
        and (
            (end_time is null and start_time < :to) or
            (end_time is not null and end_time > :from and start_time < :to)
        ) order by situation_id, situation_version desc""", nativeQuery = true)
    List<Long> findLatestByType(final String situationType, final Instant from, final Instant to, final Geometry bbox);

    @Query(value = """
        select distinct on (situation_id) datex2_id
        from data_datex2_situation
        where situation_type = :situationType
        and (
            (end_time is null and start_time < :to) or
            (end_time is not null and end_time > :from and start_time < :to)
        ) order by situation_id, situation_version desc""", nativeQuery = true)
    List<Long> findLatestByType(final String situationType, final Instant from, final Instant to);

    List<DataDatex2Situation> findBySituationId(final String situationId);
}
