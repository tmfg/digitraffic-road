package fi.livi.digitraffic.tie.service.v2.datex2;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.annotation.Rollback;

import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.dao.v3.RegionGeometryRepository;
import fi.livi.digitraffic.tie.helper.PostgisGeometryHelper;
import fi.livi.digitraffic.tie.model.v3.trafficannouncement.geojson.RegionGeometry;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.v3.datex2.V3RegionGeometryUpdateService;

public class RegionGeometryGitClientInternalTest extends AbstractDaemonTest {

    @Autowired
    private RegionGeometryGitClient RegionGeometryGitClient;
    @Autowired
    private RegionGeometryRepository regionGeometryRepository;
    @Autowired
    private DataStatusService dataStatusService;
    @Autowired
    private GenericApplicationContext applicationContext;

    private V3RegionGeometryTestHelper v3RegionGeometryTestHelper;

    @BeforeEach
    public void init() {
        final V3RegionGeometryUpdateService v3RegionGeometryUpdateService =
            applicationContext.getAutowireCapableBeanFactory().createBean(V3RegionGeometryUpdateService.class);
        v3RegionGeometryTestHelper = new V3RegionGeometryTestHelper(RegionGeometryGitClient, v3RegionGeometryUpdateService,dataStatusService);

    }

    @Disabled("Just for internal testing to test data fetch from GitHub and save to db")
    @Rollback(value = false)
    @Test
    public void updateAreaLocationRegionsFromGithub() {
        regionGeometryRepository.deleteAll();
        v3RegionGeometryTestHelper.runUpdateJob();
    }

    @Disabled("Just for internal testing to fetch all changes for geometries in github")
    @Test
    public void testClient() {
        final List<RegionGeometry> changes =
            RegionGeometryGitClient.getChangesAfterCommit(null);
        changes.forEach(System.out::println);
    }

    @Disabled("Just for internal testing to test geometry union success")
    @Test
    public void testGeometries() {
        final List<RegionGeometry> all = regionGeometryRepository.findAllByOrderByIdAsc();
//        final RegionGeometry locationRaahe = Lists.reverse(all).stream().filter(e -> e.getLocationCode().equals(340)).findFirst().orElseThrow();
//        final RegionGeometry locationHaapajarvi = Lists.reverse(all).stream().filter(e -> e.getLocationCode().equals(56)).findFirst().orElseThrow();
//        PostgisGeometryHelper.union(Arrays.asList(locationRaahe.getGeometry(), locationHaapajarvi.getGeometry()));

        PostgisGeometryHelper.union(all.stream().map(RegionGeometry::getGeometry).collect(Collectors.toList()));
    }


}
