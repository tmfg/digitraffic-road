package fi.livi.digitraffic.tie.service.v2.datex2;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import fi.livi.digitraffic.tie.AbstractDaemonTestWithoutS3;
import fi.livi.digitraffic.tie.dao.v3.RegionGeometryRepository;
import fi.livi.digitraffic.tie.model.v3.trafficannouncement.geojson.RegionGeometry;
import fi.livi.digitraffic.tie.service.v3.datex2.V3RegionGeometryUpdateService;

public class RegionGeometryGitClientInternalTest extends AbstractDaemonTestWithoutS3 {

    @Autowired
    private RegionGeometryGitClient RegionGeometryGitClient;

    @Autowired
    private V3RegionGeometryUpdateService v3RegionGeometryUpdateService;

    @Autowired
    private RegionGeometryRepository regionGeometryRepository;

    @Before
    public void cleanDb() {
//        regionGeometryRepository.deleteAll();
    }

    @Ignore("Just for internal testing to test data fetch from GitHub and save to db")
    @Rollback(value = false)
    @Test
    public void updateAreaLocationRegionsFromGithub() {
        v3RegionGeometryUpdateService.updateAreaLocationRegions();
    }

    @Ignore("Just for internal testing to fetch all changes for geometries in github")
    @Test
    public void testClient() {
        final List<RegionGeometry> changes =
            RegionGeometryGitClient.getChangesAfterCommit(null);
        changes.forEach(System.out::println);
    }
}
