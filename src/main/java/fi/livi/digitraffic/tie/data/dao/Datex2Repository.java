package fi.livi.digitraffic.tie.data.dao;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.data.model.Datex2;

@Repository
public interface Datex2Repository extends JpaRepository<Datex2, Long> {
    @Query(name = "find_latest_import_time")
    LocalDateTime findLatestImportTime(@Param("messageType") final String messageType);

    @Query(value =
            "SELECT d.*\n" +
            "FROM datex2 d\n" +
            "WHERE d.id IN (\n" +
            "  SELECT datex2_id\n" +
            "  FROM (\n" +
                      // Latest Datex2-message of situation and it's last record by end time\n" +
            "         SELECT ROW_NUMBER() OVER (PARTITION BY situation.SITUATION_ID ORDER BY d.PUBLICATION_TIME DESC, record.OVERALL_END_TIME DESC NULLS LAST) AS rnum\n" +
            "           , d.publication_time\n" +
            "           , d.id AS datex2_id\n" +
            "           , record.validy_status\n" +
            "           , coalesce(record.overall_end_time, TO_DATE('9999', 'yyyy')) overall_end_time\n" +
            "         FROM DATEX2 d\n" +
            "         INNER JOIN datex2_situation situation ON situation.datex2_id = d.id\n" +
            "         INNER JOIN datex2_situation_record record ON record.datex2_situation_id = situation.id\n" +
            "         WHERE d.message_type = :messageType\n"    +
            "       ) disorder\n" +
            "  WHERE rnum = 1\n" +
            "        AND (disorder.validy_status <> 'SUSPENDED'\n" +
            "             AND disorder.overall_end_time > current_timestamp)\n" +
            // Skip old Datex2 messages of HÄTI system
            "        AND disorder.publication_time > TO_DATE('201611', 'yyyymm')\n" +
            ")\n" +
            "order by d.publication_time, d.id",
            nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findAllActive(@Param("messageType") final String messageType);

    @Query(value =
        "            SELECT d.*\n" +
        "            FROM datex2 d\n" +
        "            WHERE d.publication_time >= TRUNC(TO_DATE('1.' || :month || '.' || :year, 'DD.MM.YYYY'), 'MONTH')\n" +
        "              AND d.publication_time < LAST_DAY(TO_DATE('1.' || :month || '.' || :year, 'DD.MM.YYYY')) + 1\n" +
        "              AND d.message_type = :messageType\n", nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findHistory(@Param("messageType") final String messageType,
                             @Param("year") final int year,
                             @Param("month") final int month);

    @Query(name = "find_history_by_situation_id")
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findHistoryBySituationId(@Param("messageType") final String messageType,
                                          @Param("situationId") final String situationId,
                                          @Param("year") final int year,
                                          @Param("month") final int month);

    @Query(name = "find_by_situation_id_and_message_type")
    List<Datex2> findBySituationIdAndMessageType(@Param("situationId") final String situationId, @Param("messageType") final String
        messageType);

    @Query("SELECT CASE WHEN count(situation) > 0 THEN TRUE ELSE FALSE END\n" +
           "FROM Datex2Situation situation\n" +
           "WHERE situation.situationId = :situationId")
    boolean existsWithSituationId(@Param("situationId") final String situationId);

    @Query(name = "list_roadworks_situation_version_times")
    List<Object[]> listRoadworkSituationVersionTimes();

    @Query(value = "delete from datex2 where message_type = 'ROADWORK'", nativeQuery = true)
    @Modifying
    void removeAllRoadworks();
}
