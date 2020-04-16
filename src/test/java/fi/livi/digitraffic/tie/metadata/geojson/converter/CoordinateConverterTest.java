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

    private final double TAMPERE_WGS84_X = 23.761290078;
    private final double TAMPERE_WGS84_Y = 61.497742570;
    private final double TAMPERE_TM35FIN_X = 327630.0;
    private final double TAMPERE_TM35FIN_Y = 6822512.0;
    private final double ALLOWED_DELTA = 0.00002;

    @Test
    public void convertFromETRSToWGS84Values() {
        final Point wgs84 = CoordinateConverter.convertFromETRS89ToWGS84(new Point(TAMPERE_TM35FIN_X, TAMPERE_TM35FIN_Y));
        assertEquals(TAMPERE_WGS84_X, wgs84.getLongitude(), ALLOWED_DELTA);
        assertEquals(TAMPERE_WGS84_Y, wgs84.getLatitude(), ALLOWED_DELTA);
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
