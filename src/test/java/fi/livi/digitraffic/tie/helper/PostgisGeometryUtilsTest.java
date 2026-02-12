package fi.livi.digitraffic.tie.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.metadata.geojson.converter.GeometryJacksonDeserializerTest;

public class PostgisGeometryUtilsTest extends AbstractTest {

    private static final Logger log = LoggerFactory.getLogger(PostgisGeometryUtilsTest.class);
    // Coordinates https://fi.wikipedia.org/wiki/Luettelo_Suomen_kuntien_koordinaateista
    // Distance https://www.nhc.noaa.gov/gccalc.shtml
    private final double TAMPERE_WGS84_X = 23.761290078;
    private final double TAMPERE_WGS84_Y = 61.497742570;
    private final double Z = 6821211;

    private final double KUOPIO_WGS84_X = 27.688935;
    private final double KUOPIO_WGS84_Y = 62.892983;

    @Test
    public void createCoordinateWithZFromETRS89ToWGS84() {
        final double TAMPERE_TM35FIN_X = 327630;
        final double TAMPERE_TM35FIN_Y = 6822512;

        final Coordinate created = PostgisGeometryUtils.createCoordinateWithZFromETRS89ToWGS84(TAMPERE_TM35FIN_X, TAMPERE_TM35FIN_Y, Z);
        checkCoordinate(created, TAMPERE_WGS84_X, TAMPERE_WGS84_Y, Z);
    }

    @Test
    public void createCoordinateWithZ() {
        final Coordinate created = PostgisGeometryUtils.createCoordinateWithZ(TAMPERE_WGS84_X, TAMPERE_WGS84_Y, Z);
        checkCoordinate(created, TAMPERE_WGS84_X, TAMPERE_WGS84_Y, Z);
    }

    @Test
    public void createLineStringWithZ() {
        final double diff_1 = 1.1;
        final double diff_2 = 2.2;
        final ArrayList<Coordinate> coords = new ArrayList<>();
        coords.add(PostgisGeometryUtils.createCoordinateWithZ(TAMPERE_WGS84_X, TAMPERE_WGS84_Y, Z));
        coords.add(PostgisGeometryUtils.createCoordinateWithZ(TAMPERE_WGS84_X + diff_1, TAMPERE_WGS84_Y + diff_1, Z + diff_1));
        coords.add(PostgisGeometryUtils.createCoordinateWithZ(TAMPERE_WGS84_X + diff_2, TAMPERE_WGS84_Y + diff_2, Z + diff_2));

        final LineString lineString = PostgisGeometryUtils.createLineStringWithZ(coords);
        checkCoordinate(lineString.getCoordinateN(0), TAMPERE_WGS84_X, TAMPERE_WGS84_Y, Z);
        checkCoordinate(lineString.getCoordinateN(1), TAMPERE_WGS84_X + diff_1, TAMPERE_WGS84_Y + diff_1, Z + diff_1);
        checkCoordinate(lineString.getCoordinateN(2), TAMPERE_WGS84_X + diff_2, TAMPERE_WGS84_Y + diff_2, Z + diff_2);
    }

    @Test
    public void simplifyRemoveTooClosePoint() {
        final ArrayList<Coordinate> coords = new ArrayList<>();
        coords.add(PostgisGeometryUtils.createCoordinateWithZ(100.0000000, 1.0000000, Z));
        coords.add(PostgisGeometryUtils.createCoordinateWithZ(100.0000001, 1.0000001, Z)); // This will be removed
        coords.add(PostgisGeometryUtils.createCoordinateWithZ(100.0000100, 1.0000100, Z));

        final LineString lineString = PostgisGeometryUtils.createLineStringWithZ(coords);
        final org.locationtech.jts.geom.Geometry simplifiedGeometry = PostgisGeometryUtils.simplify(lineString);
        assertEquals(2, simplifiedGeometry.getNumPoints());
        assertEquals(org.locationtech.jts.geom.Geometry.TYPENAME_LINESTRING, simplifiedGeometry.getGeometryType());
    }

    @Test
    public void simplifyToPointIfTooClose() {
        final ArrayList<Coordinate> coords = new ArrayList<>(); // create a rectangle
        coords.add(PostgisGeometryUtils.createCoordinateWithZ(100.000000, 1.000000, Z));
        coords.add(PostgisGeometryUtils.createCoordinateWithZ(100.000000, 1.000010, Z));
        coords.add(PostgisGeometryUtils.createCoordinateWithZ(100.000010, 1.000010, Z));
        coords.add(PostgisGeometryUtils.createCoordinateWithZ(100.000010, 1.000000, Z));
        coords.add(PostgisGeometryUtils.createCoordinateWithZ(100.000000, 1.000000, Z));

        final LineString lineString = PostgisGeometryUtils.createLineStringWithZ(coords);
        final org.locationtech.jts.geom.Geometry simplifiedGeometry = PostgisGeometryUtils.simplify(lineString);
        assertEquals(1, simplifiedGeometry.getNumPoints());
        assertEquals(org.locationtech.jts.geom.Geometry.TYPENAME_POINT, simplifiedGeometry.getGeometryType());
    }

    @Test
    public void distanceBetweenWGS84PointsInKm() {
        final double TAMPERE_KUOPIO_DISTANCE_KM = 255.8;
        final double ALLOWED_DELTA_IN_KM_H = 1.0;

        final Point tampere =  PostgisGeometryUtils.createPointWithZ(PostgisGeometryUtils.createCoordinateWithZ(TAMPERE_WGS84_X, TAMPERE_WGS84_Y, 0.0));
        final Point kuopio = PostgisGeometryUtils.createPointWithZ(PostgisGeometryUtils.createCoordinateWithZ(KUOPIO_WGS84_X, KUOPIO_WGS84_Y, 0.0));
        final double dist = PostgisGeometryUtils.distanceBetweenWGS84PointsInKm(tampere, kuopio);
        log.info("Calculated distance {} km", dist);
        assertEquals(TAMPERE_KUOPIO_DISTANCE_KM, dist, ALLOWED_DELTA_IN_KM_H);
    }

    @Test
    public void speedBetweenWGS84PointsInKmH() {
        // 100 km/h
        // 256.82 km / 100 km/h = 2,5682 h
        final int TAMPERE_KUOPIO_SECONDS_WITH_SPEED_100_KM_H = (int) (2.5682 * 60 * 60);
        final double ALLOWED_DELTA_IN_KM = 0.5;

        final Point tampere =  PostgisGeometryUtils.createPointWithZ(PostgisGeometryUtils.createCoordinateWithZ(TAMPERE_WGS84_X, TAMPERE_WGS84_Y, 0.0));
        final Point kuopio = PostgisGeometryUtils.createPointWithZ(PostgisGeometryUtils.createCoordinateWithZ(KUOPIO_WGS84_X, KUOPIO_WGS84_Y, 0.0));
        final double speed = PostgisGeometryUtils.speedBetweenWGS84PointsInKmH(tampere, kuopio, TAMPERE_KUOPIO_SECONDS_WITH_SPEED_100_KM_H);
        log.info("Calculated speed {} km/h", speed);
        assertEquals(100.0, speed, ALLOWED_DELTA_IN_KM);
    }

    @Test
    public void point() {
        final fi.livi.digitraffic.tie.metadata.geojson.Geometry<?> geom =
            PostgisGeometryUtils.convertGeoJSONStringToGeoJSON(GeometryJacksonDeserializerTest.POINT);
        assertNotNull(geom);
        assertFalse(geom.getCoordinates().isEmpty());
        assertEquals(Geometry.Type.Point, geom.getType());
        log.info(geom.toString());
    }

    @Test
    public void lineString() {
        final fi.livi.digitraffic.tie.metadata.geojson.Geometry<?> geom =
            PostgisGeometryUtils.convertGeoJSONStringToGeoJSON(GeometryJacksonDeserializerTest.LINE_STRING);
        assertNotNull(geom);
        assertFalse(geom.getCoordinates().isEmpty());
        assertEquals(Geometry.Type.LineString, geom.getType());
        log.info(geom.toString());
    }

    @Test
    public void multiLineString() {
        final fi.livi.digitraffic.tie.metadata.geojson.Geometry<?> geom =
            PostgisGeometryUtils.convertGeoJSONStringToGeoJSON(GeometryJacksonDeserializerTest.MULTI_LINE_STRING);
        assertNotNull(geom);
        assertFalse(geom.getCoordinates().isEmpty());
        assertEquals(Geometry.Type.MultiLineString, geom.getType());
        log.info(geom.toString());
    }

    @Test
    public void polygon() {
        final fi.livi.digitraffic.tie.metadata.geojson.Geometry<?> geom =
            PostgisGeometryUtils.convertGeoJSONStringToGeoJSON(GeometryJacksonDeserializerTest.POLYGON);
        assertNotNull(geom);
        assertFalse(geom.getCoordinates().isEmpty());
        assertEquals(Geometry.Type.Polygon, geom.getType());
        log.info(geom.toString());
    }

    @Test
    public void polygonWithInternalPolygon() {
        final fi.livi.digitraffic.tie.metadata.geojson.Geometry<?> geom =
            PostgisGeometryUtils.convertGeoJSONStringToGeoJSON(GeometryJacksonDeserializerTest.POLYGON_WITH_INTERNAL_POLYGON);
        assertNotNull(geom);
        assertFalse(geom.getCoordinates().isEmpty());
        assertEquals(Geometry.Type.Polygon, geom.getType());
        log.info(geom.toString());
    }

    @Test
    public void multiPoint() {
        final fi.livi.digitraffic.tie.metadata.geojson.Geometry<?> geom =
            PostgisGeometryUtils.convertGeoJSONStringToGeoJSON(GeometryJacksonDeserializerTest.MULTI_POINT);
        assertNotNull(geom);
        assertFalse(geom.getCoordinates().isEmpty());
        assertEquals(Geometry.Type.MultiPoint, geom.getType());
        log.info(geom.toString());
    }

    @Test
    public void multiPolygon() {
        final fi.livi.digitraffic.tie.metadata.geojson.Geometry<?> geom =
            PostgisGeometryUtils.convertGeoJSONStringToGeoJSON(GeometryJacksonDeserializerTest.MULTI_POLYGON);
        assertNotNull(geom);
        assertFalse(geom.getCoordinates().isEmpty());
        assertEquals(Geometry.Type.MultiPolygon, geom.getType());
        log.info(geom.toString());
    }

    @Test
    public void multiPolygonWithInnerPolygon() {
        final fi.livi.digitraffic.tie.metadata.geojson.Geometry<?> geom =
            PostgisGeometryUtils.convertGeoJSONStringToGeoJSON(GeometryJacksonDeserializerTest.MULTI_POLYGON_WITH_INNER_POLYGON);
        assertNotNull(geom);
        assertFalse(geom.getCoordinates().isEmpty());
        assertEquals(Geometry.Type.MultiPolygon, geom.getType());
        log.info(geom.toString());
    }

    @Test
    public void checkTypeIn() {
        final Point point =  PostgisGeometryUtils.createPointWithZ(PostgisGeometryUtils.createCoordinateWithZ(TAMPERE_WGS84_X, TAMPERE_WGS84_Y, 0.0));
        final ArrayList<Coordinate> coords = new ArrayList<>();
        coords.add(PostgisGeometryUtils.createCoordinateWithZ(TAMPERE_WGS84_X, TAMPERE_WGS84_Y, Z));
        coords.add(PostgisGeometryUtils.createCoordinateWithZ(TAMPERE_WGS84_X + 10, TAMPERE_WGS84_Y + 10, Z + 10));
        final LineString lineString = PostgisGeometryUtils.createLineStringWithZ(coords);

        assertThrows(IllegalArgumentException.class, () -> PostgisGeometryUtils.checkTypeInAndReturn(point, PostgisGeometryUtils.GeometryType.LINESTRING));

        assertThrows(IllegalArgumentException.class, () -> PostgisGeometryUtils.checkTypeInAndReturn(lineString, PostgisGeometryUtils.GeometryType.POINT));

        assertThrows(IllegalArgumentException.class, () -> PostgisGeometryUtils.checkTypeInAndReturn(lineString, (PostgisGeometryUtils.GeometryType)null));
        assertThrows(IllegalArgumentException.class, () -> PostgisGeometryUtils.checkTypeInAndReturn(lineString,
                (PostgisGeometryUtils.GeometryType) null));

        assertEquals(point, PostgisGeometryUtils.checkTypeInAndReturn(point));
        assertEquals(point, PostgisGeometryUtils.checkTypeInAndReturn(point, PostgisGeometryUtils.GeometryType.POINT));
        assertEquals(point, PostgisGeometryUtils.checkTypeInAndReturn(point, PostgisGeometryUtils.GeometryType.LINESTRING, PostgisGeometryUtils.GeometryType.POINT));
        assertEquals(lineString, PostgisGeometryUtils.checkTypeInAndReturn(lineString, PostgisGeometryUtils.GeometryType.LINESTRING, PostgisGeometryUtils.GeometryType.POINT));

    }

    @Test
    public void snapToGrid() throws ParseException {
        final org.locationtech.jts.geom.Geometry geometry =
            PostgisGeometryUtils.convertWktToGeomety("LINESTRING(1.1234568 2.1234568, 1.1234567 2.1234567, 1.1234667 2.1234667)");
        final org.locationtech.jts.geom.Geometry result = PostgisGeometryUtils.snapToGrid(geometry);
        final org.locationtech.jts.geom.Geometry expected =
            PostgisGeometryUtils.convertWktToGeomety("LINESTRING(1.123457 2.123457, 1.123467 2.123467)");
        log.info("snapToGrid {} -> {}, expected: {}", geometry, result, expected);

        assertEquals(expected, result);
    }

    @Test
    public void snapToGridResultEmptyLinestringReturnsPoint() throws ParseException {
        // Sql equivivalents
        // st_astext(ST_Snaptogrid(ST_GeomFromText('LINESTRING(1.1234568 2.1234568, 1.1234567 2.1234567, 1.1234667 2.1234667)'), 0.000001)) AS lineStringJson,
        // st_astext(ST_Snaptogrid(ST_GeomFromText('POINT(1.1234568 2.1234568)'), 0.000001)) AS pointJson
        final org.locationtech.jts.geom.Geometry geometry =
            PostgisGeometryUtils.convertWktToGeomety("LINESTRING(1.1234568 2.1234568, 1.1234567 2.1234567, 1.1234570 2.1234574)");
        final org.locationtech.jts.geom.Geometry result = PostgisGeometryUtils.snapToGrid(geometry);

        final org.locationtech.jts.geom.Geometry expected =
            PostgisGeometryUtils.convertWktToGeomety("POINT(1.123457 2.123457)");
        log.info("snapToGrid {} -> {}, expected: {}", geometry, result, expected);
        assertEquals(expected, result);
    }



    private void checkCoordinate(final Coordinate coordinate, final double x, final double y, final double z) {
        final double ALLOWED_COORDINATE_DELTA = 0.00002;
        assertEquals(x, coordinate.getX(), ALLOWED_COORDINATE_DELTA);
        assertEquals(y, coordinate.getY(), ALLOWED_COORDINATE_DELTA);
        assertEquals(z, coordinate.getZ(), ALLOWED_COORDINATE_DELTA);
    }
}
