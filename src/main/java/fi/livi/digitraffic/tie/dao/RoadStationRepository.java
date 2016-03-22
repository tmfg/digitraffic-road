package fi.livi.digitraffic.tie.dao;

import java.util.List;

import fi.livi.digitraffic.tie.model.RoadStation;
import fi.livi.digitraffic.tie.model.RoadStationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
            "AND RS.TYPE = :type",
            nativeQuery = true)
    List<RoadStation> findOrphansByType(@Param("type") int type);
}
