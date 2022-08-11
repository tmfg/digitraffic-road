package fi.livi.digitraffic.tie;

import org.springframework.boot.test.mock.mockito.MockBean;

import fi.livi.digitraffic.tie.service.v2.datex2.RegionGeometryGitClient;

public class AbstractWebServiceTestWithRegionGeometryGitMock extends AbstractWebServiceTest {

    @MockBean
    protected RegionGeometryGitClient regionGeometryGitClientMock;
}
