package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
            "  FROM ROAD_WEATHER_STATION RWS\n" +
            "  WHERE RWS.ROAD_STATION_ID = RS.ID\n" +
            ")\n" +
            "AND RS.TYPE = 2",
            nativeQuery = true)
    List<RoadStation> findOrphanWeatherStationRoadStations();

    @Query(value =
            "SELECT RS.*\n" +
                    "FROM ROAD_STATION RS\n" +
                    "WHERE NOT EXISTS (\n" +
                    "  SELECT NULL\n" +
                    "  FROM ROAD_WEATHER_STATION RWS\n" +
                    "  WHERE RWS.ROAD_STATION_ID = RS.ID\n" +
                    ")\n" +
                    "AND RS.TYPE = 3",
            nativeQuery = true)
    List<RoadStation> findOrphanCameraStationRoadStations();

    RoadStation findByTypeAndNaturalId(RoadStationType type, long naturalId);
}
