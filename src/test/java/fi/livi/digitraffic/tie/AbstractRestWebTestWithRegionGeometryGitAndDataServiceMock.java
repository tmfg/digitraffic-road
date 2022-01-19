package fi.livi.digitraffic.tie;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.springframework.boot.test.mock.mockito.MockBean;

import fi.livi.digitraffic.tie.model.v3.trafficannouncement.geojson.RegionGeometry;
import fi.livi.digitraffic.tie.service.v3.datex2.V3RegionGeometryDataService;

public class AbstractRestWebTestWithRegionGeometryGitAndDataServiceMock extends AbstractRestWebTestWithRegionGeometryGitMock {

    @MockBean
    protected V3RegionGeometryDataService v3RegionGeometryDataServicMock;

    protected void whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(final RegionGeometry regionGeometry) {
        when(v3RegionGeometryDataServicMock.getAreaLocationRegionEffectiveOn(eq(regionGeometry.getLocationCode()), any())).thenReturn(regionGeometry);
    }

}
