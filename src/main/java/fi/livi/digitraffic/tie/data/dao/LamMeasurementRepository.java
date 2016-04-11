package fi.livi.digitraffic.tie.data.dao;

import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.data.model.LamMeasurement;

@Repository
public interface LamMeasurementRepository extends JpaRepository<LamMeasurement, Long> {
    @Query(value =
            "select lam_station_id as lam_id, traffic_volume_1 as traffic_volume1, traffic_volume_2 as traffic_volume2, average_speed_1 " +
                    "as average_speed1, average_speed_2 as average_speed2, measured\n" +
                    "from lam_station ls, lam_station_data lsd\n" +
                    "where lsd.lam_station_id = ls.id\n" +
                    "and ls.obsolete = 0",
            nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<LamMeasurement> listAllLamDataFromNonObsoleteStations();
}
