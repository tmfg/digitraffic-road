package fi.livi.digitraffic.tie.data.dao;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;

import fi.livi.digitraffic.tie.AbstractJpaTest;
import fi.livi.digitraffic.tie.dao.v3.RegionGeometryRepository;
import fi.livi.digitraffic.tie.helper.AssertHelper;
import fi.livi.digitraffic.tie.model.v3.trafficannouncement.geojson.RegionGeometry;
import fi.livi.digitraffic.tie.service.v2.datex2.RegionGeometryTestHelper;

@Import({ JacksonAutoConfiguration.class })
public class RegionGeometryDaoTest extends AbstractJpaTest {
    private static final Logger log = LoggerFactory.getLogger(RegionGeometryDaoTest.class);


    @Autowired
    private RegionGeometryRepository regionGeometryRepository;

    @Before
    public void cleanDb() {
        regionGeometryRepository.deleteAll();
    }

    @Test
    public void savedAndLoadedEquals() {

        final RegionGeometry src = RegionGeometryTestHelper.createNewRegionGeometry(1);
        regionGeometryRepository.save(src);
        entityManager.flush();
        entityManager.clear();

        final String latestCommitId = regionGeometryRepository.getLatestCommitId();
        assertEquals(src.getGitCommitId(), latestCommitId);

        final List<RegionGeometry> result = regionGeometryRepository.findAll();
        AssertHelper.assertCollectionSize(1, result);
        final RegionGeometry tgt = result.get(0);

        assertArrayEquals(src.getGeometry().getCoordinates(), tgt.getGeometry().getCoordinates());
        assertEquals(src.getGeometry(), tgt.getGeometry());
        assertEquals(src.getEffectiveDate(), tgt.getEffectiveDate());
        assertEquals(src.getId(), tgt.getId());
        assertEquals(src.getLocationCode(), tgt.getLocationCode());
        assertEquals(src.getVersionDate(), tgt.getVersionDate());
        assertEquals(src.getType(), tgt.getType());
        assertEquals(src.getGitId(), tgt.getGitId());
        assertEquals(src.getGitPath(), tgt.getGitPath());
        assertEquals(src.getGitCommitId(), tgt.getGitCommitId());
    }

    @Test
    public void getLatestCommitId() {
        final Integer count = getRandomId(10, 50);
        final AtomicReference<String> latestCommitId = new AtomicReference<>();
        IntStream.range(0, count).forEach(i -> {
            final RegionGeometry src = RegionGeometryTestHelper.createNewRegionGeometry(1);
            regionGeometryRepository.save(src);
            latestCommitId.set(src.getGitCommitId());
        });
        assertEquals(latestCommitId.get(), regionGeometryRepository.getLatestCommitId());
    }

    @Test
    public void findAllByOrderByIdAsc() {
        final Integer count = getRandomId(10, 50);
        final List<RegionGeometry> allInOrder = new ArrayList<>();
        IntStream.range(0, count).forEach(i -> allInOrder.add(RegionGeometryTestHelper.createNewRegionGeometry(1)));
        allInOrder.forEach(a -> regionGeometryRepository.save(a));
        // This will change default order for findAll
        entityManager.createNativeQuery(
            "UPDATE region_geometry\n" +
            "SET git_id = '" + RandomStringUtils.randomAlphanumeric(32) + "'\n" +
            "WHERE id = " + allInOrder.get(5).getId()).executeUpdate();

        final List<RegionGeometry> allDb = regionGeometryRepository.findAllByOrderByIdAsc();
        for (int i = 0; i < allInOrder.size(); i++) {
            assertEquals("Elemets [" + i + "] not equal", allInOrder.get(i).getId(), allDb.get(i).getId());
        }
    }
}
