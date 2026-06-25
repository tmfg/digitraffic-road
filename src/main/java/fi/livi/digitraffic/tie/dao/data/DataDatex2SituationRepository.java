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


    /**
     * Returns messages of the given type (and optionally version) for the latest situation version
     * of each situation_id matching the given type and time range.
     * Joins data_datex2_situation_message directly to avoid loading all message versions into memory.
     *
     * @param situationType  situation type, e.g. "ROAD_WORK", "TRAFFIC_ANNOUNCEMENT"; never null
     * @param from           start of the active time window (inclusive); never null — caller defaults to now minus 1 hour
     * @param to             end of the active time window (exclusive); never null — caller defaults to far-future sentinel
     * @param bbox           optional bounding box geometry filter; pass null to skip
     * @param messageType    message type to fetch, e.g. "DATEX2" or "SIMPPELI"; never null
     * @param messageVersion optional message version filter, e.g. "3.5" or "3.7"; pass null to match all versions
     */
    @Query(value = """
        select m.message_id, m.message, m.modified_at
        from data_datex2_situation_message m
        inner join data_datex2_situation s on s.datex2_id = m.datex2_id
        where s.situation_type = :situationType
          and s.is_latest_version = true
          and (
              (s.end_time is null and s.start_time < :to) or
              (s.end_time is not null and s.end_time > :from and s.start_time < :to)
          )
          and (cast(:bbox as text) is null or ST_INTERSECTS(:bbox, s.geometry))
          and m.message_type = :messageType
          and (:messageVersion is null or m.message_version = :messageVersion)""", nativeQuery = true)
    List<MessageAndModified> findMessagesByType(final String situationType, final Instant from, final Instant to,
                                                final Geometry bbox, final String messageType, final String messageVersion);

    /**
     * Returns the latest messages of the given type (and optionally version) for a single situationId.
     * Only messages belonging to the latest situation version (is_latest_version = true) are returned.
     *
     * @param situationId    the situation identifier
     * @param messageType    message type to fetch, e.g. "DATEX2" or "SIMPPELI"; never null
     * @param messageVersion optional message version filter; pass null to match all versions
     */
    @Query(value = """
        select m.message_id, m.message, m.modified_at
        from data_datex2_situation_message m
        inner join data_datex2_situation s on s.datex2_id = m.datex2_id
        where s.situation_id = :situationId
          and s.is_latest_version = true
          and m.message_type = :messageType
          and (:messageVersion is null or m.message_version = :messageVersion)""", nativeQuery = true)
    List<MessageAndModified> findLatestMessagesBySituationId(final String situationId, final String messageType, final String messageVersion);

    /**
     * Returns all historical messages of the given type (and optionally version) for a single situationId,
     * ordered by situation_version descending (newest first). Used for history endpoints.
     *
     * @param situationId    the situation identifier
     * @param messageType    message type to fetch, e.g. "DATEX2" or "SIMPPELI"; never null
     * @param messageVersion optional message version filter; pass null to match all versions
     */
    @Query(value = """
        select m.message_id, m.message, m.modified_at
        from data_datex2_situation_message m
        inner join data_datex2_situation s on s.datex2_id = m.datex2_id
        where s.situation_id = :situationId
          and m.message_type = :messageType
          and (:messageVersion is null or m.message_version = :messageVersion)
        order by s.situation_version desc""", nativeQuery = true)
    List<MessageAndModified> findAllMessagesBySituationId(final String situationId, final String messageType, final String messageVersion);

    // is_latest_version = true ensures only the latest row per situation_id is considered,
    // then the time filter is applied to that latest row only (fixes the filter-before-distinct bug)
    @Query(value = """
        select datex2_id as message_id, message, modified_at
        from datex2_rtti
        where is_latest_version = true
          and start_time < :to
          and (end_time is null or end_time > :from)
          and (:srtiOnly = false or is_srti = true)""", nativeQuery = true)
    List<MessageAndModified> findAllTrafficData(final Instant from, final Instant to, final boolean srtiOnly);

    // Uses is_latest_version = true — no DISTINCT ON needed
    @Query(value = """
        select datex2_id as message_id, message, modified_at
        from datex2_rtti
        where situation_id = :situationId
          and is_latest_version = true""", nativeQuery = true)
    List<MessageAndModified> findLatestTrafficDataMessageBySituationId(final String situationId);

    // Intentionally returns all versions (used for history endpoints)
    @Query(value = """
        select datex2_id as message_id, message, modified_at
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
