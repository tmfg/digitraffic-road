package fi.livi.digitraffic.tie.dao.data;

import java.time.Instant;
import java.util.List;

import fi.livi.digitraffic.tie.model.data.MessageAndModified;

import fi.livi.digitraffic.tie.model.data.SituationMqttMessage;

import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.data.DataDatex2Situation;

@Repository
public interface DataDatex2SituationRepository extends JpaRepository<DataDatex2Situation, Long> {

    // Uses is_latest_version = true — no DISTINCT ON needed
    @Query(value = """
        select datex2_id
        from data_datex2_situation
        where situation_id = :situationId
          and is_latest_version = true""", nativeQuery = true)
    List<Long> findLatestSituationBySituationId(final String situationId);

    // Intentionally returns all versions (used for history endpoints)
    @Query(value = """
        select datex2_id
        from data_datex2_situation
        where situation_id = :situationId
        order by situation_version desc""", nativeQuery = true)
    List<Long> findAllBySituationId(final String situationId);

    // is_latest_version = true ensures only the latest version per situation_id is considered,
    // then the time filter is applied to that latest version only (fixes the filter-before-distinct bug)
    @Query(value = """
        select datex2_id
        from data_datex2_situation
        where situation_type = :situationType
          and is_latest_version = true
          and (
              (end_time is null and start_time < :to) or
              (end_time is not null and end_time > :from and start_time < :to)
          )
          and (cast(:bbox as text) is null or ST_INTERSECTS(:bbox, geometry))""", nativeQuery = true)
    List<Long> findLatestByType(final String situationType, final Instant from, final Instant to, final Geometry bbox);

    // is_latest_version = true ensures only the latest row per situation_id is considered,
    // then the time filter is applied to that latest row only (fixes the filter-before-distinct bug)
    @Query(value = """
        select message, modified_at
        from datex2_rtti
        where is_latest_version = true
          and start_time < :to
          and (end_time is null or end_time > :from)
          and (:srtiOnly = false or is_srti = true)""", nativeQuery = true)
    List<MessageAndModified> findAllTrafficData(final Instant from, final Instant to, final boolean srtiOnly);

    // Uses is_latest_version = true — no DISTINCT ON needed
    @Query(value = """
        select message, modified_at
        from datex2_rtti
        where situation_id = :situationId
          and is_latest_version = true""", nativeQuery = true)
    List<MessageAndModified> findLatestTrafficDataMessageBySituationId(final String situationId);

    // Intentionally returns all versions (used for history endpoints)
    @Query(value = """
        select message, modified_at
        from datex2_rtti
        where situation_id = :situationId
        order by publication_time desc""", nativeQuery = true)
    List<MessageAndModified> findTrafficDataMessagesBySituationId(final String situationId);

    @Query(value = """
    select message, m.modified_at, situation_type, message_type, message_version
    from data_datex2_situation_message m
    left join data_datex2_situation on m.datex2_id = data_datex2_situation.datex2_id
    where m.modified_at > :lastUpdated
    order by m.modified_at desc
""", nativeQuery = true)
    List<SituationMqttMessage> findMessagesForMqtt(final Instant lastUpdated);
}
