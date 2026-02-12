package fi.livi.digitraffic.tie.data.dao;

import static fi.livi.digitraffic.tie.TestUtils.getRandomId;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.test.util.AssertUtil;
import fi.livi.digitraffic.tie.AbstractJpaTest;
import fi.livi.digitraffic.tie.dao.trafficmessage.RegionGeometryRepository;
import fi.livi.digitraffic.tie.model.trafficmessage.RegionGeometry;
import fi.livi.digitraffic.tie.service.trafficmessage.RegionGeometryTestHelper;

public class RegionGeometryDaoTest extends AbstractJpaTest {

    @Autowired
    private RegionGeometryRepository regionGeometryRepository;

    @BeforeEach
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
        AssertUtil.assertCollectionSize(1, result);
        final RegionGeometry tgt = result.getFirst();

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
        final int count = getRandomId(10, 50);
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
        final int count = getRandomId(10, 50);
        final List<RegionGeometry> allInOrder = new ArrayList<>();
        IntStream.range(0, count).forEach(i -> allInOrder.add(RegionGeometryTestHelper.createNewRegionGeometry(1)));
        regionGeometryRepository.saveAll(allInOrder);
        // This will change default order for findAll
        entityManager.createNativeQuery(
            "UPDATE region_geometry\n" +
            "SET git_id = '" + RandomStringUtils.secure().nextAlphanumeric(32) + "'\n" +
            "WHERE id = " + allInOrder.get(5).getId()).executeUpdate();

        final List<RegionGeometry> allDb = regionGeometryRepository.findAllByOrderByIdAsc();
        for (int i = 0; i < allInOrder.size(); i++) {
            assertEquals(allInOrder.get(i).getId(), allDb.get(i).getId(), "Elemets [" + i + "] not equal");
        }
    }
}
