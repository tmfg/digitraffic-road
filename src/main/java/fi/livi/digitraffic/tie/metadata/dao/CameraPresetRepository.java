package fi.livi.digitraffic.tie.metadata.dao;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.dto.NearestRoadStation;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;

@Repository
public interface CameraPresetRepository extends JpaRepository<CameraPreset, Long> {

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    @EntityGraph(attributePaths = { "roadStation", "roadStation.roadAddress", "nearestWeatherStation", "nearestWeatherStation.roadStation", "nearestWeatherStation.roadStation.roadAddress" }, type = EntityGraph.EntityGraphType.LOAD)
    List<CameraPreset> findAll();

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

    List<CameraPreset> findByRoadStation_LotjuIdIsNullOrLotjuIdIsNull();

    @Query(value =
           "SELECT CP.PRESET_ID\n" +
           "FROM CAMERA_PRESET CP\n" +
           "WHERE PUBLISHABLE = 0",
           nativeQuery = true)
    List<String> findAllNotPublishableCameraPresetsPresetIds();

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    @Query(value = "select rs.natural_id nearest_natural_id, ws.id weather_station_id\n" +
        "from weather_station ws, road_station rs\n" +
        "where ws.id in (:wsIdList)\n" +
        "and ws.road_station_id = rs.id", nativeQuery = true)
    List<NearestRoadStation> findAllRoadStationNaturalIds(@Param("wsIdList") final Collection<Long> wsIdList);

    List<CameraPreset> findByRoadStation_LotjuId(final Long cameraLotjuId);

    @Query(value =
               "SELECT CP.LOTJU_ID\n" +
               "FROM CAMERA_PRESET CP\n" +
               "WHERE CP.LOTJU_ID IS NOT NULL",
           nativeQuery = true)
    Set<Long> findAllCameraPresetsLotjuIds();

    @Modifying
    @Query(value = "UPDATE CAMERA_PRESET SET OBSOLETE_DATE = sysdate WHERE CAMERA_LOTJU_ID = :cameraLotjuId " +
                   "AND LOTJU_ID NOT IN (:presetLotjuIds)", nativeQuery = true)
    int obsoleteMissingCameraPresetsForCamera(@Param("cameraLotjuId") long cameraLotjuId, @Param("presetLotjuIds") List<Long> presetLotjuIds);

    @Modifying
    @Query(value = "UPDATE CAMERA_PRESET SET OBSOLETE_DATE = sysdate WHERE CAMERA_LOTJU_ID = :cameraLotjuId ", nativeQuery = true)
    int obsoleteAllCameraPresetsForCamera(@Param("cameraLotjuId") long cameraLotjuId);

    @Modifying(clearAutomatically = true)
    @Query(value =
               "UPDATE ROAD_STATION RS\n" +
               "SET RS.OBSOLETE = 0\n" +
               "  , RS.OBSOLETE_DATE = NULL\n" +
               "WHERE RS.ROAD_STATION_TYPE = 'CAMERA_STATION'\n" +
               "  AND RS.OBSOLETE_DATE IS NOT NULL\n" +
               "  AND EXISTS(\n" +
               "    SELECT NULL\n" +
               "    FROM CAMERA_PRESET CP\n" +
               "    WHERE CP.PUBLISHABLE = 1\n" +
               "      AND CP.ROAD_STATION_ID = RS.ID\n" +
               ")", nativeQuery = true)
    int nonObsoleteCameraRoadStationsWithPublishablePresets();

    @Modifying(clearAutomatically = true)
    @Query(value =
               "UPDATE ROAD_STATION RS\n" +
               "SET RS.OBSOLETE = 1\n" +
               "  , RS.OBSOLETE_DATE = sysdate\n" +
               "WHERE RS.ROAD_STATION_TYPE = 'CAMERA_STATION'\n" +
               "  AND RS.OBSOLETE_DATE IS NULL\n" +
               "  AND NOT EXISTS(\n" +
               "    SELECT NULL\n" +
               "    FROM CAMERA_PRESET CP\n" +
               "    WHERE CP.PUBLISHABLE = 1\n" +
               "      AND CP.ROAD_STATION_ID = RS.ID\n" +
               ")", nativeQuery = true)
    int obsoleteCameraRoadStationsWithoutPublishablePresets();

    CameraPreset findByPublishableTrueAndLotjuId(long presetLotjuId);
}
