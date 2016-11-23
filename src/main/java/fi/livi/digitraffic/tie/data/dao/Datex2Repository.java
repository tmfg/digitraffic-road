package fi.livi.digitraffic.tie.data.dao;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.data.model.Datex2;

@Repository
public interface Datex2Repository extends JpaRepository<Datex2, Long> {


    @Query(value =
            "select max(datex2.import_date) as updated\n" +
            "from datex2",
            nativeQuery = true)
    LocalDateTime getLatestMeasurementTime();

    @Query(value =
            "SELECT d.*\n" +
            "FROM datex2 d\n" +
            "WHERE d.id IN (\n" +
            "  SELECT datex2_id\n" +
            "  FROM (\n" +
            "    SELECT ROW_NUMBER() OVER (PARTITION BY record.situation_record_id ORDER BY d.publication_time DESC) AS rnum\n" +
            "         , d.publication_time\n" +
            "         , d.id AS datex2_id\n" +
            "         , record.validy_status \n" +
            "         , nvl(record.overall_end_time, TO_DATE('9999', 'yyyy')) overall_end_time\n" +
            "         , record.id AS record_id\n" +
            "         , i18n.value\n" +
            "    FROM DATEX2 d\n" +
            "    INNER JOIN datex2_situation situation ON situation.datex2_id = d.id\n" +
            "    INNER JOIN datex2_situation_record record ON record.datex2_situation_id = situation.id\n" +
            "    INNER JOIN SITUATION_RECORD_COMMENT_I18N i18n on i18n.DATEX2_SITUATION_RECORD_ID = record.id and i18n.lang = 'fi'\n" +
            "  ) disorder\n" +
            "  WHERE rnum = 1\n" +
            "    AND (disorder.validy_status = 'ACTIVE'\n" +
            "         AND disorder.overall_end_time > sysdate)\n" +
            // Skipataan vanhat hätin ilmoitukset
            "    AND disorder.publication_time > TO_DATE('201611', 'yyyymm')\n" +
            ")\n" +
            "order by d.publication_time",
            nativeQuery = true)
    List<Datex2> findAllActive();

    @Query(value =
            "SELECT d.*\n" +
            "FROM datex2 d\n" +
            "WHERE d.publication_time >= TRUNC(TO_DATE('1.' || :month || '.' || :year, 'DD.MM.YYYY'), 'MONTH')\n" +
            "  AND d.publication_time < LAST_DAY(TO_DATE('1.' || :month || '.' || :year, 'DD.MM.YYYY')) + 1",
            nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findHistory(@Param("year") final int year,
                             @Param("month") final int month);

    @Query(value =
            "SELECT d.*\n" +
            "FROM datex2 d\n" +
            "WHERE EXISTS (\n" +
            "  SELECT NULL\n" +
            "  FROM datex2_situation situation\n" +
            "  INNER JOIN datex2_situation_record record ON record.datex2_situation_id = situation.id\n" +
            "  WHERE situation.datex2_id = d.id\n" +
            "    AND situation.situation_id = :situationId\n" +
            "    AND d.publication_time >= TRUNC(TO_DATE('1.' || :month || '.' || :year, 'DD.MM.YYYY'), 'MONTH')\n" +
            "    AND d.publication_time < LAST_DAY(TO_DATE('1.' || :month || '.' || :year, 'DD.MM.YYYY')) + 1\n" +
            ")",
            nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findHistory(@Param("situationId") final String situationId,
                             @Param("year") final int year,
                             @Param("month") final int month);

    @Query(value =
           "SELECT d.*\n" +
           "FROM datex2 d\n" +
           "WHERE d.id in (\n" +
           "  SELECT situation.datex2_id\n" +
           "  FROM datex2_situation situation\n" +
           "  WHERE situation.situation_id = :situationId\n" +
           ")",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="100"))
    List<Datex2> findBySituationId(@Param("situationId") final String situationId);
}
