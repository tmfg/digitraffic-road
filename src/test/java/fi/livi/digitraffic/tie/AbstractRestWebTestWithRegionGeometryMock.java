package fi.livi.digitraffic.tie;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import fi.livi.digitraffic.tie.service.v2.datex2.RegionGeometryGitClient;
import fi.livi.digitraffic.tie.service.v3.datex2.V3RegionGeometryDataService;

public class AbstractRestWebTestWithRegionGeometryMock extends AbstractRestWebTest {

    @MockBean
    protected RegionGeometryGitClient regionGeometryGitClientMock;

    @SpyBean
    protected V3RegionGeometryDataService v3RegionGeometryDataServiceSpy;
}
