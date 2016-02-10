package fi.livi.digitraffic.tie.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.LamStationMetadata;

@Repository
public interface LamStationRepository extends JpaRepository<LamStationMetadata, Long> {

    @Query(value =
            "SELECT LS.NATURAL_ID AS LAM_ID\n" +
                    "     , RS.NAME AS RWS_NAME\n" +
                    "     , RS.NAME_FI AS NAME_FI\n" +
                    "     , RS.NAME_SE AS NAME_SE\n" +
                    "     , RS.NAME_EN AS NAME_EN\n" +
                    "     , RS.LATITUDE AS LATITUDE\n" +
                    "     , RS.LONGITUDE AS LONGITUDE\n" +
                    "     , RS.ELEVATION AS ELEVATION\n" +
                    "     , RD.NAME AS  PROVINCE\n" +
                    "FROM LAM_STATION LS\n" +
                    "INNER JOIN ROAD_STATION RS\n" +
                    "  ON LS.ROAD_STATION_ID = RS.ID\n" +
                    "INNER JOIN ROAD_DISTRICT RD\n" +
                    "  ON LS.ROAD_DISTRICT_ID = RD.ID\n" +
                    "WHERE LS.OBSOLETE = 0\n" +
                    "  AND RS.OBSOLETE = 0\n" +
                    "ORDER BY LS.NATURAL_ID",
            nativeQuery = true)
    List<LamStationMetadata> findAll();


    @Query(value =
            "SELECT LS.NATURAL_ID AS LAM_ID\n" +
                    "     , RS.NAME AS RWS_NAME\n" +
                    "     , RS.NAME_FI AS NAME_FI\n" +
                    "     , RS.NAME_SE AS NAME_SE\n" +
                    "     , RS.NAME_EN AS NAME_EN\n" +
                    "     , RS.LATITUDE AS LATITUDE\n" +
                    "     , RS.LONGITUDE AS LONGITUDE\n" +
                    "     , RS.ELEVATION AS ELEVATION\n" +
                    "     , RD.NAME AS  PROVINCE\n" +
                    "FROM LAM_STATION LS\n" +
                    "INNER JOIN ROAD_STATION RS\n" +
                    "  ON LS.ROAD_STATION_ID = RS.ID\n" +
                    "INNER JOIN ROAD_DISTRICT RD\n" +
                    "  ON LS.ROAD_DISTRICT_ID = RD.ID\n" +
                    "WHERE LS.OBSOLETE = 0\n" +
                    "  AND RS.OBSOLETE = 0\n" +
                    "ORDER BY LS.NATURAL_ID",
            nativeQuery = true)
    List<LamStationMetadata> findAllNonObsolete();
}
