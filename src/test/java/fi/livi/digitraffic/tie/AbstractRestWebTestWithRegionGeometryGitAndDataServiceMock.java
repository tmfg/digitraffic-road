package fi.livi.digitraffic.tie;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import fi.livi.digitraffic.tie.model.trafficmessage.RegionGeometry;
import fi.livi.digitraffic.tie.service.trafficmessage.v1.RegionGeometryDataServiceV1;

public class AbstractRestWebTestWithRegionGeometryGitAndDataServiceMock extends AbstractRestWebTest {

    @MockitoBean
    protected RegionGeometryDataServiceV1 regionGeometryDataServiceV1;

    protected void whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(final RegionGeometry regionGeometry) {
        when(regionGeometryDataServiceV1.getAreaLocationRegionEffectiveOn(eq(regionGeometry.getLocationCode()), any())).thenReturn(regionGeometry);
    }

}
