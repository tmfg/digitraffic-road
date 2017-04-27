package fi.livi.digitraffic.tie.metadata.dao;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.CameraPreset;

@Repository
public interface CameraPresetRepository extends JpaRepository<CameraPreset, Long> {
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    @EntityGraph(attributePaths = { "roadStation", "roadStation.roadAddress", "nearestWeatherStation" }, type = EntityGraph.EntityGraphType.LOAD)
    List<CameraPreset> findByPublishableIsTrueAndRoadStationPublishableIsTrueOrderByPresetId();

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<CameraPreset> findByCameraIdAndPublishableIsTrueAndRoadStationPublishableIsTrueOrderByPresetId(final String cameraId);

    @Query(value =
            "SELECT CP.*\n" +
            "FROM CAMERA_PRESET CP\n" +
            "WHERE NOT EXISTS (\n" +
            "  SELECT NULL\n" +
            "  FROM ROAD_STATION RS\n" +
            "  WHERE CP.ROAD_STATION_ID = RS.ID\n" +
            "    AND RS.TYPE = 3" +
            ")",
            nativeQuery = true)
    List<CameraPreset> findAllCameraPresetsWithoutRoadStation();

    @Query(value =
            "SELECT MAX(CP.PIC_LAST_MODIFIED) UPDATED\n" +
            "FROM CAMERA_PRESET CP",
            nativeQuery = true)
    LocalDateTime getLatestMeasurementTime();

    CameraPreset findCameraPresetByPresetId(final String presetId);

    List<CameraPreset> findByPublishableIsTrueAndLotjuIdIn(final Collection<Long> presetIds);

    List<CameraPreset> findByCameraLotjuIdIsNullOrLotjuIdIsNull();

    @Query(value =
           "SELECT CP.PRESET_ID\n" +
           "FROM CAMERA_PRESET CP\n" +
           "WHERE PUBLISHABLE = 0",
           nativeQuery = true)
    List<String> findAllNotPublishableCameraPresetsPresetIds();
}
