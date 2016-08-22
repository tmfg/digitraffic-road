package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;

@Repository
public interface RoadStationRepository extends JpaRepository<RoadStation, Long>{

    List<RoadStation> findByType(RoadStationType type);

    @Query(value =
            "SELECT RS.*\n" +
            "FROM ROAD_STATION RS\n" +
            "WHERE NOT EXISTS (\n" +
            "  SELECT NULL\n" +
            "  FROM WEATHER_STATION RWS\n" +
            "  WHERE RWS.ROAD_STATION_ID = RS.ID\n" +
            ")\n" +
            "AND RS.TYPE = 2",
            nativeQuery = true)
    List<RoadStation> findOrphanWeatherRoadStations();

    @Query(value =
            "SELECT RS.*\n" +
                    "FROM ROAD_STATION RS\n" +
                    "WHERE NOT EXISTS (\n" +
                    "  SELECT NULL\n" +
                    "  FROM CAMERA_PRESET CP\n" +
                    "  WHERE CP.ROAD_STATION_ID = RS.ID\n" +
                    ")\n" +
                    "AND RS.TYPE = 3",
            nativeQuery = true)
    List<RoadStation> findOrphanCameraRoadStations();

    @Query(value =
                   "SELECT RS.*\n" +
                   "FROM ROAD_STATION RS\n" +
                   "WHERE NOT EXISTS (\n" +
                   "  SELECT NULL\n" +
                   "  FROM LAM_STATION LAM\n" +
                   "  WHERE LAM.ROAD_STATION_ID = RS.ID\n" +
                   ")\n" +
                   "AND RS.TYPE = 1",
           nativeQuery = true)
    List<RoadStation> findOrphanLamRoadStations();

    RoadStation findByTypeAndNaturalId(RoadStationType type, long naturalId);

    @Query(value =
           "SELECT rs.naturalId\n" +
           "FROM RoadStation rs\n" +
           "WHERE rs.type = ?1\n" +
           "  AND rs.obsolete = false\n" +
           "  AND rs.isPublic = true")
    List<Long> findNonObsoleteAndPublicRoadStationsNaturalIds(final RoadStationType roadStationType);


    @Query("SELECT CASE WHEN count(rs) > 0 THEN TRUE ELSE FALSE END\n" +
           "FROM RoadStation rs\n" +
           "WHERE rs.isPublic = 1\n" +
           "  AND rs.obsolete = 0\n" +
           "  AND rs.type = :roadStationType\n" +
           "  AND rs.naturalId = :roadStationNaturalId")
    boolean isPublicAndNotObsoleteRoadStation(@Param("roadStationNaturalId")
                                              final long roadStationNaturalId,
                                              @Param("roadStationType")
                                              final RoadStationType roadStationType);
}
