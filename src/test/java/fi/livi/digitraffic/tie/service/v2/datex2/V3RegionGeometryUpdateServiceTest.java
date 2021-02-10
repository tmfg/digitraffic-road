package fi.livi.digitraffic.tie.service.v2.datex2;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.dao.v3.RegionGeometryRepository;
import fi.livi.digitraffic.tie.model.v3.trafficannouncement.geojson.RegionGeometry;
import fi.livi.digitraffic.tie.service.v3.datex2.V3RegionGeometryUpdateService;

@Import({ V3RegionGeometryUpdateService.class, RegionGeometryGitClient.class})
public class V3RegionGeometryUpdateServiceTest extends AbstractServiceTest {
    private static final Logger log = getLogger(V3RegionGeometryUpdateServiceTest.class);

    @Autowired
    private V3RegionGeometryUpdateService v3RegionGeometryUpdateService;

    @MockBean
    private RegionGeometryGitClient regionGeometryGitClient;

    @Autowired
    private RegionGeometryRepository regionGeometryRepository;

    @Before
    public void cleanDb() {
        regionGeometryRepository.deleteAll();
    }

    @Test
    public void updateAreaLocationRegion() {
        final Instant secondCommiteffectiveDate = Instant.now();
        final Instant firstCommiteffectiveDate = secondCommiteffectiveDate.minus(1, ChronoUnit.DAYS);

        final String commitId1 = RandomStringUtils.randomAlphanumeric(32);
        final String commitId2 = RandomStringUtils.randomAlphanumeric(32);
        final List<RegionGeometry> commit1Changes = createCommit(commitId1, firstCommiteffectiveDate, 1,2,3);
        final List<RegionGeometry> commit2Changes = createCommit(commitId2, secondCommiteffectiveDate, 1,2,3);

        when(regionGeometryGitClient.getChangesAfterCommit(eq(null))).thenReturn(commit1Changes);
        when(regionGeometryGitClient.getChangesAfterCommit(eq(commitId1))).thenReturn(commit2Changes);

        // No commits in db -> null commit id
        v3RegionGeometryUpdateService.updateAreaLocationRegions();
        verify(regionGeometryGitClient, times(1)).getChangesAfterCommit(eq(null));

        final List<RegionGeometry> dbCommit1 = regionGeometryRepository.findAll(Sort.by("id"));
        Assert.assertEquals(commit1Changes.size(), dbCommit1.size());

        // Commit1 in db -> commitId1
        v3RegionGeometryUpdateService.updateAreaLocationRegions();
        verify(regionGeometryGitClient, times(1)).getChangesAfterCommit(eq(commitId1));

        final List<RegionGeometry> dbCommit1And2 = regionGeometryRepository.findAll(Sort.by("id"));
        Assert.assertEquals(commit1Changes.size() + commit2Changes.size(), dbCommit1And2.size());

        final List<RegionGeometry> allInOrder = regionGeometryRepository.findAllByOrderByIdAsc();

        assertVersion(0, 1, firstCommiteffectiveDate, commitId1, allInOrder);
        assertVersion(1, 2, firstCommiteffectiveDate, commitId1, allInOrder);
        assertVersion(2, 3, firstCommiteffectiveDate, commitId1, allInOrder);
        assertVersion(3, 1, secondCommiteffectiveDate, commitId2, allInOrder);
        assertVersion(4, 2, secondCommiteffectiveDate, commitId2, allInOrder);
        assertVersion(5, 3, secondCommiteffectiveDate, commitId2, allInOrder);
    }

    private void assertVersion(final int index, final Integer locationCode, final Instant effectiveDate, final String commitId,
                               final List<RegionGeometry> allInOrder) {
        final RegionGeometry region = allInOrder.get(index);
        Assert.assertEquals(effectiveDate, region.getEffectiveDate());
        Assert.assertEquals(locationCode, region.getLocationCode());
        Assert.assertEquals(commitId, region.getGitCommitId());
    }

    /**
     * Creates commit contents
     */
    private List<RegionGeometry> createCommit(final String commitId1, final Instant effectiveDate, int...locationCode) {
        return Arrays.stream(locationCode)
            .mapToObj(i -> RegionGeometryTestHelper.createNewRegionGeometry(i, effectiveDate, commitId1))
            .collect(Collectors.toList());
    }
}
