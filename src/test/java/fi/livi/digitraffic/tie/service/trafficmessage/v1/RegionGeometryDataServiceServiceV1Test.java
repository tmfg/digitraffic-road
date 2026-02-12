package fi.livi.digitraffic.tie.service.trafficmessage.v1;

import static fi.livi.digitraffic.test.util.AssertUtil.assertCollectionSize;
import static fi.livi.digitraffic.tie.service.trafficmessage.RegionGeometryTestHelper.readRegionGeometry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import fi.livi.digitraffic.tie.AbstractWebServiceTestWithRegionGeometryGitMock;
import fi.livi.digitraffic.tie.dao.trafficmessage.RegionGeometryRepository;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.AreaType;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.region.RegionGeometryFeatureCollection;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.region.RegionGeometryProperties;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.model.trafficmessage.RegionGeometry;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.trafficmessage.RegionGeometryTestHelper;
import fi.livi.digitraffic.tie.service.trafficmessage.RegionGeometryUpdateJobTestHelper;
import fi.livi.digitraffic.tie.service.trafficmessage.RegionGeometryUpdateService;

public class RegionGeometryDataServiceServiceV1Test extends AbstractWebServiceTestWithRegionGeometryGitMock {

    private static final Logger log = LoggerFactory.getLogger(RegionGeometryDataServiceServiceV1Test.class);

    @Autowired
    private RegionGeometryRepository regionGeometryRepository;
    @MockitoSpyBean
    private RegionGeometryDataServiceV1 regionGeometryDataServiceV1;
    @Autowired
    private DataStatusService dataStatusService;
    @Autowired
    private GenericApplicationContext applicationContext;

    private RegionGeometryUpdateJobTestHelper v3RegionGeometryTestHelper;

    @BeforeEach
    public void init() {
        final RegionGeometryUpdateService regionGeometryUpdateService =
            applicationContext.getAutowireCapableBeanFactory().createBean(RegionGeometryUpdateService.class);
        v3RegionGeometryTestHelper = new RegionGeometryUpdateJobTestHelper(regionGeometryGitClientMock, regionGeometryUpdateService, dataStatusService);

        regionGeometryRepository.deleteAll();

    }

    @Test
    public void getAreaLocationRegionEffectiveOn_WhenToCommitsHaveSameffectiveDatLatestCommitReturned() {
        final Instant secondAndThirdCommiteffectiveDate = Instant.now();
        final Instant firstCommiteffectiveDate = secondAndThirdCommiteffectiveDate.minus(1, ChronoUnit.DAYS);
        final String commitId1 = RandomStringUtils.secure().nextAlphanumeric(32);
        final String commitId2 = RandomStringUtils.secure().nextAlphanumeric(32);
        final String commitId3 = RandomStringUtils.secure().nextAlphanumeric(32);
        final List<RegionGeometry> commit1Changes = createCommit(commitId1, firstCommiteffectiveDate, 1,2,3);
        final List<RegionGeometry> commit2Changes = createCommit(commitId2, secondAndThirdCommiteffectiveDate, 1,2,3);
        final List<RegionGeometry> commit3Changes = createCommit(commitId3, secondAndThirdCommiteffectiveDate, 1,2,3);

        when(regionGeometryGitClientMock.getChangesAfterCommit(eq(null))).thenReturn(commit1Changes);
        when(regionGeometryGitClientMock.getChangesAfterCommit(eq(commitId1))).thenReturn(commit2Changes);
        when(regionGeometryGitClientMock.getChangesAfterCommit(eq(commitId2))).thenReturn(commit3Changes);

        v3RegionGeometryTestHelper.runUpdateJob(); // update to commit1
        v3RegionGeometryTestHelper.runUpdateJob(); // update to commit2
        v3RegionGeometryTestHelper.runUpdateJob(); // update to commit3

        regionGeometryDataServiceV1.refreshCache();

        // Latest valid on time should be returned
        assertVersion(commit3Changes.getFirst(),
                      regionGeometryDataServiceV1.getAreaLocationRegionEffectiveOn(1, secondAndThirdCommiteffectiveDate));

        // First commit should be returned
        assertVersion(commit1Changes.getFirst(),
                      regionGeometryDataServiceV1.getAreaLocationRegionEffectiveOn(1, secondAndThirdCommiteffectiveDate.minusSeconds(1)));
    }

    @Test
    public void getAreaLocationRegionEffectiveOn_WhenThereIsInvalidTypeFirstValidShouldReturn() {
        final Instant secondCommiteffectiveDate = Instant.now();
        final Instant firstCommiteffectiveDate = secondCommiteffectiveDate.minus(1, ChronoUnit.DAYS);
        final String commitId1 = RandomStringUtils.secure().nextAlphanumeric(32);
        final String commitId2 = RandomStringUtils.secure().nextAlphanumeric(32);
        final List<RegionGeometry> commit1Changes = Collections.singletonList(
            RegionGeometryTestHelper.createNewRegionGeometry(1, firstCommiteffectiveDate, commitId1, AreaType.UNKNOWN));
        final List<RegionGeometry> commit2Changes = createCommit(commitId2, secondCommiteffectiveDate, 1);

        when(regionGeometryGitClientMock.getChangesAfterCommit(eq(null))).thenReturn(commit1Changes);
        when(regionGeometryGitClientMock.getChangesAfterCommit(eq(commitId1))).thenReturn(commit2Changes);

        v3RegionGeometryTestHelper.runUpdateJob(); // update to commit1
        v3RegionGeometryTestHelper.runUpdateJob(); // update to commit2
        regionGeometryDataServiceV1.refreshCache();

        // Even when asking version valid from commit1, it should not be returned as it is not valid
        // Instead commit2 version should be returned although it's not effective but it's first effective that is valid
        assertVersion(commit2Changes.getFirst(),
            regionGeometryDataServiceV1.getAreaLocationRegionEffectiveOn(1, firstCommiteffectiveDate));
    }

    @Test
    public void findAreaLocationRegionsWithEffectiveDateAndId() {
        // Create two commits with two effective dates and three locations
        final Instant commit2EffectiveDate = Instant.now();
        final Instant commit1EffectiveDate = commit2EffectiveDate.minus(1, ChronoUnit.DAYS);

        final String commitId1 = RandomStringUtils.secure().nextAlphanumeric(32);
        final String commitId2 = RandomStringUtils.secure().nextAlphanumeric(32);

        final List<RegionGeometry> commit1Changes = createCommit(commitId1, commit1EffectiveDate, 1,2,3);
        final List<RegionGeometry> commit2Changes = createCommit(commitId2, commit2EffectiveDate, 1,2,3);

        when(regionGeometryGitClientMock.getChangesAfterCommit(eq(null))).thenReturn(commit1Changes);
        when(regionGeometryGitClientMock.getChangesAfterCommit(eq(commitId1))).thenReturn(commit2Changes);

        v3RegionGeometryTestHelper.runUpdateJob(); // update to commit1
        v3RegionGeometryTestHelper.runUpdateJob(); // update to commit2
        regionGeometryDataServiceV1.refreshCache();

        // Id 1 with first effective date
        final RegionGeometryFeatureCollection commit1Area1 =
            regionGeometryDataServiceV1.findAreaLocationRegions(false, false, commit1EffectiveDate, 1);
        assertCollectionSize(1, commit1Area1.getFeatures());
        final RegionGeometryProperties commit1Area1Props =
            commit1Area1.getFeatures().getFirst().getProperties();
        assertEquals(1, commit1Area1Props.locationCode);
        assertEquals(commit1EffectiveDate, commit1Area1Props.effectiveDate);

        // Id 2 with first effective date
        final RegionGeometryFeatureCollection commit2Area1 =
            regionGeometryDataServiceV1.findAreaLocationRegions(false, false, commit2EffectiveDate, 1);
        assertCollectionSize(1, commit2Area1.getFeatures());
        final RegionGeometryProperties commit2Area1Props =
            commit2Area1.getFeatures().getFirst().getProperties();
        assertEquals(1, commit1Area1Props.locationCode);
        assertEquals(commit2EffectiveDate, commit2Area1Props.effectiveDate);

        // All with effective date
        final RegionGeometryFeatureCollection commit2All =
            regionGeometryDataServiceV1.findAreaLocationRegions(false, false, commit2EffectiveDate, null);
        assertCollectionSize(3, commit2All.getFeatures());
        commit2All.getFeatures().forEach(f -> assertEquals(commit2EffectiveDate, f.getProperties().effectiveDate));
    }

    @Test
    public void findAreaLocationRegionsWithUpdateInfo() {
        // Create commit and ask update info
        final Instant effectiveDate = Instant.now();
        final String commitId = RandomStringUtils.secure().nextAlphanumeric(32);
        final List<RegionGeometry> commitChanges = createCommit(commitId, effectiveDate, 1,2);
        when(regionGeometryGitClientMock.getChangesAfterCommit(eq(null))).thenReturn(commitChanges);
        v3RegionGeometryTestHelper.runUpdateJob(); // update to commit1
        regionGeometryDataServiceV1.refreshCache();

        // Id 1 with first effective date
        final RegionGeometryFeatureCollection commitArea =
            regionGeometryDataServiceV1.findAreaLocationRegions(true, false, effectiveDate, null);
        assertTrue(commitArea.getFeatures().isEmpty());
        assertTrue(effectiveDate.minusSeconds(1).isBefore(commitArea.getLastModified()));
        assertTrue(effectiveDate.plusSeconds(1).isAfter(commitArea.getLastModified()));
    }

    @Test
    public void combineGeometriesThatFailedInProdEnv() {
        final RegionGeometry kokkola =
                readRegionGeometry(169, "Kokkola", Instant.parse("2020-01-01T00:00:00Z"), "123",
                        AreaType.MUNICIPALITY);
        final RegionGeometry lestijarvi =
                readRegionGeometry(226, "Lestijärvi", Instant.parse("2020-01-01T00:00:00Z"), "123",
                        AreaType.MUNICIPALITY);
        doReturn(kokkola).when(regionGeometryDataServiceV1)
                .getAreaLocationRegionEffectiveOn(eq(169), any(Instant.class));
        doReturn(lestijarvi).when(regionGeometryDataServiceV1)
                .getAreaLocationRegionEffectiveOn(eq(226), any(Instant.class));
        regionGeometryDataServiceV1.refreshCache(); // Just to coundown the dataPopulationLatch
        final Geometry<?> area = regionGeometryDataServiceV1.getGeoJsonGeometryUnion(Instant.now(), 169, 226);
        assertEquals(Geometry.Type.MultiPolygon, area.getType());
        log.info("Got area: {}", area);

        final Geometry<?> area2 = regionGeometryDataServiceV1.getGeoJsonGeometryUnion(Instant.now(), 226, 169);
        assertEquals(Geometry.Type.MultiPolygon, area2.getType());
        log.info("Got area: {}", area2);
    }

    @Test
    public void combineGeometriesThatFailedInProdEnvGUID50452788() {
        final RegionGeometry heinola = // 00086_Heinola.json
                readRegionGeometry(86, "Heinola", Instant.parse("2025-09-12T00:48:21.998Z"), "123",
                        AreaType.MUNICIPALITY);
        final RegionGeometry porvoo = // 00339_Porvoo.json
                readRegionGeometry(339, "Porvoo", Instant.parse("2025-09-12T00:48:21.998Z"), "123",
                        AreaType.MUNICIPALITY);
        doReturn(heinola).when(regionGeometryDataServiceV1)
                .getAreaLocationRegionEffectiveOn(eq(86), any(Instant.class));
        doReturn(porvoo).when(regionGeometryDataServiceV1)
                .getAreaLocationRegionEffectiveOn(eq(339), any(Instant.class));
        regionGeometryDataServiceV1.refreshCache(); // Just to coundown the dataPopulationLatch
        final Geometry<?> area = regionGeometryDataServiceV1.getGeoJsonGeometryUnion(Instant.now(), 86, 339);
        assertEquals(Geometry.Type.MultiPolygon, area.getType());
        log.info("Got area: {}", area);

        final Geometry<?> area2 = regionGeometryDataServiceV1.getGeoJsonGeometryUnion(Instant.now(), 339, 86);
        assertEquals(Geometry.Type.MultiPolygon, area2.getType());
        log.info("Got area: {}", area2);
    }

    private void assertVersion(final RegionGeometry expected, final RegionGeometry actual) {
        assertEquals(expected.getId(), actual.getId());
    }

    /**
     * Creates commit contents
     */
    private List<RegionGeometry> createCommit(final String commitId1, final Instant effectiveDate, final int...locationCode) {
        return Arrays.stream(locationCode)
            .mapToObj(i -> RegionGeometryTestHelper.createNewRegionGeometry(i, effectiveDate, commitId1))
            .collect(Collectors.toList());
    }
}
