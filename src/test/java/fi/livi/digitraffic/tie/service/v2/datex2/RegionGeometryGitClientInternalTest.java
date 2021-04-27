package fi.livi.digitraffic.tie.service.v2.datex2;

import java.util.List;

import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.annotation.Rollback;

import fi.livi.digitraffic.tie.AbstractDaemonTestWithoutS3;
import fi.livi.digitraffic.tie.dao.v3.RegionGeometryRepository;
import fi.livi.digitraffic.tie.model.v3.trafficannouncement.geojson.RegionGeometry;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.v3.datex2.V3RegionGeometryUpdateService;

public class RegionGeometryGitClientInternalTest extends AbstractDaemonTestWithoutS3 {

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
    public void cleanDb() {
//        regionGeometryRepository.deleteAll();

        final V3RegionGeometryUpdateService v3RegionGeometryUpdateService =
            applicationContext.getAutowireCapableBeanFactory().createBean(V3RegionGeometryUpdateService.class);
        v3RegionGeometryTestHelper = new V3RegionGeometryTestHelper(RegionGeometryGitClient, v3RegionGeometryUpdateService,dataStatusService);

    }

    @Ignore("Just for internal testing to test data fetch from GitHub and save to db")
    @Rollback(value = false)
    @Test
    public void updateAreaLocationRegionsFromGithub() {
        v3RegionGeometryTestHelper.runUpdateJob();
    }

    @Ignore("Just for internal testing to fetch all changes for geometries in github")
    @Test
    public void testClient() {
        final List<RegionGeometry> changes =
            RegionGeometryGitClient.getChangesAfterCommit(null);
        changes.forEach(System.out::println);
    }
}
