package fi.livi.digitraffic.tie.dao.v1;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.v1.camera.PresetHistoryChangeDto;
import fi.livi.digitraffic.tie.dto.v1.camera.PresetHistoryPresenceDto;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPresetHistory;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPresetHistoryPK;

@Repository
public interface CameraPresetHistoryRepository extends JpaRepository<CameraPresetHistory, CameraPresetHistoryPK> {

    Optional<CameraPresetHistory> findByIdPresetIdAndIdVersionId(final String presetId, final String versionId);

    @Query(value = "SELECT DISTINCT ON (preset_id)\n" +
                   "history.*\n" +
                   "FROM camera_preset_history history\n" +
                   "WHERE history.preset_id = :presetId\n" +
                   "ORDER BY history.preset_id, history.last_modified DESC",
           nativeQuery = true)
    Optional<CameraPresetHistory> findLatestByPresetId(final String presetId);

    @Query(value =  "SELECT DISTINCT ON (preset_id)\n" +
                    "history.*\n" +
                    "FROM camera_preset_history history\n" +
                    "WHERE history.publishable = true\n" +
                    "  AND history.last_modified <= :atTime\n" +
                    "  AND history.last_modified >= :oldestTimeLimit\n" +
                    "  AND (history.camera_id in (:cameraIds) OR\n" +
                    "       history.preset_id in (:presetIds))    " +
                    "ORDER BY history.preset_id, history.last_modified DESC",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="10000"))
    List<CameraPresetHistory> findLatestPublishableByCameraAndPresetIdsAndTimeOrderByPresetIdAndLastModifiedDesc(
            final List<String> cameraIds, final List<String> presetIds,
            final Instant atTime, final Instant oldestTimeLimit);

    @Query(value =  "SELECT history.*\n" +
                    "FROM camera_preset_history history\n" +
                    "WHERE history.publishable = true\n" +
                    "  AND history.last_modified >= :oldestTimeLimit\n" +
                    "  AND (history.camera_id in (:cameraIds) OR\n" +
                    "       history.preset_id in (:presetIds))    " +
                    "ORDER BY history.preset_id, history.last_modified DESC",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="10000"))
    List<CameraPresetHistory> findAllPublishableByCameraAndPresetIdsOrderByPresetIdAndLastModifiedDesc(
        final List<String> cameraIds, final List<String> presetIds, final Instant oldestTimeLimit);

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<CameraPresetHistory> findByIdPresetIdOrderByLastModifiedAsc(final String presetId);

    @Modifying
    int deleteByIdPresetId(final String presetId);

    boolean existsByIdPresetId(final String presetId);

    boolean existsByCameraId(final String cameraId);

    @Modifying
    @Query(value = "UPDATE camera_preset_history history\n" +
                   "SET publishable = (:isPublic AND preset_public)\n" +
                   "WHERE history.publishable <> :isPublic\n" +
                   "  AND history.last_modified >= :fromTime\n" +
                   "  AND history.camera_id = :cameraId",
           nativeQuery = true)
    void updatePresetHistoryPublicityForCameraId(final String cameraId, final boolean isPublic, final Instant fromTime);

    @Query(value = "SELECT h.camera_id as cameraId, " +
                   "       h.preset_id as presetId," +
                   "       bool_or(h.publishable AND h.last_modified >= :fromTime AND h.last_modified <= :toTime) as historyPresent\n"+
                   "FROM camera_preset_history h\n"+
                   "WHERE h.last_modified >= :oldestTimeLimit\n" +
                   "GROUP BY h.camera_id, h.preset_id\n"+
                   "ORDER BY h.camera_id, h.preset_id",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<PresetHistoryPresenceDto> findCameraPresetHistoryPresenceByTime(final Instant fromTime, final Instant toTime, final Instant oldestTimeLimit);

    @Query(value = "SELECT h.camera_id as cameraId, " +
                   "       h.preset_id as presetId," +
                   "       bool_or(h.publishable AND h.last_modified >= :fromTime AND h.last_modified <= :toTime) as historyPresent\n"+
                   "FROM camera_preset_history h\n"+
                   "WHERE h.last_modified >= :oldestTimeLimit\n" +
                   "  AND h.preset_id = :presetId\n" +
                   "GROUP BY h.camera_id, h.preset_id\n"+
                   "ORDER BY h.camera_id, h.preset_id",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<PresetHistoryPresenceDto> findCameraPresetHistoryPresenceByPresetIdAndTime(final String presetId, final Instant fromTime, final Instant toTime, final Instant oldestTimeLimit);

    @Query(value = "SELECT h.camera_id as cameraId, " +
                   "       h.preset_id as presetId," +
                   "       bool_or(h.publishable AND h.last_modified >= :fromTime AND h.last_modified <= :toTime) as historyPresent\n"+
                   "FROM camera_preset_history h\n"+
                   "WHERE h.last_modified >= :oldestTimeLimit\n" +
                   "  AND h.camera_id = :cameraId\n" +
                   "GROUP BY h.camera_id, h.preset_id\n"+
                   "ORDER BY h.camera_id, h.preset_id",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<PresetHistoryPresenceDto> findCameraPresetHistoryPresenceByCameraIdAndTime(final String cameraId, final Instant fromTime, final Instant toTime, final Instant oldestTimeLimit);

    @Query(value =
        "select post_history.preset_id presetId\n" +
        "     , post_history.camera_id cameraId\n" +
        "     , pre_history.publishable publishableFrom\n" +
        "     , post_history.publishable publishableTo\n" +
        "     , post_history.last_modified lastModified\n" +
        "     , post_history.modified\n" +
        "FROM camera_preset_history AS post_history, camera_preset_history AS pre_history\n" +
        "WHERE post_history.modified > :fromTime\n" +
        "  and pre_history.preset_id = post_history.preset_id\n" +
        "  and pre_history.preset_seq = post_history.preset_seq_prev\n" +
        "  and pre_history.publishable <> post_history.publishable\n" +
        "  AND ( ( :cameraIds IS NULL AND :presetIds IS NULL ) " +
        "    OR ( post_history.camera_id in ( :cameraIds ) OR post_history.preset_id in ( :presetIds ) ) )\n" +
        "ORDER BY post_history.preset_id, post_history.last_modified",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<PresetHistoryChangeDto> findCameraPresetHistoryChangesAfter(final Instant fromTime, final List<String> cameraIds, final List<String> presetIds);

    @Query(value =
               "select max(h.modified)\n" +
               "FROM camera_preset_history h",
           nativeQuery = true)
    Instant getLatestChangesTime();

    @Modifying
    @Query(value = "delete FROM camera_preset_history h WHERE h.last_modified < now() - :hours * interval '1 hour'", nativeQuery = true)
    void deleteOlderThanHours(final int hours);


    @Query(value =
               "select post_history.preset_id presetId\n" +
               "     , post_history.camera_id cameraId\n" +
               "     , pre_history.publishable publishableFrom\n" +
               "     , post_history.publishable publishableTo\n" +
               "     , post_history.last_modified lastModified\n" +
               "     , post_history.modified\n" +
               "FROM camera_preset_history AS post_history, camera_preset_history AS pre_history\n" +
               "WHERE post_history.modified > :fromTime\n" +
               "  and pre_history.preset_id = post_history.preset_id\n" +
               "  and pre_history.preset_seq = post_history.preset_seq_prev\n" +
               "  and pre_history.publishable <> post_history.publishable\n" +
               "ORDER BY post_history.camera_id, post_history.modified, post_history.preset_id",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<PresetHistoryChangeDto> findCameraPresetHistoryChangesAfter(final Instant fromTime);

    @Query(value =
               "SELECT history.*\n" +
               "FROM camera_preset_history history\n" +
               "WHERE history.publishable = true\n" +
               "  AND history.last_modified >= :oldestTimeLimit\n" +
               "  AND (history.camera_id =:cameraId OR :cameraId IS NULL)\n" +
               "ORDER BY history.preset_id, history.last_modified",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="10000"))
    List<CameraPresetHistory> findAllPublishableHistoryByCameraIdOrNullOrderByPresetIdAndLastModified(final String cameraId, final Instant oldestTimeLimit);
    @Query(value =
               "SELECT history.*\n" +
               "FROM camera_preset_history history\n" +
               "WHERE history.publishable = true\n" +
               "  AND history.last_modified >= :oldestTimeLimit\n" +
               "  AND history.preset_id = :presetId    " +
               "ORDER BY history.preset_id, history.last_modified",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="10000"))
    List<CameraPresetHistory> findAllPublishableHistoryByPresetIdOrderByPresetIdAndLastModified(final String presetId, final Instant oldestTimeLimit);
}