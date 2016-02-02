package fi.livi.digitraffic.tie.dao;

import java.util.List;

import fi.livi.digitraffic.tie.model.LamStationMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LamStationRepository extends JpaRepository<LamStationMetadata, Long> {

    @Query(value =
            "SELECT LS.NATURAL_ID AS LAM_ID\n" +
                    "     , RS.NAME AS RWS_NAME\n" +
                    "     , LS.NAME AS NAME_FI\n" +
                    "     , LS.NAME AS NAME_SV\n" +
                    "     , LS.NAME AS NAME_EN\n" +
                    "     , 0 AS X\n" +
                    "     , 0 AS Y\n" +
                    "     , 0 AS Z\n" +
                    "     , RD.NAME AS  PROVINCE\n" +
                    "FROM LAM_STATION LS\n" +
                    "INNER JOIN ROAD_DISTRICT RD \n" +
                    "  ON LS.ROAD_DISTRICT_ID = RD.ID\n" +
                    "INNER JOIN ROAD_STATION RS\n" +
                    "  ON LS.ROAD_STATION_ID = RS.ID\n" +
                    "WHERE LS.OBSOLETE = 0\n" +
                    "  AND RS.OBSOLETE = 0\n" +
                    "ORDER BY LS.NATURAL_ID",
            nativeQuery = true)
    List<LamStationMetadata> findAllNonObsolete();
}
