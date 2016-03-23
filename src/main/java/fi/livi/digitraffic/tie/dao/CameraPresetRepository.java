package fi.livi.digitraffic.tie.dao;

import java.util.List;

import fi.livi.digitraffic.tie.model.CameraPreset;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CameraPresetRepository extends JpaRepository<CameraPreset, Long> {

    @EntityGraph("camera")
    @Override
    List<CameraPreset> findAll();

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
}