package fi.livi.digitraffic.tie.data.dao;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.data.dto.lam.LamMeasurementDto;

@Repository
public interface LamMeasurementRepository extends JpaRepository<LamMeasurementDto, Long> {

    @Query(value =
            "SELECT LS.NATURAL_ID\n" +
            "     , LSD.TRAFFIC_VOLUME_1 AS TRAFFIC_VOLUME1\n" +
            "     , LSD.TRAFFIC_VOLUME_2 AS TRAFFIC_VOLUME2\n" +
            "     , LSD.AVERAGE_SPEED_1 AS AVERAGE_SPEED1\n" +
            "     , LSD.AVERAGE_SPEED_2 AS AVERAGE_SPEED2\n" +
            "     , LSD.MEASURED\n" +
            // over (partition by null) or over() give a calculation on a row basis.
            "     , max(LSD.MEASURED) over () STATION_LATEST_MEASURED\n" +
            "FROM LAM_STATION LS\n" +
            "INNER JOIN ROAD_STATION RS ON RS.ID = LS.ROAD_STATION_ID\n" +
            "INNER JOIN LAM_STATION_DATA LSD ON LSD.LAM_STATION_ID = LS.ID\n" +
            "WHERE LS.OBSOLETE = 0\n" +
            "  AND RS.IS_PUBLIC = 1\n" +
            "ORDER BY LS.NATURAL_ID",
            nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<LamMeasurementDto> listAllLamDataFromNonObsoleteStations();

    @Query(value =
            "SELECT MAX(LSD.MEASURED) AS UPDATED\n" +
            "FROM LAM_STATION LS\n" +
            "INNER JOIN LAM_STATION_DATA LSD ON LSD.LAM_STATION_ID = LS.ID\n" +
            "WHERE LS.OBSOLETE = 0",
           nativeQuery = true)
    LocalDateTime getLatestMeasurementTime();
}
