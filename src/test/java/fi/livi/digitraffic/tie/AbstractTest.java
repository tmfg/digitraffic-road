package fi.livi.digitraffic.tie;

import static java.time.ZoneOffset.UTC;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fi.livi.digitraffic.tie.metadata.model.CalculatorDeviceType;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.model.CameraType;
import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.model.RoadAddress;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.TmsStation;
import fi.livi.digitraffic.tie.metadata.service.RoadDistrictService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RoadApplication.class,
                properties = { "config.test=true" },
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public abstract class AbstractTest {
    @Autowired
    protected ResourceLoader resourceLoader;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected RoadDistrictService roadDistrictService;

    protected Path getPath(final String filename) {
        return new File(getClass().getResource(filename).getFile()).toPath();
    }

    protected List<Resource> loadResources(final String pattern) throws IOException {
        return Arrays.asList(ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(pattern));
    }

    protected Resource loadResource(final String pattern) throws IOException {
        return resourceLoader.getResource(pattern);
    }

    protected ArrayList<String> readResourceContents(final String resourcePattern) throws IOException {
        final List<Resource> datex2Resources = loadResources(resourcePattern);
        final ArrayList<String> contents = new ArrayList<>();

        for (Resource datex2Resource : datex2Resources) {
            contents.add(FileUtils.readFileToString(datex2Resource.getFile(), StandardCharsets.UTF_8));
        }
        return contents;
    }

    protected String readResourceContent(final String resourcePattern) throws IOException {
        final Resource datex2Resource = loadResource(resourcePattern);

        return FileUtils.readFileToString(datex2Resource.getFile(), StandardCharsets.UTF_8);
    }

    protected static void assertCollectionSize(final int expectedSize, final Collection<?> collection) {
        final int collectionSize = collection.size();

        Assert.assertTrue(String.format("Collection size was expected to be %d, was %s", expectedSize, collectionSize),
            collectionSize == expectedSize);
    }

    protected static void assertEmpty(final Collection<?> col) {
        assertCollectionSize(0, col);
    }

    protected static void assertTimesEqual(final ZonedDateTime t1, final ZonedDateTime t2) {
        if(t1 == null && t2 == null) return;

        if(t1 == null && t2 != null) {
            Assert.fail("was asserted to be null, was not");
        }

        if(t1 != null && t2 == null) {
            Assert.fail("given value was null");
        }

        final ZonedDateTime tz1 = t1.withZoneSameInstant(UTC);
        final ZonedDateTime tz2 = t2.withZoneSameInstant(UTC);

        Assert.assertEquals(tz1, tz2);
    }

    protected CameraPreset generateDummyPreset() {
        final RoadStation rs = generateDummyRoadStation(RoadStationType.CAMERA_STATION);

        final CameraPreset cp = new CameraPreset();
        cp.setRoadStation(rs);

        final String cameraId = "C" + rs.getNaturalId();
        final String direction = String.valueOf(RandomUtils.nextLong(10, 100));
        cp.setPresetId(cameraId + direction);
        cp.setCameraId(cameraId);
        cp.setPublicExternal(true);
        cp.setPublicInternal(true);
        cp.setLotjuId(RandomUtils.nextLong(100000000, 1000000000) * -1);
        cp.setInCollection(true);
        cp.setCameraType(CameraType.VAPIX);
        cp.setCompression(50);
        cp.setDefaultDirection(true);
        cp.setResolution("1920x1080");
        cp.setDirection("1");
        cp.setLotjuId(rs.getLotjuId());
        cp.setCameraLotjuId(cp.getLotjuId());

        return cp;
    }

    protected TmsStation generateDummyTmsStation() {
        final RoadStation rs = generateDummyRoadStation(RoadStationType.TMS_STATION);

        final TmsStation ts = new TmsStation();
        ts.setRoadStation(rs);
        ts.setLotjuId(rs.getLotjuId());
        ts.setNaturalId(rs.getLotjuId());
        ts.setRoadDistrict(roadDistrictService.findByNaturalId(1));
        ts.setCalculatorDeviceType(CalculatorDeviceType.DSL_5);
        ts.setName("st120_Pähkinärinne");
        ts.setDirection1Municipality("Vihti");
        ts.setDirection1MunicipalityCode(927);
        ts.setDirection2Municipality("Helsinki");
        ts.setDirection2MunicipalityCode(91);
        ts.setWinterFreeFlowSpeed1(70);
        ts.setWinterFreeFlowSpeed2(70);
        ts.setSummerFreeFlowSpeed1(80);
        ts.setSummerFreeFlowSpeed2(80);

        return ts;
    }

    protected RoadStation generateDummyRoadStation(final RoadStationType roadStationType) {
        final RoadStation rs = new RoadStation(roadStationType);
        rs.setNaturalId(80000  + RandomUtils.nextLong(1000, 10000));
        rs.setName(roadStationType.name());
        rs.setLotjuId(rs.getNaturalId());
        rs.setPublic(true);
        rs.setCollectionStatus(CollectionStatus.GATHERING);
        rs.setPurpose("Maisema");
        rs.setLatitude(BigDecimal.valueOf(6687086));
        rs.setLongitude(BigDecimal.valueOf(351822));
        rs.setAltitude(BigDecimal.TEN);
        rs.setMunicipality("Vihti");
        rs.setMunicipalityCode("927");
        rs.setProvince("Uusimaa");
        rs.setProvinceCode("1");
        rs.setName("vt1_Vihti_Palojärvi_1");
        rs.setNameFi("Tie 1 Vihti, Palojärvi");
        rs.setNameEn("Road 1 Vihti, Palojärvi");
        rs.setNameSv("Väg 1 Vihtis, Palojärvi");

        RoadAddress ra = generateDummyRoadAddres();
        rs.setRoadAddress(ra);

        return rs;
    }

    private RoadAddress generateDummyRoadAddres() {
        final RoadAddress ra = new RoadAddress();
        ra.setCarriagewayCode(1);
        ra.setSideCode(1);
        ra.setContractArea("Uusimaa");
        ra.setContractAreaCode(1);
        ra.setDistanceFromRoadSectionStart(1000);
        ra.setRoadMaintenanceClass("1");
        ra.setRoadNumber(1);
        ra.setRoadSection(1);
        return ra;
    }

}
