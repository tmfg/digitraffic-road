package fi.livi.digitraffic.tie.metadata.geojson.converter;

import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.RANGE_X_MAX;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.RANGE_X_MIN;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.RANGE_Y_MAX;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.RANGE_Y_MIN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.livi.digitraffic.tie.metadata.geojson.Point;

public class CoordinateConverterTest {
    private static final Logger log = LoggerFactory.getLogger(CoordinateConverterTest.class);

    private final double TAMPERE_WGS84_X = 23.761290078;
    private final double TAMPERE_WGS84_Y = 61.497742570;
    private final double TAMPERE_TM35FIN_X = 327630.0;
    private final double TAMPERE_TM35FIN_Y = 6822512.0;

    private final double JKL_WGS84_X = 25.77764;
    private final double JKL_WGS84_Y = 62.23864;
    private final double JKL_TM35FIN_X = 436480.0;
    private final double JKL_TM35FIN_Y = 6901365.0;

    private final double OULU_WGS84_X = 25.52143;
    private final double OULU_WGS84_Y = 65.02124;
    private final double OULU_TM35FIN_X = 430336.0;
    private final double OULU_TM35FIN_Y = 7211637.0;

    private final double UTSJOKI_WGS84_X = 27.03920;
    private final double UTSJOKI_WGS84_Y = 69.89214;
    private final double UTSJOKI_TM35FIN_X = 501504.0;
    private final double UTSJOKI_TM35FIN_Y = 7753845.0;

    private final double ALLOWED_DELTA_WGS84 = 0.00002;
    private final double ALLOWED_DELTA_TM35FIN = 1.2;

    @Test
    public void convertFromETRSToWGS84Values() {
        final Point wgs84 = CoordinateConverter.convertFromETRS89ToWGS84(new Point(TAMPERE_TM35FIN_X, TAMPERE_TM35FIN_Y));
        assertEquals(TAMPERE_WGS84_X, wgs84.getLongitude(), ALLOWED_DELTA_WGS84);
        assertEquals(TAMPERE_WGS84_Y, wgs84.getLatitude(), ALLOWED_DELTA_WGS84);
    }

    @Test
    public void convertFromWGS84ToETRS89Values() {
        final Point tm35fin = CoordinateConverter.convertFromWGS84ToETRS89(new Point(TAMPERE_WGS84_X, TAMPERE_WGS84_Y));
        assertEquals(TAMPERE_TM35FIN_X, tm35fin.getLongitude(), ALLOWED_DELTA_TM35FIN);
        assertEquals(TAMPERE_TM35FIN_Y, tm35fin.getLatitude(), ALLOWED_DELTA_TM35FIN);
    }

    @Test
    public void threadSafety() throws InterruptedException {
        final AtomicBoolean fail = new AtomicBoolean(false);
        final Set<Thread> threads =  new HashSet<>();
        threads.add(startThread(TAMPERE_TM35FIN_X, TAMPERE_TM35FIN_Y, TAMPERE_WGS84_X, TAMPERE_WGS84_Y, fail));
        threads.add(startThread(JKL_TM35FIN_X, JKL_TM35FIN_Y, JKL_WGS84_X, JKL_WGS84_Y, fail));
        threads.add(startThread(OULU_TM35FIN_X, OULU_TM35FIN_Y, OULU_WGS84_X, OULU_WGS84_Y, fail));
        threads.add(startThread(UTSJOKI_TM35FIN_X, UTSJOKI_TM35FIN_Y, UTSJOKI_WGS84_X, UTSJOKI_WGS84_Y, fail));

        threads.add(startThread(TAMPERE_TM35FIN_X, TAMPERE_TM35FIN_Y, TAMPERE_WGS84_X, TAMPERE_WGS84_Y, fail));
        threads.add(startThread(JKL_TM35FIN_X, JKL_TM35FIN_Y, JKL_WGS84_X, JKL_WGS84_Y, fail));
        threads.add(startThread(OULU_TM35FIN_X, OULU_TM35FIN_Y, OULU_WGS84_X, OULU_WGS84_Y, fail));
        threads.add(startThread(UTSJOKI_TM35FIN_X, UTSJOKI_TM35FIN_Y, UTSJOKI_WGS84_X, UTSJOKI_WGS84_Y, fail));

        while (threads.stream().mapToInt(t -> t.isAlive() ? 1 : 0).sum() > 0) {
            Thread.sleep(100);
        }
        assertFalse(fail.get());
    }

    private Thread startThread(final double tm35FIN_x, final double tm35FIN_y, final double wgs84_x, final double wgs84_y,
                               final AtomicBoolean fail) {
        final Runnable runnable = () ->
            {
                log.info("Start thread {}", Thread.currentThread().getId());
                int i = 1;

                while (i < 1001 && !fail.get()) {
                    final Point tgtWGS84 = CoordinateConverter.convertFromETRS89ToWGS84(new Point(tm35FIN_x, tm35FIN_y, 0.0));
                    final Point tgtETRS89 = CoordinateConverter.convertFromWGS84ToETRS89(new Point(wgs84_x, wgs84_y, 0.0));
                    if ( doubleIsDifferent(wgs84_x, tgtWGS84.getX(), ALLOWED_DELTA_WGS84) ) {
                        fail.set(true);
                    }
                    if ( doubleIsDifferent(wgs84_y, tgtWGS84.getY(), ALLOWED_DELTA_WGS84) ) {
                        fail.set(true);
                    }
                    if ( doubleIsDifferent(tm35FIN_x, tgtETRS89.getX(), ALLOWED_DELTA_TM35FIN) ) {
                        fail.set(true);
                    }
                    if ( doubleIsDifferent(tm35FIN_y, tgtETRS89.getY(), ALLOWED_DELTA_TM35FIN) ) {
                        fail.set(true);
                    }
                    i++;
                }
                log.info("End thread {}", Thread.currentThread().getId());
            };
        Thread thread = new Thread(runnable);
        thread.start();
        return thread;
    }

    static private boolean doubleIsDifferent(final double d1, final double d2, final double delta) {
        if (Double.compare(d1, d2) == 0) {
            return false;
        }
        if ((Math.abs(d1 - d2) <= delta)) {
            return false;
        }
        log.error("Values not equal {} and {} with delta {} on thread {}", d1, d2, delta, Thread.currentThread().getId());
        return true;
    }

    @Disabled
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
