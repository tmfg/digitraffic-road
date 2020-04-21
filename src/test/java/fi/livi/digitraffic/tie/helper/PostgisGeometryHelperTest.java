package fi.livi.digitraffic.tie.helper;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.livi.digitraffic.tie.AbstractTest;

@RunWith(JUnit4.class)
public class PostgisGeometryHelperTest extends AbstractTest {

    private static final Logger log = LoggerFactory.getLogger(PostgisGeometryHelperTest.class);
    // Coordinates https://fi.wikipedia.org/wiki/Luettelo_Suomen_kuntien_koordinaateista
    // Distance https://www.nhc.noaa.gov/gccalc.shtml
    private final double TAMPERE_WGS84_X = 23.761290078;
    private final double TAMPERE_WGS84_Y = 61.497742570;
    private final double TAMPERE_TM35FIN_X = 327630;
    private final double TAMPERE_TM35FIN_Y = 6822512;
    private final double Z = 6821211;
    private final double ALLOWED_COORDINATE_DELTA = 0.00002;

    private final double KUOPIO_WGS84_X = 27.688935;
    private final double KUOPIO_WGS84_Y = 62.892983;
    private final double TAMPERE_KUOPIO_DISTANCE_KM = 255.8;
    private final double ALLOWED_DELTA_IN_KM = 0.5;
    private final double ALLOWED_DELTA_IN_KM_H = 1.0;
    private final int TAMPERE_KUOPIO_SECONDS_WITH_SPEED_100_KM_H = (int)(2.5682 * 60 * 60); // 256.82 km / 100 km/h = 2,5682 h

    @Test
    public void createCoordinateWithZFromETRS89ToWGS84() {
        final Coordinate created = PostgisGeometryHelper.createCoordinateWithZFromETRS89ToWGS84(TAMPERE_TM35FIN_X, TAMPERE_TM35FIN_Y, Z);
        checkCoordinate(created, TAMPERE_WGS84_X, TAMPERE_WGS84_Y, Z);
    }

    @Test
    public void createCoordinateWithZ() {
        final Coordinate created = PostgisGeometryHelper.createCoordinateWithZ(TAMPERE_WGS84_X, TAMPERE_WGS84_Y, Z);
        checkCoordinate(created, TAMPERE_WGS84_X, TAMPERE_WGS84_Y, Z);
    }

    @Test
    public void createLineStringWithZ() {
        final double diff_1 = 1.1;
        final double diff_2 = 2.2;
        final ArrayList<Coordinate> coords = new ArrayList<>();
        coords.add(PostgisGeometryHelper.createCoordinateWithZ(TAMPERE_WGS84_X, TAMPERE_WGS84_Y, Z));
        coords.add(PostgisGeometryHelper.createCoordinateWithZ(TAMPERE_WGS84_X + diff_1, TAMPERE_WGS84_Y + diff_1, Z + diff_1));
        coords.add(PostgisGeometryHelper.createCoordinateWithZ(TAMPERE_WGS84_X + diff_2, TAMPERE_WGS84_Y + diff_2, Z + diff_2));

        final LineString lineString = PostgisGeometryHelper.createLineStringWithZ(coords);
        checkCoordinate(lineString.getCoordinateN(0), TAMPERE_WGS84_X, TAMPERE_WGS84_Y, Z);
        checkCoordinate(lineString.getCoordinateN(1), TAMPERE_WGS84_X + diff_1, TAMPERE_WGS84_Y + diff_1, Z + diff_1);
        checkCoordinate(lineString.getCoordinateN(2), TAMPERE_WGS84_X + diff_2, TAMPERE_WGS84_Y + diff_2, Z + diff_2);
    }

    @Test
    public void distanceBetweenWGS84PointsInKm() {
        final Point tampere =  PostgisGeometryHelper.createPointWithZ(PostgisGeometryHelper.createCoordinateWithZ(TAMPERE_WGS84_X, TAMPERE_WGS84_Y, 0.0));
        final Point kuopio = PostgisGeometryHelper.createPointWithZ(PostgisGeometryHelper.createCoordinateWithZ(KUOPIO_WGS84_X, KUOPIO_WGS84_Y, 0.0));
        double dist = PostgisGeometryHelper.distanceBetweenWGS84PointsInKm(tampere, kuopio);
        log.info("Calculated distance {} km", dist);
        assertEquals(TAMPERE_KUOPIO_DISTANCE_KM, dist, ALLOWED_DELTA_IN_KM_H);
    }

    @Test
    public void speedBetweenWGS84PointsInKmH() {
        // 100 km/h
        final Point tampere =  PostgisGeometryHelper.createPointWithZ(PostgisGeometryHelper.createCoordinateWithZ(TAMPERE_WGS84_X, TAMPERE_WGS84_Y, 0.0));
        final Point kuopio = PostgisGeometryHelper.createPointWithZ(PostgisGeometryHelper.createCoordinateWithZ(KUOPIO_WGS84_X, KUOPIO_WGS84_Y, 0.0));
        double speed = PostgisGeometryHelper.speedBetweenWGS84PointsInKmH(tampere, kuopio, TAMPERE_KUOPIO_SECONDS_WITH_SPEED_100_KM_H);
        log.info("Calculated speed {} km/h", speed);
        assertEquals(100.0, speed, ALLOWED_DELTA_IN_KM);
    }

    private void checkCoordinate(Coordinate coordinate, final double x, final double y, final double z) {
        assertEquals(x, coordinate.getX(), ALLOWED_COORDINATE_DELTA);
        assertEquals(y, coordinate.getY(), ALLOWED_COORDINATE_DELTA);
        assertEquals(z, coordinate.getZ(), ALLOWED_COORDINATE_DELTA);
    }
}
