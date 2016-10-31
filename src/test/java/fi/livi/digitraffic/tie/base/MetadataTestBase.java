package fi.livi.digitraffic.tie.base;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import fi.livi.digitraffic.tie.MetadataApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MetadataApplication.class,
                properties = {"config.test=true"})
@WebAppConfiguration
public abstract class MetadataTestBase {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected void generateMissingLotjuIds() {

        SqlRowSet result = jdbcTemplate.queryForRowSet("SELECT * FROM ROAD_STATION");

        for (String s : result.getMetaData().getColumnNames()) {
            System.out.println(s);
        }


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
                "UPDATE ROAD_STATION\n" +
                "SET OBSOLETE_DATE = null,\n" +
                "    OBSOLETE = 0\n" +
                "WHERE LOTJU_ID < 0");
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
