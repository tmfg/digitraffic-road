package fi.livi.digitraffic.tie.metadata.dao;

import java.time.Instant;
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

import fi.livi.digitraffic.tie.metadata.dto.NearestRoadStation;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;

@Repository
public interface CameraPresetRepository extends JpaRepository<CameraPreset, Long> {

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    @EntityGraph(attributePaths = { "roadStation", "roadStation.roadAddress", "nearestWeatherStation", "nearestWeatherStation.roadStation", "nearestWeatherStation.roadStation.roadAddress" }, type = EntityGraph.EntityGraphType.LOAD)
    List<CameraPreset> findAll();

    // TODO DPO-462 Kelikamerakuvien salassapidon toteutus - rewrite to check publicity now, check RoadStation.isPublicNow()
    /*
        public boolean isPublicNow() {
            // If current value is valid now, let's use it
            if (publicityStartTime == null || !publicityStartTime.isAfter(ZonedDateTime.now())) {
                return isPublic;
            }
            return isPublicPrevious;
            }
     */
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    @EntityGraph(attributePaths = { "roadStation", "roadStation.roadAddress", "nearestWeatherStation" }, type = EntityGraph.EntityGraphType.LOAD)
    List<CameraPreset> findByPublishableIsTrueAndRoadStationPublishableIsTrueOrderByPresetId();

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<CameraPreset> findByCameraIdAndPublishableIsTrueAndRoadStationPublishableIsTrueOrderByPresetId(final String cameraId);

    @Query(value =
            "SELECT MAX(CP.PIC_LAST_MODIFIED) UPDATED\n" +
            "FROM CAMERA_PRESET CP",
            nativeQuery = true)
    Instant getLatestMeasurementTime();

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
               "SET OBSOLETE_DATE = NULL\n" +
               "WHERE ROAD_STATION_TYPE = 'CAMERA_STATION'\n" +
               "  AND OBSOLETE_DATE IS NOT NULL\n" +
               "  AND EXISTS(\n" +
               "    SELECT NULL\n" +
               "    FROM CAMERA_PRESET CP\n" +
               "    WHERE CP.PUBLISHABLE = true\n" +
               "      AND CP.ROAD_STATION_ID = ROAD_STATION.ID\n" +
               ")", nativeQuery = true)
    int nonObsoleteCameraRoadStationsWithPublishablePresets();

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

    CameraPreset findByPublishableTrueAndLotjuId(long presetLotjuId);

    @EntityGraph(attributePaths = "roadStation")
    CameraPreset findByLotjuId(final long presetLotjuId);
}
