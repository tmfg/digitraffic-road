package fi.livi.digitraffic.tie.dao.weathercam;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.model.weathercam.CameraPreset;
import fi.livi.digitraffic.tie.model.weathercam.WeatherStationPreset;
import jakarta.persistence.QueryHint;

@Repository
public interface CameraPresetRepository extends JpaRepository<CameraPreset, Long> {

    @Override
    @NonNull
    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize",
                           value = "1000"))
    @EntityGraph(attributePaths = { "roadStation", "roadStation.roadAddress", "nearestWeatherStation",
                                    "nearestWeatherStation.roadStation",
                                    "nearestWeatherStation.roadStation.roadAddress" },
                 type = EntityGraph.EntityGraphType.LOAD)
    List<CameraPreset> findAll();

    /**
     * Selects public presets by publicity status of preset and station:
     * CameraPreset.publishable && RoadStation.isPublicNow()
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
    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize",
                           value = "1000"))
    @EntityGraph(attributePaths = { "roadStation", "roadStation.roadAddress", "nearestWeatherStation" },
                 type = EntityGraph.EntityGraphType.LOAD)
    List<CameraPreset> findByPublishableIsTrueAndRoadStationPublishableNowIsTrueOrderByPresetId(final String cameraId);

    /**
     * Only difference to {@link #findByPublishableIsTrueAndRoadStationPublishableNowIsTrueOrderByPresetId(String)} ()}
     * is that this query filters result by given cameraId.
     *
     * @param cameraId camera id which presets to fetch.
     * @return Publishable presets for given camera id
     * @see CameraPresetRepository#findByPublishableIsTrueAndRoadStationPublishableNowIsTrueOrderByPresetId(String)
     */
    @Query(value = """
            SELECT cp.preset_id as PresetId
                 , cp.pic_last_modified as PictureLastModified
                 , cp.camera_id as CameraId
                 , cp.pic_last_modified_db as PicLastModifiedDb
            FROM camera_preset cp, road_station rs
            WHERE cp.road_station_id = rs.id
              AND cp.publishable = true
              AND cp.camera_id=:cameraId
              AND rs.obsolete_date IS NULL
              AND rs.collection_status <> 'REMOVED_PERMANENTLY'
              AND (
                (rs.is_public = true AND COALESCE(rs.publicity_start_time, CURRENT_TIMESTAMP) <= CURRENT_TIMESTAMP)
                OR (rs.is_public_previous = true AND COALESCE(rs.publicity_start_time, CURRENT_TIMESTAMP) > CURRENT_TIMESTAMP)
              )
              ORDER BY cp.preset_id""",
           nativeQuery = true)
    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize",
                           value = "20"))
    @Transactional(readOnly = true)
    List<WeatherStationPreset> findByCameraIdAndPublishableIsTrueAndRoadStationPublishableNowIsTrueOrderByPresetId(
            final String cameraId);

    List<CameraPreset> findByRoadStation_LotjuId(final Long cameraLotjuId);

    @Modifying(clearAutomatically = true)
    @Query(value = """
            UPDATE ROAD_STATION
            SET OBSOLETE_DATE = current_timestamp
            WHERE TYPE = 'CAMERA_STATION'
              AND OBSOLETE_DATE IS NULL
              AND NOT EXISTS(
                SELECT NULL
                FROM CAMERA_PRESET CP
                WHERE CP.PUBLISHABLE = true
                  AND CP.ROAD_STATION_ID = ROAD_STATION.ID
            )""",
           nativeQuery = true)
    int obsoleteCameraRoadStationsWithoutPublishablePresets();

    // NULLS FIRST is the default for DESC order so this will get the one with null
    // obsolete_date first if any such exists
    @EntityGraph(attributePaths = "roadStation")
    CameraPreset findFirstByLotjuIdOrderByObsoleteDateDesc(final long presetLotjuId);

    @EntityGraph(attributePaths = "roadStation")
    CameraPreset findByPresetId(String presetId);

    @Query(value = """
            select distinct cp.camera_id as cameraId, wrs.natural_id as nearestWeatherStationNaturalId
            from camera_preset cp
            inner join weather_station ws on cp.nearest_rd_weather_station_id = ws.id
            inner join road_station wrs on ws.road_station_id = wrs.id AND wrs.type = 'WEATHER_STATION'
            inner join road_station crs on cp.road_station_id = crs.id AND crs.type = 'CAMERA_STATION'
            where cp.publishable = true
              and wrs.publishable = true
              and crs.publishable = true""",
           nativeQuery = true)
    List<WeathercamNearestWeatherStationV1> findAllPublishableNearestWeatherStations();

    @Query(value = """
            select wrs.natural_id
            from camera_preset cp
            inner join weather_station ws on cp.nearest_rd_weather_station_id = ws.id
            inner join road_station wrs on ws.road_station_id = wrs.id AND wrs.type = 'WEATHER_STATION'
            inner join road_station crs on cp.road_station_id = crs.id AND crs.type = 'CAMERA_STATION'
            where cp.publishable = true
              and wrs.publishable = true
              and crs.publishable = true
              and cp.camera_id = :cameraId
            limit 1""",
           nativeQuery = true)
    Long findNearestWeatherStationNaturalIdByCameraNatualId(final String cameraId);

    interface WeathercamNearestWeatherStationV1 {
        String getCameraId();

        Long getNearestWeatherStationNaturalId();
    }
}
