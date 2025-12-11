package fi.livi.digitraffic.tie.dao.data;

import java.time.Instant;
import java.util.List;

import fi.livi.digitraffic.tie.model.data.MessageAndModified;

import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.data.DataDatex2Situation;

@Repository
public interface DataDatex2SituationRepository extends JpaRepository<DataDatex2Situation, Long> {
    @Query(value = """
        select distinct on (situation_id) datex2_id
        from data_datex2_situation
        where situation_id = :situationId
        order by situation_id, situation_version desc""", nativeQuery = true)
    List<Long> findLatestSituationBySituationId(final String situationId);
    @Query(value = """
        select datex2_id
        from data_datex2_situation
        where situation_id = :situationId
        order by situation_id, situation_version desc""", nativeQuery = true)
    List<Long> findAllBySituationId(final String situationId);

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

    @Query(value = """
        select distinct on (situation_id) message, modified_at
            from datex2_rtti
            where start_time < :to
            and (end_time is null or end_time > :from)
            and (:srtiOnly = false or is_srti = true)
            order by situation_id, publication_time desc""", nativeQuery = true)
    List<MessageAndModified> findAllTrafficData(final Instant from, final Instant to, final boolean srtiOnly);

    @Query(value = """
    select distinct on (situation_id) message, modified_at
    from datex2_rtti
    where situation_id = :situationId
    order by situation_id, publication_time desc
""", nativeQuery = true)
    List<MessageAndModified> findLatestTrafficDataMessageBySituationId(final String situationId);

    @Query(value = """
    select message, modified_at
    from datex2_rtti
    where situation_id = :situationId
    order by publication_time desc
""", nativeQuery = true)
    List<MessageAndModified> findTrafficDataMessagesBySituationId(final String situationId);
}
