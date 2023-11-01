package fi.livi.digitraffic.tie;

import org.springframework.boot.test.mock.mockito.MockBean;

import fi.livi.digitraffic.tie.service.trafficmessage.RegionGeometryGitClient;

public class AbstractWebServiceTestWithRegionGeometryGitMock extends AbstractWebServiceTest {

    @MockBean
    protected RegionGeometryGitClient regionGeometryGitClientMock;
}
