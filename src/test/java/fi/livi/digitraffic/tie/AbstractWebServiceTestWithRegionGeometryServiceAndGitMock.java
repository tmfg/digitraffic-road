package fi.livi.digitraffic.tie;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;

import fi.livi.digitraffic.tie.model.trafficmessage.RegionGeometry;
import fi.livi.digitraffic.tie.service.trafficmessage.v1.RegionGeometryDataServiceV1;

public class AbstractWebServiceTestWithRegionGeometryServiceAndGitMock extends AbstractWebServiceTestWithRegionGeometryGitMock {

    @MockBean
    protected RegionGeometryDataServiceV1 regionGeometryDataServiceV1;

    @BeforeEach
    public void init() {
        doNothing().when(regionGeometryDataServiceV1).awaitDataPopulation();
    }
    protected void whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(final RegionGeometry regionGeometry) {
        when(regionGeometryDataServiceV1.getAreaLocationRegionEffectiveOn(eq(regionGeometry.getLocationCode()), any())).thenReturn(regionGeometry);
    }

    protected void assertFeature(final TrafficAnnouncementFeature feature, final String expectedType, final int expectedCount) {
        assertEquals(expectedType, feature.getGeometry().getType().toString());
        assertEquals(expectedCount, feature.getGeometry().getCoordinates().size());
    }

    protected void assertMultiLineString(final TrafficAnnouncementFeature feature, final int expectedCount) {
        assertFeature(feature, "MultiLineString", expectedCount);
    }
}
