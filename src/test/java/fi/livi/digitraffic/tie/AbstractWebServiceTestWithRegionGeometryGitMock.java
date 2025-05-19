package fi.livi.digitraffic.tie;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import fi.livi.digitraffic.tie.service.trafficmessage.RegionGeometryGitClient;

public class AbstractWebServiceTestWithRegionGeometryGitMock extends AbstractWebServiceTest {

    @MockitoBean
    protected RegionGeometryGitClient regionGeometryGitClientMock;
}
