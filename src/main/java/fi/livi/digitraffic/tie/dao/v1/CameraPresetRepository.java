package fi.livi.digitraffic.tie.dao.v1;

import java.util.Collection;
import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.v1.NearestRoadStation;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPreset;

@Repository
public interface CameraPresetRepository extends JpaRepository<CameraPreset, Long> {

    @Override
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    @EntityGraph(attributePaths = { "roadStation", "roadStation.roadAddress", "nearestWeatherStation", "nearestWeatherStation.roadStation", "nearestWeatherStation.roadStation.roadAddress" }, type = EntityGraph.EntityGraphType.LOAD)
    List<CameraPreset> findAll();

    /**
     * Selects public presets by publicity status of preset and station:
     * CameraPreset.publishable && RoadStation.isPublicNow()
     *
     * RoadStation.isPublicNow() is resolved by checking publicityStartTime:
     * If publicityStartTime is effective now (null or in the past) then isPublic is used.
     * If publicityStartTime is in the future, then isPublicPrevious is used (as isPublic os not effective yet).
     *
     * @return All publishable presets
     */
    @Query(value =
               "SELECT cp, rs, ra, ws " +
               "FROM CameraPreset cp " +
               "inner join cp.roadStation as rs " +
               "left outer join rs.roadAddress as ra " +
               "left outer join cp.nearestWeatherStation as ws " +
               "where cp.publishable = true " +
               "  AND rs.obsoleteDate IS NULL " +
               "  AND rs.collectionStatus <> 'REMOVED_PERMANENTLY' " +
               "  AND ( " +
               "        (rs.isPublic = true AND COALESCE(rs.publicityStartTime, CURRENT_TIMESTAMP) <= CURRENT_TIMESTAMP) " +
               "        OR (rs.isPublicPrevious = true AND COALESCE(rs.publicityStartTime, CURRENT_TIMESTAMP) > CURRENT_TIMESTAMP) " +
               "  ) " +
               "  AND (:cameraId IS NULL OR cp.cameraId = :cameraId) " +
               "ORDER BY cp.presetId")
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    @EntityGraph(attributePaths = { "roadStation", "roadStation.roadAddress", "nearestWeatherStation" }, type = EntityGraph.EntityGraphType.LOAD)
    List<CameraPreset> findByPublishableIsTrueAndRoadStationPublishableNowIsTrueOrderByPresetId(final String cameraId);

    /**
     * Only difference to {@link #findByPublishableIsTrueAndRoadStationPublishableNowIsTrueOrderByPresetId(String)} ()}
     * is that this query filters result by given cameraId.

     * @param cameraId camera id which presets to fetch.
     * @return Publishable presets for given camera id
     *
     * @see {@link CameraPresetRepository#findByPublishableIsTrueAndRoadStationPublishableNowIsTrueOrderByPresetId(String)}
     */
    @Query(value =
               "SELECT cp, rs, ra, ws " +
               "FROM CameraPreset cp " +
               "inner join cp.roadStation as rs " +
               "left outer join rs.roadAddress as ra " +
               "left outer join cp.nearestWeatherStation as ws " +
               "where cp.publishable = true " +
               "  AND cp.cameraId=:cameraId " +
               "  AND rs.obsoleteDate IS NULL " +
               "  AND rs.collectionStatus <> 'REMOVED_PERMANENTLY' " +
               "  AND ( " +
               "        (rs.isPublic = true AND COALESCE(rs.publicityStartTime, CURRENT_TIMESTAMP) <= CURRENT_TIMESTAMP) " +
               "        OR (rs.isPublicPrevious = true AND COALESCE(rs.publicityStartTime, CURRENT_TIMESTAMP) > CURRENT_TIMESTAMP) " +
               "  ) " +
               "ORDER BY cp.presetId")
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="20"))
    @EntityGraph(attributePaths = { "roadStation", "roadStation.roadAddress", "nearestWeatherStation" }, type = EntityGraph.EntityGraphType.LOAD)
    List<CameraPreset> findByCameraIdAndPublishableIsTrueAndRoadStationPublishableNowIsTrueOrderByPresetId(final String cameraId);

    @Query(value =
           "SELECT CP.PRESET_ID\n" +
           "FROM CAMERA_PRESET CP\n" +
           "WHERE PUBLISHABLE = false",
           nativeQuery = true)
    List<String> findAllNotPublishableCameraPresetsPresetIds();

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    @Query(value = "select rs.natural_id nearestNaturalId, ws.id weatherStationId\n" +
        "from weather_station ws, road_station rs\n" +
        "where ws.id in (:wsIdList)\n" +
        "and ws.road_station_id = rs.id", nativeQuery = true)
    List<NearestRoadStation> findAllRoadStationNaturalIds(@Param("wsIdList") final Collection<Long> wsIdList);

    List<CameraPreset> findByRoadStation_LotjuId(final Long cameraLotjuId);

    @Modifying(clearAutomatically = true)
    @Query(value =
               "UPDATE ROAD_STATION\n" +
               "SET OBSOLETE_DATE = current_timestamp\n" +
               "WHERE ROAD_STATION_TYPE = 'CAMERA_STATION'\n" +
               "  AND OBSOLETE_DATE IS NULL\n" +
               "  AND NOT EXISTS(\n" +
               "    SELECT NULL\n" +
               "    FROM CAMERA_PRESET CP\n" +
               "    WHERE CP.PUBLISHABLE = true\n" +
               "      AND CP.ROAD_STATION_ID = ROAD_STATION.ID\n" +
               ")", nativeQuery = true)
    int obsoleteCameraRoadStationsWithoutPublishablePresets();

    // NULLS FIRST is the default for DESC order so this will get the one with null
    // obsolete_date first if any such exists
    @EntityGraph(attributePaths = "roadStation")
    CameraPreset findFirstByLotjuIdOrderByObsoleteDateDesc(final long presetLotjuId);

    @EntityGraph(attributePaths = "roadStation")
    CameraPreset findByPresetId(String presetId);

    @Query(value =
        "select distinct cp.camera_id as cameraId, wrs.natural_id as nearestWeatherStationNaturalId\n" +
            "from camera_preset cp\n" +
            "inner join weather_station ws on cp.nearest_rd_weather_station_id = ws.id\n" +
            "inner join road_station wrs on ws.road_station_id = wrs.id AND wrs.type = 2 -- WeatherStation\n" +
            "inner join road_station crs on cp.road_station_id = crs.id AND crs.type = 3 -- CameraStation\n" +
            "where cp.publishable = true\n" +
            "  and wrs.publishable = true\n" +
            "  and crs.publishable = true", nativeQuery = true)
    List<WeathercamNearestWeatherStationV1> findAllPublishableNearestWeatherStations();

    @Query(value =
        "select wrs.natural_id\n" +
        "from camera_preset cp\n" +
        "inner join weather_station ws on cp.nearest_rd_weather_station_id = ws.id\n" +
        "inner join road_station wrs on ws.road_station_id = wrs.id AND wrs.type = 2 -- WeatherStation\n" +
        "inner join road_station crs on cp.road_station_id = crs.id AND crs.type = 3 -- CameraStation\n" +
        "where cp.publishable = true\n" +
        "  and wrs.publishable = true\n" +
        "  and crs.publishable = true\n" +
        "  and cp.camera_id = :cameraId\n" +
        "limit 1", nativeQuery = true)
    Long getNearestWeatherStationNaturalIdByCameraNatualId(final String cameraId);

    interface WeathercamNearestWeatherStationV1 {
        String getCameraId();

        Long getNearestWeatherStationNaturalId();
    }
}
