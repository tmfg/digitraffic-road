package fi.livi.digitraffic.tie.data.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.data.model.Datex2;

@Repository
public interface Datex2Repository extends JpaRepository<Datex2, Long> {
    @Query(name = "find_latest_import_time")
    LocalDateTime findLatestImportTime(@Param("messageType") final String messageType);

    @Query(name = "find_all_active")
    List<Datex2> findAllActive(@Param("messageType") final String messageType);

    @Query(name = "find_history")
    List<Datex2> findHistory(@Param("messageType") final String messageType,
                             @Param("year") final int year,
                             @Param("month") final int month);

    @Query(name = "find_history_by_situation_id")
    List<Datex2> findHistoryBySituationId(@Param("messageType") final String messageType,
                                          @Param("situationId") final String situationId,
                                          @Param("year") final int year,
                                          @Param("month") final int month);

    @Query(name = "find_by_situation_id_and_message_type")
    List<Datex2> findBySituationIdAndMessageType(@Param("situationId") final String situationId, @Param("messageType") final String
        messageType);

    @Query(name = "exists_with_situation_id")
    boolean existsWithSituationId(@Param("situationId") final String situationId);

    @Query(name = "list_roadworks_situation_version_times")
    List<Object[]> listRoadworkSituationVersionTimes();

    @Query(value = "delete from datex2 where message_type = 'ROADWORK'", nativeQuery = true)
    @Modifying
    void removeAllRoadworks();
}
