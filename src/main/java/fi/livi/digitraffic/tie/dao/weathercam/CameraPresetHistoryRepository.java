package fi.livi.digitraffic.tie.dao.weathercam;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.v1.camera.PresetHistoryChangeDto;
import fi.livi.digitraffic.tie.dto.v1.camera.PresetHistoryPresenceDto;
import fi.livi.digitraffic.tie.model.weathercam.CameraPresetHistory;
import fi.livi.digitraffic.tie.model.weathercam.CameraPresetHistoryPK;
import jakarta.persistence.QueryHint;

@Repository
public interface CameraPresetHistoryRepository extends JpaRepository<CameraPresetHistory, CameraPresetHistoryPK> {

    Optional<CameraPresetHistory> findByIdPresetIdAndIdVersionId(final String presetId, final String versionId);

    @Query(value = """
            SELECT DISTINCT ON (preset_id)
              history.created,
              history.modified,
              history.preset_id,
              history.version_id,
              history.camera_preset_id,
              history.last_modified,
              history.publishable,
              history.size,
              history.camera_id,
              history.preset_public
            FROM camera_preset_history history
            WHERE history.preset_id = :presetId
            ORDER BY history.preset_id, history.last_modified DESC""",
           nativeQuery = true)
    Optional<CameraPresetHistory> findLatestByPresetId(final String presetId);

    @Query(value = """
            SELECT DISTINCT ON (preset_id)
              history.created,
              history.modified,
              history.preset_id,
              history.version_id,
              history.camera_preset_id,
              history.last_modified,
              history.publishable,
              history.size,
              history.camera_id,
              history.preset_public
            FROM camera_preset_history history
            WHERE history.publishable = true
              AND history.last_modified <= :atTime
              AND history.last_modified >= :oldestTimeLimit
              AND (history.camera_id in (:cameraIds) OR
                   history.preset_id in (:presetIds))    ORDER BY history.preset_id, history.last_modified DESC""",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="10000"))
    List<CameraPresetHistory> findLatestPublishableByCameraAndPresetIdsAndTimeOrderByPresetIdAndLastModifiedDesc(
            final List<String> cameraIds, final List<String> presetIds,
            final Instant atTime, final Instant oldestTimeLimit);

    @Query(value = """
            SELECT
              history.created,
              history.modified,
              history.preset_id,
              history.version_id,
              history.camera_preset_id,
              history.last_modified,
              history.publishable,
              history.size,
              history.camera_id,
              history.preset_public
            FROM camera_preset_history history
            WHERE history.publishable = true
              AND history.last_modified >= :oldestTimeLimit
              AND (history.camera_id in (:cameraIds) OR
                   history.preset_id in (:presetIds))    ORDER BY history.preset_id, history.last_modified DESC""",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="10000"))
    List<CameraPresetHistory> findAllPublishableByCameraAndPresetIdsOrderByPresetIdAndLastModifiedDesc(
        final List<String> cameraIds, final List<String> presetIds, final Instant oldestTimeLimit);

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<CameraPresetHistory> findByIdPresetIdOrderByLastModifiedAsc(final String presetId);

    @Modifying
    // Spring Boot 3.5.6 produces an error with the default implementation of deleteByIdPresetId in case there is more than row with the same presetId - maybe a bug?
    @Query(value = """
        DELETE FROM camera_preset_history history WHERE history.preset_id = :presetId""", nativeQuery = true)
    int deleteAllByIdPresetId(final String presetId);

    boolean existsByIdPresetId(final String presetId);

    boolean existsByCameraId(final String cameraId);

    @Modifying
    @Query(value = """
            UPDATE camera_preset_history history
            SET publishable = (:isPublic AND preset_public)
            WHERE history.publishable <> :isPublic
              AND history.last_modified >= :fromTime
              AND history.camera_id = :cameraId""",
           nativeQuery = true)
    void updatePresetHistoryPublicityForCameraId(final String cameraId, final boolean isPublic, final Instant fromTime);

    @Query(value = """
            SELECT h.camera_id as cameraId,
                   h.preset_id as presetId,
                   bool_or(h.publishable AND h.last_modified >= :fromTime AND h.last_modified <= :toTime) as historyPresent
            FROM camera_preset_history h
            WHERE h.last_modified >= :oldestTimeLimit
            GROUP BY h.camera_id, h.preset_id
            ORDER BY h.camera_id, h.preset_id""", nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<PresetHistoryPresenceDto> findCameraPresetHistoryPresenceByTime(final Instant fromTime, final Instant toTime, final Instant oldestTimeLimit);

    @Query(value = """
                   SELECT h.camera_id as cameraId
                        , h.preset_id as presetId
                        , bool_or(h.publishable AND h.last_modified >= :fromTime AND h.last_modified <= :toTime) as historyPresent
                   FROM camera_preset_history h
                   WHERE h.last_modified >= :oldestTimeLimit
                     AND h.preset_id = :presetId
                   GROUP BY h.camera_id, h.preset_id
                   ORDER BY h.camera_id, h.preset_id""",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<PresetHistoryPresenceDto> findCameraPresetHistoryPresenceByPresetIdAndTime(final String presetId, final Instant fromTime, final Instant toTime, final Instant oldestTimeLimit);

    @Query(value = """
            SELECT h.camera_id as cameraId
                 , h.preset_id as presetId,       bool_or(h.publishable AND h.last_modified >= :fromTime AND h.last_modified <= :toTime) as historyPresent
            FROM camera_preset_history h
            WHERE h.last_modified >= :oldestTimeLimit
              AND h.camera_id = :cameraId
            GROUP BY h.camera_id, h.preset_id
            ORDER BY h.camera_id, h.preset_id""",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<PresetHistoryPresenceDto> findCameraPresetHistoryPresenceByCameraIdAndTime(final String cameraId, final Instant fromTime, final Instant toTime, final Instant oldestTimeLimit);

    @Query(value ="""
            SELECT post_history.preset_id presetId
                 , post_history.camera_id cameraId
                 , pre_history.publishable publishableFrom
                 , post_history.publishable publishableTo
                 , post_history.last_modified lastModified
                 , post_history.modified
            FROM camera_preset_history AS post_history, camera_preset_history AS pre_history
            WHERE post_history.modified > :fromTime
              AND pre_history.preset_id = post_history.preset_id
              AND pre_history.preset_seq = post_history.preset_seq_prev
              AND pre_history.publishable <> post_history.publishable
              AND ( ( :cameraIds IS NULL AND :presetIds IS NULL )
                    OR ( post_history.camera_id in ( :cameraIds )
                    OR post_history.preset_id in ( :presetIds ) ) )
            ORDER BY post_history.preset_id, post_history.last_modified""", nativeQuery = true)
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


    @Query(value = """
            select post_history.preset_id presetId
                 , post_history.camera_id cameraId
                 , pre_history.publishable publishableFrom
                 , post_history.publishable publishableTo
                 , post_history.last_modified lastModified
                 , post_history.modified
            FROM camera_preset_history AS post_history, camera_preset_history AS pre_history
            WHERE post_history.modified > :fromTime
              and pre_history.preset_id = post_history.preset_id
              and pre_history.preset_seq = post_history.preset_seq_prev
              and pre_history.publishable <> post_history.publishable
            ORDER BY post_history.camera_id, post_history.modified, post_history.preset_id""",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<PresetHistoryChangeDto> findCameraPresetHistoryChangesAfter(final Instant fromTime);

    @Query(value = """
            SELECT
              history.created,
              history.modified,
              history.preset_id,
              history.version_id,
              history.camera_preset_id,
              history.last_modified,
              history.publishable,
              history.size,
              history.camera_id,
              history.preset_public
            FROM camera_preset_history history
            WHERE history.publishable = true
              AND history.last_modified >= :oldestTimeLimit
              AND (history.camera_id =:cameraId OR :cameraId IS NULL)
            ORDER BY history.preset_id, history.last_modified""",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="10000"))
    List<CameraPresetHistory> findAllPublishableHistoryByCameraIdOrNullOrderByPresetIdAndLastModified(final String cameraId, final Instant oldestTimeLimit);
    @Query(value = """
            SELECT
              history.created,
              history.modified,
              history.preset_id,
              history.version_id,
              history.camera_preset_id,
              history.last_modified,
              history.publishable,
              history.size,
              history.camera_id,
              history.preset_public
            FROM camera_preset_history history
            WHERE history.publishable = true
              AND history.last_modified >= :oldestTimeLimit
              AND history.preset_id = :presetId
            ORDER BY history.preset_id, history.last_modified""",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="10000"))
    List<CameraPresetHistory> findAllPublishableHistoryByPresetIdOrderByPresetIdAndLastModified(final String presetId, final Instant oldestTimeLimit);
}
