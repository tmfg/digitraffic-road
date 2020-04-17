package fi.livi.digitraffic.tie.metadata.geojson.converter;

import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper.RANGE_X_MAX;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper.RANGE_X_MIN;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper.RANGE_Y_MAX;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper.RANGE_Y_MIN;
import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import fi.livi.digitraffic.tie.metadata.geojson.Point;

public class CoordinateConverterTest {

    @Test
    public void convertFromETRSToWGS84Succeeds() {
        assertCoordinates(CoordinateConverter.convertFromETRS89ToWGS84(new Point(386261, 6686131)));
    }

    // Reference system https://www.retkikartta.fi/
    private void assertCoordinates(final Point point) {
        assertEquals(24.9422, point.getLongitude(), 0.0001);
        assertEquals(60.2958, point.getLatitude(), 0.0001);
    }

    @Ignore
    @Test
    public void justConvertForTesting() {
        convertFromWGS84ToETRS89(339803.0, 6818579.0);
        convertFromWGS84ToETRS89(RANGE_X_MIN, RANGE_Y_MIN);
        convertFromWGS84ToETRS89(RANGE_X_MAX, RANGE_Y_MAX);
    }

    private void convertFromWGS84ToETRS89(double x, double y) {
        final Point from = new Point(x, y);
        final Point to = CoordinateConverter.convertFromWGS84ToETRS89(from);
        System.out.println("From: " + from + "\nTo:   " + to);

    }
}
