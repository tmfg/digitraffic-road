package fi.livi.digitraffic.tie.metadata.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.CameraPreset;

@Repository
public interface CameraPresetRepository extends JpaRepository<CameraPreset, Long> {

    @EntityGraph("camera")
    @Override
    List<CameraPreset> findAll();

    List<CameraPreset> findByObsoleteDateIsNullAndRoadStationObsoleteDateIsNullAndRoadStationIsPublicTrueOrderByPresetId();

    List<CameraPreset> findByCameraIdAndObsoleteDateIsNullAndRoadStationObsoleteDateIsNullAndRoadStationIsPublicTrueOrderByPresetId(final String cameraId);


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
    List<CameraPreset> finAllCameraPresetsWithOutRoadStation();


    @Query(value =
            "SELECT MAX(CP.PIC_LAST_MODIFIED) UPDATED\n" +
            "FROM CAMERA_PRESET CP",
            nativeQuery = true)
    LocalDateTime getLatestMeasurementTime();
}
