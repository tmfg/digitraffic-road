package fi.livi.digitraffic.tie.data.dao;

import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.RoadStationStatus;

@Repository
public interface RoadStationStatusRepository extends JpaRepository<RoadStationStatus, Long> {
    @Query(value =
            "SELECT RS.NATURAL_ID AS ROAD_STATION_ID\n" +
            "     , SV1.VALUE AS CONDITION_CODE\n" +
            "     , SV1.MEASURED AS CONDITION_UPDATED\n" +
            "     , SV2.VALUE AS COLLECTION_STATUS_CODE\n" +
            "     , SV2.MEASURED AS COLLECTION_STATUS_UPDATED\n" +
            "FROM ROAD_STATION RS\n" +
            "LEFT OUTER JOIN SENSOR_VALUE SV1 ON SV1.ROAD_STATION_ID = RS.ID AND SV1.ROAD_STATION_SENSOR_ID = 1\n" +
            "LEFT OUTER JOIN SENSOR_VALUE SV2 ON SV2.ROAD_STATION_ID = RS.ID AND SV2.ROAD_STATION_SENSOR_ID = 2\n" +
            "WHERE RS.OBSOLETE = 0\n" +
            "  AND NVL(SV1.VALUE, SV2.VALUE) IS NOT NULL\n" +
             "ORDER BY RS.NATURAL_ID",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<RoadStationStatus> findAllRoadStationStatuses();
}
