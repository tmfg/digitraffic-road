package fi.livi.digitraffic.tie.dao.trafficmessage.datex2;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2;
import jakarta.persistence.QueryHint;

@Repository
public interface Datex2Repository extends JpaRepository<Datex2, Long> {

    String FIND_ALL_ACTIVE_SITUATION_TYPES_AS_D = """
        WITH LATEST_SITUATION AS (
            WITH s AS (
                SELECT DISTINCT ON (s.situation_id) s.situation_id
                                                  , s.id
                                                  , s.datex2_id
                FROM datex2_situation s
                ORDER BY s.situation_id DESC, s.id DESC
            )
            select s.situation_id
                 , s.id
                 , s.datex2_id
            FROM DATEX2 d2
            INNER JOIN s ON d2.id = s.datex2_id
            WHERE d2.situation_type IN (:situationTypes)
        )
        SELECT d.*
        FROM datex2 d
        INNER JOIN latest_situation ON latest_situation.datex2_id = d.id
        WHERE exists(
            SELECT null
            FROM datex2_situation_record situation_record
            WHERE situation_record.datex2_situation_id = latest_situation.id
              AND situation_record.effective_end_time >= current_timestamp - :activeInPastHours * interval '1 hour'
        )""";


    String FIND_ALL_ACTIVE_SITUATION_TYPES_AS_D_WITH_JSON = FIND_ALL_ACTIVE_SITUATION_TYPES_AS_D + "  AND d.json_message IS NOT NULL\n";
    String FIND_ALL_ACTIVE_ORDER_BY = "order by d.publication_time, d.id";


    @Query(value = FIND_ALL_ACTIVE_SITUATION_TYPES_AS_D + FIND_ALL_ACTIVE_ORDER_BY, nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findAllActiveBySituationType(final int activeInPastHours, final String...situationTypes);

    @Query(value = FIND_ALL_ACTIVE_SITUATION_TYPES_AS_D_WITH_JSON + FIND_ALL_ACTIVE_ORDER_BY, nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findAllActiveBySituationTypeWithJson(int activeInPastHours, final String... situationTypes);

    @Query(value = """
        WITH v_time AS (
           SELECT d2s.datex2_id, MAX(sr.version_time) as version_time
           FROM datex2_situation_record sr
           INNER JOIN datex2_situation d2s ON sr.datex2_situation_id = d2s.id
           WHERE d2s.situation_id = :situationId
           GROUP BY d2s.datex2_id
        )
        SELECT d.*
        FROM datex2 d
        INNER JOIN v_time ON v_time.datex2_id = d.id
        WHERE d.id IN (
           SELECT situation.datex2_id
           FROM datex2_situation situation
           WHERE situation.situation_id = :situationId)
        ORDER BY v_time.version_time DESC, d.id DESC""", nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findBySituationId(final String situationId);

    @Query(value = """
        WITH v_time AS (
           SELECT d2s.datex2_id, max(sr.version_time) as version_time
           from datex2_situation_record sr
           inner join datex2_situation d2s on sr.datex2_situation_id = d2s.id
           where d2s.situation_id = :situationId
           group by d2s.datex2_id
        )
        SELECT d.*
        FROM datex2 d
        INNER JOIN v_time ON v_time.datex2_id = d.id
        WHERE d.id in (
           SELECT situation.datex2_id
           FROM datex2_situation situation
           WHERE situation.situation_id = :situationId)
             AND d.json_message IS NOT NULL
        ORDER BY v_time.version_time DESC, d.id DESC""", nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findBySituationIdWithJson(final String situationId);

    @Query("""
        SELECT CASE WHEN count(situation) > 0 THEN TRUE ELSE FALSE END
        FROM Datex2Situation situation
        WHERE situation.situationId = :situationId""")
    boolean existsWithSituationId(@Param("situationId") final String situationId);

    @Query(value = """
        SELECT version_time
        FROM (
           SELECT ROW_NUMBER() OVER (PARTITION BY situation.SITUATION_ID ORDER BY record.version_time DESC) AS rnum
                , record.version_time
           FROM DATEX2 d
           INNER JOIN datex2_situation situation ON situation.datex2_id = d.id
           INNER JOIN datex2_situation_record record ON record.datex2_situation_id = situation.id
           WHERE d.situation_type = :situationType
             AND situation.situation_id = :situationId) d2
        WHERE rnum = 1""",
           nativeQuery = true)
    Instant findDatex2SituationLatestVersionTime(final String situationId, final String situationType);

    @Query(value = """
        SELECT max(d.modified)
        FROM datex2 d
        WHERE situation_type in (:situationTypes)""", nativeQuery = true)
    Instant getLastModified(final String...situationTypes);

    List<Datex2> findByCreatedIsAfterOrderByCreated(final Instant createdAfter);
}
