package fi.livi.digitraffic.tie.metadata.dao;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.CameraPresetHistory;
import fi.livi.digitraffic.tie.metadata.model.CameraPresetHistoryPK;

@Repository
public interface CameraPresetHistoryRepository extends JpaRepository<CameraPresetHistory, CameraPresetHistoryPK> {

    Optional<CameraPresetHistory> findByIdPresetIdAndIdVersionId(final String presetId, final String versionId);

    @Query(value = "SELECT DISTINCT ON (preset_id)\n" +
                   "history.*\n" +
                   "FROM camera_preset_history history\n" +
                   "where history.preset_id = :presetId\n" +
                   "ORDER BY history.preset_id, history.last_modified DESC",
           nativeQuery = true)
    Optional<CameraPresetHistory> findLatestByPresetId(final String presetId);

    @Query(value = "SELECT DISTINCT ON (preset_id)\n" +
                   "history.*\n" +
                   "FROM camera_preset_history history\n" +
                   "where history.publishable = true\n" +
                   "  AND history.preset_id = :presetId\n" +
                   "  AND history.last_modified <= :atTime\n" +
                   "  AND history.last_modified >= :maxTime\n" +
                   "ORDER BY history.preset_id, history.last_modified DESC",
           nativeQuery = true)
    Optional<CameraPresetHistory> findLatestPublishableByPresetIdAndTime(final String presetId, Instant atTime, Instant maxTime);

    @Query(value = "SELECT history.*\n" +
                   "FROM camera_preset_history history\n" +
                   "where history.publishable = true\n" +
                   "  AND history.preset_id = :presetId\n" +
                   "  AND history.last_modified >= :maxTime\n" +
                   "ORDER BY history.preset_id, history.last_modified DESC",
                   nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<CameraPresetHistory> findAllPublishableByPresetIdOrderByLastModifiedDesc(final String presetId, Instant maxTime);

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<CameraPresetHistory> findByIdPresetIdOrderByLastModifiedAsc(final String presetId);

    int deleteByIdPresetId(final String presetId);

    boolean existsByIdPresetId(final String presetId);

    @Query(value =
        "UPDATE camera_preset_history history\n" +
        "SET publishable = :isPublic\n" +
        "WHERE history.publishable <> :isPublic\n" +
        "  AND history.last_modified >= :startTime\n" +
        "  AND history.preset_id like (:cameraId || '%' )",
        nativeQuery = true)
    void updatePresetHistoryPublicityForCameraId(final String cameraId, final boolean isPublic, final Instant startTime);
}
