package fi.livi.digitraffic.tie.base;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.apache.commons.io.FileUtils;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import fi.livi.digitraffic.tie.MetadataApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MetadataApplication.class,
                properties = {"config.test=true"})
@WebAppConfiguration
@Transactional
public abstract class AbstractMetadataWebTestBase {

    @Autowired
    ResourceLoader resourceLoader;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @PersistenceContext
    protected EntityManager entityManager;

    protected Path getPath(final String filename) {
        return new File(getClass().getResource(filename).getFile()).toPath();
    }

    protected List<Resource> loadResources(String pattern) throws IOException {
        return Arrays.asList(ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(pattern));
    }

    protected Resource loadResource(String pattern) throws IOException {
        return resourceLoader.getResource(pattern);
    }

    protected ArrayList<String> readResourceContents(String resourcePattern) throws IOException {
        List<Resource> datex2Resources = loadResources(resourcePattern);
        ArrayList<String> contents = new ArrayList<>();
        for (Resource datex2Resource : datex2Resources) {
            contents.add(FileUtils.readFileToString(datex2Resource.getFile(), StandardCharsets.UTF_8));
        }
        return contents;
    }

    protected String readResourceContent(String resourcePattern) throws IOException {
        Resource datex2Resource = loadResource(resourcePattern);
        return FileUtils.readFileToString(datex2Resource.getFile(), StandardCharsets.UTF_8);
    }

    protected void generateMissingLotjuIdsWithJdbc() {

        //        jdbcTemplate.execute(
        //                "UPDATE ROAD_STATION\n" +
        //                        "SET LOTJU_ID = -1 * id\n" +
        //                        "WHERE LOTJU_ID IS NULL");
        //
        //        jdbcTemplate.execute(
        //                "UPDATE LAM_STATION TGT\n" +
        //                        "SET LOTJU_ID = (\n" +
        //                        "  SELECT LOTJU_ID" +
        //                        "  FROM ROAD_STATION RS\n" +
        //                        "  WHERE RS.ID = TGT.ROAD_STATION_ID\n" +
        //                        ")\n" +
        //                        "WHERE LOTJU_ID IS NULL");
        //
        //        jdbcTemplate.execute(
        //                "UPDATE WEATHER_STATION TGT\n" +
        //                        "SET LOTJU_ID = (\n" +
        //                        "  SELECT LOTJU_ID" +
        //                        "  FROM ROAD_STATION RS\n" +
        //                        "  WHERE RS.ID = TGT.ROAD_STATION_ID\n" +
        //                        ")\n" +
        //                        "WHERE LOTJU_ID IS NULL");
        //
        //        jdbcTemplate.execute(
        //                "UPDATE ROAD_STATION_SENSOR\n" +
        //                        "SET LOTJU_ID = -1 * id\n" +
        //                        "WHERE LOTJU_ID IS NULL");
    }

    protected void fixDataWithJdbc() {

        //        jdbcTemplate.execute(
        //                "UPDATE ROAD_STATION\n" +
        //                        "SET COLLECTION_STATUS = 'GATHERING'\n" +
        //                        "WHERE COLLECTION_STATUS IS NULL");
        //
        //        jdbcTemplate.execute(
        //                "UPDATE ROAD_STATION\n" +
        //                        "SET MUNICIPALITY = 'Helsinki'\n" +
        //                        "WHERE MUNICIPALITY IS NULL");
        //
        //        jdbcTemplate.execute(
        //                "UPDATE ROAD_STATION\n" +
        //                        "SET MUNICIPALITY_CODE = 91\n" +
        //                        "WHERE MUNICIPALITY_CODE IS NULL");
        //
        //        jdbcTemplate.execute(
        //                "UPDATE ROAD_STATION\n" +
        //                        "SET PROVINCE = 'Uusimaa'\n" +
        //                        "WHERE PROVINCE IS NULL");
        //
        //        jdbcTemplate.execute(
        //                "UPDATE ROAD_STATION\n" +
        //                        "SET PROVINCE_CODE = 1\n" +
        //                        "WHERE PROVINCE_CODE IS NULL");
        //
        //        jdbcTemplate.execute(
        //                "UPDATE ROAD_STATION RS\n" +
        //                        "SET RS.OBSOLETE_DATE = null,\n" +
        //                        "    RS.OBSOLETE = 0\n" +
        //                        "WHERE RS.LOTJU_ID IS NOT NULL\n" +
        //                        "  AND NOT exists (\n" +
        //                        "    SELECT NULL\n" +
        //                        "    FROM ROAD_STATION ORS\n" +
        //                        "    WHERE ors.natural_id = rs.natural_id\n" +
        //                        "    AND ors.id <> rs.id\n" +
        //                        "    AND (ORS.OBSOLETE_DATE = null or ORS.OBSOLETE = 0)\n" +
        //                        "  )\n" +
        //                        "  AND NOT exists (\n" +
        //                        "    SELECT NULL\n" +
        //                        "    FROM ROAD_STATION ORS\n" +
        //                        "    WHERE ors.LOTJU_ID = rs.LOTJU_ID\n" +
        //                        "    AND ors.id <> rs.id\n" +
        //                        "    AND (ORS.OBSOLETE_DATE = null or ORS.OBSOLETE = 0)\n" +
        //                        ")");
    }

    protected void restoreGeneratedLotjuIdsWithJdbc() {

        //        jdbcTemplate.execute(
        //                "UPDATE ROAD_STATION\n" +
        //                        "SET LOTJU_ID = NULL\n" +
        //                        "WHERE LOTJU_ID < 0");
        //
        //        jdbcTemplate.execute(
        //                "UPDATE LAM_STATION\n" +
        //                        "SET LOTJU_ID = NULL\n" +
        //                        "WHERE LOTJU_ID < 0");
        //
        //        jdbcTemplate.execute(
        //                "UPDATE WEATHER_STATION\n" +
        //                        "SET LOTJU_ID = NULL\n" +
        //                        "WHERE LOTJU_ID < 0");
        //
        //        jdbcTemplate.execute(
        //                "UPDATE ROAD_STATION_SENSOR\n" +
        //                        "SET LOTJU_ID = NULL\n" +
        //                        "WHERE LOTJU_ID < 0");
    }

}
