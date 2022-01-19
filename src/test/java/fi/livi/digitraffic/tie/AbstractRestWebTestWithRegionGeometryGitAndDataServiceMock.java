package fi.livi.digitraffic.tie;

import org.springframework.boot.test.mock.mockito.MockBean;

import fi.livi.digitraffic.tie.service.v3.datex2.V3RegionGeometryDataService;

public class AbstractRestWebTestWithRegionGeometryGitAndDataServiceMock extends AbstractRestWebTestWithRegionGeometryGitMock {
    @MockBean
    protected V3RegionGeometryDataService v3RegionGeometryDataServicMock;
}
