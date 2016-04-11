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
    @Query(value = "select rs.natural_id as station_id, sv1.value as road_station_status, nvl(sv1.measured, sv2.measured) as updated, sv2.value as station_data_collection_status\n"
            + "from road_station rs, sensor_value sv1, sensor_value sv2\n"
            + "where rs.obsolete = 0\n"
            + "and sv1.road_station_id (+) = rs.id\n"
            + "and sv2.road_station_id (+) = rs.id\n"
            + "and sv1.road_station_sensor_id (+) = 1\n"
            + "and sv2.road_station_sensor_id (+) = 2\n"
            + "and nvl(sv1.value, sv2.value) is not null", nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<RoadStationStatus> findAllRoadStationStatuses();
}
