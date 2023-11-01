package fi.livi.digitraffic.tie.service.trafficmessage;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.annotation.Rollback;

import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.dao.trafficmessage.RegionGeometryRepository;
import fi.livi.digitraffic.tie.helper.PostgisGeometryUtils;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.trafficmessage.RegionGeometry;
import fi.livi.digitraffic.tie.service.DataStatusService;

public class RegionGeometryGitClientInternalTest extends AbstractDaemonTest {
    private static final Logger log = LoggerFactory.getLogger(RegionGeometryGitClientInternalTest.class);

    @Autowired
    private fi.livi.digitraffic.tie.service.trafficmessage.RegionGeometryGitClient RegionGeometryGitClient;
    @Autowired
    private RegionGeometryRepository regionGeometryRepository;
    @Autowired
    private DataStatusService dataStatusService;
    @Autowired
    private GenericApplicationContext applicationContext;

    private RegionGeometryUpdateJobTestHelper v3RegionGeometryTestHelper;

    @BeforeEach
    public void init() {
        final RegionGeometryUpdateService regionGeometryUpdateService =
            applicationContext.getAutowireCapableBeanFactory().createBean(RegionGeometryUpdateService.class);
        v3RegionGeometryTestHelper = new RegionGeometryUpdateJobTestHelper(RegionGeometryGitClient, regionGeometryUpdateService,dataStatusService);

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
        changes.forEach(c -> log.info(ToStringHelper.toStringFull(c)));
    }

    @Disabled("Just for internal testing to test geometry union success")
    @Test
    public void testGeometries() {
        final List<RegionGeometry> all = regionGeometryRepository.findAllByOrderByIdAsc();
//        final RegionGeometry locationRaahe = Lists.reverse(all).stream().filter(e -> e.getLocationCode().equals(340)).findFirst().orElseThrow();
//        final RegionGeometry locationHaapajarvi = Lists.reverse(all).stream().filter(e -> e.getLocationCode().equals(56)).findFirst().orElseThrow();
//        PostgisGeometryHelper.union(Arrays.asList(locationRaahe.getGeometry(), locationHaapajarvi.getGeometry()));

        PostgisGeometryUtils.union(all.stream().map(RegionGeometry::getGeometry).collect(Collectors.toList()));
    }


}
