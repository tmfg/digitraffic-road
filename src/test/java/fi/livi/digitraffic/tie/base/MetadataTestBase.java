package fi.livi.digitraffic.tie.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class MetadataTestBase extends AbstractTestBase {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected void generateMissingLotjuIds() {

        jdbcTemplate.execute(
                "UPDATE ROAD_STATION\n" +
                "SET LOTJU_ID = -1 * id\n" +
                "WHERE LOTJU_ID IS NULL");

        jdbcTemplate.execute(
                "UPDATE LAM_STATION TGT\n" +
                "SET LOTJU_ID = (\n" +
                "  SELECT LOTJU_ID" +
                "  FROM ROAD_STATION RS\n" +
                "  WHERE RS.ID = TGT.ROAD_STATION_ID\n" +
                ")\n" +
                "WHERE LOTJU_ID IS NULL");

        jdbcTemplate.execute(
                "UPDATE WEATHER_STATION TGT\n" +
                "SET LOTJU_ID = (\n" +
                "  SELECT LOTJU_ID" +
                "  FROM ROAD_STATION RS\n" +
                "  WHERE RS.ID = TGT.ROAD_STATION_ID\n" +
                ")\n" +
                "WHERE LOTJU_ID IS NULL");

        jdbcTemplate.execute(
                "UPDATE CAMERA_PRESET\n" +
                "SET LOTJU_ID = -1 * id\n" +
                "WHERE LOTJU_ID IS NULL");

        jdbcTemplate.execute(
                "UPDATE CAMERA_PRESET\n" +
                "SET CAMERA_LOTJU_ID = -10 * id\n" +
                "WHERE CAMERA_LOTJU_ID IS NULL");

        jdbcTemplate.execute(
                "UPDATE ROAD_STATION_SENSOR\n" +
                "SET LOTJU_ID = -1 * id\n" +
                "WHERE LOTJU_ID IS NULL");
    }

    protected void fixData() {

        jdbcTemplate.execute(
                "UPDATE ROAD_STATION\n" +
                "SET COLLECTION_STATUS = 'GATHERING'\n" +
                "WHERE COLLECTION_STATUS IS NULL");

        jdbcTemplate.execute(
                "UPDATE ROAD_STATION\n" +
                "SET MUNICIPALITY = 'Helsinki'\n" +
                "WHERE MUNICIPALITY IS NULL");

        jdbcTemplate.execute(
                "UPDATE ROAD_STATION\n" +
                "SET MUNICIPALITY_CODE = 91\n" +
                "WHERE MUNICIPALITY_CODE IS NULL");

        jdbcTemplate.execute(
                "UPDATE ROAD_STATION\n" +
                "SET PROVINCE = 'Uusimaa'\n" +
                "WHERE PROVINCE IS NULL");

        jdbcTemplate.execute(
                "UPDATE ROAD_STATION\n" +
                "SET PROVINCE_CODE = 1\n" +
                "WHERE PROVINCE_CODE IS NULL");

        jdbcTemplate.execute(
                "UPDATE ROAD_STATION RS\n" +
                "SET RS.OBSOLETE_DATE = null,\n" +
                "    RS.OBSOLETE = 0\n" +
                "WHERE RS.LOTJU_ID IS NOT NULL\n" +
                "  AND NOT exists (\n" +
                "    SELECT NULL\n" +
                "    FROM ROAD_STATION ORS\n" +
                "    WHERE ors.natural_id = rs.natural_id\n" +
                "    AND ors.id <> rs.id\n" +
                "    AND (ORS.OBSOLETE_DATE = null or ORS.OBSOLETE = 0)\n" +
                "  )\n" +
                "  AND NOT exists (\n" +
                "    SELECT NULL\n" +
                "    FROM ROAD_STATION ORS\n" +
                "    WHERE ors.LOTJU_ID = rs.LOTJU_ID\n" +
                "    AND ors.id <> rs.id\n" +
                "    AND (ORS.OBSOLETE_DATE = null or ORS.OBSOLETE = 0)\n" +
                ")");
    }

    protected void restoreGeneratedLotjuIds() {

        jdbcTemplate.execute(
                "UPDATE ROAD_STATION\n" +
                "SET LOTJU_ID = NULL\n" +
                "WHERE LOTJU_ID < 0");

        jdbcTemplate.execute(
                "UPDATE LAM_STATION\n" +
                "SET LOTJU_ID = NULL\n" +
                "WHERE LOTJU_ID < 0");

        jdbcTemplate.execute(
                "UPDATE WEATHER_STATION\n" +
                "SET LOTJU_ID = NULL\n" +
                "WHERE LOTJU_ID < 0");

        jdbcTemplate.execute(
                "UPDATE CAMERA_PRESET\n" +
                "SET LOTJU_ID = NULL\n" +
                "WHERE LOTJU_ID < 0");

        jdbcTemplate.execute(
                "UPDATE ROAD_STATION_SENSOR\n" +
                "SET LOTJU_ID = NULL\n" +
                "WHERE LOTJU_ID < 0");
    }
}
