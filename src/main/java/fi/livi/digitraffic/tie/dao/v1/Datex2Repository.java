package fi.livi.digitraffic.tie.dao.v1;

import java.time.Instant;
import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.v1.datex2.Datex2;

@Repository
public interface Datex2Repository extends JpaRepository<Datex2, Long> {

    String FIND_ALL_ACTIVE_AS_D =
            "WITH LATEST_SITUATION AS (\n" +
            "    WITH s AS (\n" +
            "        SELECT DISTINCT ON (s.situation_id) s.situation_id\n" +
            "                                          , s.id\n" +
            "                                          , s.datex2_id\n" +
            "        FROM datex2_situation s\n" +
            "        ORDER BY s.situation_id DESC, s.id DESC\n" +
            "    )\n" +
            "    select s.situation_id\n" +
            "         , s.id\n" +
            "         , s.datex2_id\n" +
            "    FROM DATEX2 d2\n" +
            "    INNER JOIN s ON d2.id = s.datex2_id\n" +
            "    WHERE d2.message_type IN (:messageType)\n" +
            ")\n" +
            "SELECT d.*\n" +
            "FROM datex2 d\n" +
            "INNER JOIN latest_situation ON latest_situation.datex2_id = d.id\n" +
            "WHERE exists(\n" +
            "    SELECT null\n" +
            "    FROM datex2_situation_record situation_record\n" +
            "    WHERE situation_record.datex2_situation_id = latest_situation.id\n" +
            "      AND situation_record.effective_end_time >= current_timestamp - :activeInPastHours * interval '1 hour'\n" +
            ")\n";

    String FIND_ALL_ACTIVE_SITUATION_TYPES_AS_D =
            "WITH LATEST_SITUATION AS (\n" +
            "    WITH s AS (\n" +
            "        SELECT DISTINCT ON (s.situation_id) s.situation_id\n" +
            "                                          , s.id\n" +
            "                                          , s.datex2_id\n" +
            "        FROM datex2_situation s\n" +
            "        ORDER BY s.situation_id DESC, s.id DESC\n" +
            "    )\n" +
            "    select s.situation_id\n" +
            "         , s.id\n" +
            "         , s.datex2_id\n" +
            "    FROM DATEX2 d2\n" +
            "    INNER JOIN s ON d2.id = s.datex2_id\n" +
            "    WHERE d2.situation_type IN (:situationTypes)\n" +
            ")\n" +
            "SELECT d.*\n" +
            "FROM datex2 d\n" +
            "INNER JOIN latest_situation ON latest_situation.datex2_id = d.id\n" +
            "WHERE exists(\n" +
            "    SELECT null\n" +
            "    FROM datex2_situation_record situation_record\n" +
            "    WHERE situation_record.datex2_situation_id = latest_situation.id\n" +
            "      AND situation_record.effective_end_time >= current_timestamp - :activeInPastHours * interval '1 hour'\n" +
            ")\n";

    String FIND_ALL_ACTIVE_AS_D_WITH_JSON = FIND_ALL_ACTIVE_AS_D + "  AND d.json_message IS NOT NULL\n";
    String FIND_ALL_ACTIVE_SITUATION_TYPES_AS_D_WITH_JSON = FIND_ALL_ACTIVE_SITUATION_TYPES_AS_D + "  AND d.json_message IS NOT NULL\n";
    String FIND_ALL_ACTIVE_ORDER_BY = "order by d.publication_time, d.id";

    @Query(value = FIND_ALL_ACTIVE_AS_D + FIND_ALL_ACTIVE_ORDER_BY, nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findAllActive(final String messageType, final int activeInPastHours);

    @Query(value = FIND_ALL_ACTIVE_SITUATION_TYPES_AS_D + FIND_ALL_ACTIVE_ORDER_BY, nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findAllActiveBySituationType(final int activeInPastHours, final String...situationTypes);

    @Query(value = FIND_ALL_ACTIVE_AS_D_WITH_JSON + FIND_ALL_ACTIVE_ORDER_BY, nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findAllActiveWithJson(final String messageType, final int activeInPastHours);

    @Query(value = FIND_ALL_ACTIVE_SITUATION_TYPES_AS_D_WITH_JSON + FIND_ALL_ACTIVE_ORDER_BY, nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findAllActiveBySituationTypeWithJson(int activeInPastHours, final String...situationTypes);

    @Query(value =
        "SELECT d.*\n" +
        "FROM datex2 d\n" +
        "WHERE d.publication_time >= date_trunc('month', TO_DATE('1.' || :month || '.' || :year, 'DD.MM.YYYY'))\n" +
        "AND d.publication_time < LAST_DAY(TO_DATE('1.' || :month || '.' || :year, 'DD.MM.YYYY')) + 1\n" +
        "AND d.message_type = :messageType\n" +
        "ORDER BY d.publication_time desc, d.id desc",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findHistory(@Param("messageType") final String messageType,
                             @Param("year") final int year,
                             @Param("month") final int month);

    @Query(value =
        "SELECT d.*\n" +
        "FROM datex2 d\n" +
        "WHERE EXISTS (\n" +
        "    SELECT NULL FROM datex2_situation situation\n" +
        "    INNER JOIN datex2_situation_record record ON record.datex2_situation_id = situation.id\n" +
        "    WHERE situation.datex2_id = d.id\n" + "                AND situation.situation_id = :situationId\n" +
        "    AND d.publication_time >= date_trunc('month'. TO_DATE('1.' || :month || '.' || :year, 'DD.MM.YYYY'))\n" +
        "    AND d.publication_time < LAST_DAY(TO_DATE('1.' || :month || '.' || :year, 'DD.MM.YYYY')) + 1\n" +
        "    AND d.message_type = :messageType\n" + "            )\n" +
        "ORDER BY d.publication_time desc, d.id desc",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findHistoryBySituationId(@Param("messageType") final String messageType,
                                          @Param("situationId") final String situationId,
                                          @Param("year") final int year,
                                          @Param("month") final int month);

    @Query(value =
        "SELECT d.*\n" +
        "FROM datex2 d\n" +
        "WHERE d.id in (\n" +
        "    SELECT situation.datex2_id\n" +
        "    FROM datex2_situation situation\n" +
        "    WHERE situation.situation_id = :situationId)\n" +
        "  AND message_type = :messageType", nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findBySituationIdAndMessageType(final String situationId, final String messageType);

    @Query(value =
        "SELECT d.*\n" +
        "FROM datex2 d\n" +
        "WHERE d.id in (\n" +
        "    SELECT situation.datex2_id\n" +
        "    FROM datex2_situation situation\n" +
        "    WHERE situation.situation_id = :situationId)\n" +
        "  AND message_type = :messageType\n" +
        "  AND d.json_message IS NOT NULL", nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findBySituationIdAndMessageTypeWithJson(final String situationId, final String messageType);

    @Query(value =
           "WITH v_time AS (\n" +
           "    SELECT d2s.datex2_id, MAX(sr.version_time) as version_time\n" +
           "    FROM datex2_situation_record sr\n" +
           "    INNER JOIN datex2_situation d2s ON sr.datex2_situation_id = d2s.id\n" +
           "    WHERE d2s.situation_id = :situationId\n" +
           "    GROUP BY d2s.datex2_id\n" +
           ")\n" +
           "SELECT d.*\n" +
           "FROM datex2 d\n" +
           "INNER JOIN v_time ON v_time.datex2_id = d.id\n" +
           "WHERE d.id IN (\n" +
           "    SELECT situation.datex2_id\n" +
           "    FROM datex2_situation situation\n" +
           "    WHERE situation.situation_id = :situationId)\n" +
           "ORDER BY v_time.version_time DESC, d.id DESC", nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findBySituationId(final String situationId);

    @Query(value =
           "WITH v_time AS (\n" +
           "    SELECT d2s.datex2_id, max(sr.version_time) as version_time\n" +
           "    from datex2_situation_record sr\n" +
           "    inner join datex2_situation d2s on sr.datex2_situation_id = d2s.id\n" +
           "    where d2s.situation_id = :situationId\n" +
           "    group by d2s.datex2_id\n" +
           ")\n" +
           "SELECT d.*\n" +
           "FROM datex2 d\n" +
           "INNER JOIN v_time ON v_time.datex2_id = d.id\n" +
           "WHERE d.id in (\n" +
           "    SELECT situation.datex2_id\n" +
           "    FROM datex2_situation situation\n" +
           "    WHERE situation.situation_id = :situationId)\n" +
           "      AND d.json_message IS NOT NULL\n" +
           "ORDER BY v_time.version_time DESC, d.id DESC", nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findBySituationIdWithJson(final String situationId);

    @Query("SELECT CASE WHEN count(situation) > 0 THEN TRUE ELSE FALSE END\n" +
           "FROM Datex2Situation situation\n" +
           "WHERE situation.situationId = :situationId")
    boolean existsWithSituationId(@Param("situationId") final String situationId);

    @Query(value =
               "SELECT version_time\n" +
               "FROM (\n" +
               "    SELECT ROW_NUMBER() OVER (PARTITION BY situation.SITUATION_ID ORDER BY record.version_time DESC) AS rnum\n" +
               "         , record.version_time\n" +
               "    FROM DATEX2 d\n" +
               "    INNER JOIN datex2_situation situation ON situation.datex2_id = d.id\n" +
               "    INNER JOIN datex2_situation_record record ON record.datex2_situation_id = situation.id\n" +
               "    WHERE d.situation_type = :situationType\n" +
               "      AND situation.situation_id = :situationId" +
               ") d2\n" +
               "WHERE rnum = 1",
           nativeQuery = true)
    Instant findDatex2SituationLatestVersionTime(final String situationId, final String situationType);

    @Query(value =
               "SELECT max(d.modified)\n" +
               "FROM datex2 d\n" +
               "WHERE situation_type in (:situationTypes)", nativeQuery = true)
    Instant getLastModified(final String...situationTypes);
}
