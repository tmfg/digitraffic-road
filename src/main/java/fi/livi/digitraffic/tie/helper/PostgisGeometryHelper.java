package fi.livi.digitraffic.tie.helper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;

public class PostgisGeometryHelper {

    public static final PrecisionModel PRECISION_MODEL = new PrecisionModel(10);
    public static final int SRID = 4326; // = WGS84 http://www.epsg-registry.org/
    public static final GeometryFactory GF = new GeometryFactory(PRECISION_MODEL, SRID);
    private static double EARTH_RADIUS_KM = 6371;

    public static Coordinate createCoordinateWithZ(final double x, final double y, final Double z) {
        // PostGIS PointZ can't have null Z-coordinate
        return new Coordinate(x, y, z != null ? z : 0);
    }

    public static Coordinate createCoordinateWithZFromETRS89ToWGS84(double x, double y, Double z) {
        final fi.livi.digitraffic.tie.metadata.geojson.Point wgs84 =
            CoordinateConverter.convertFromETRS89ToWGS84(new fi.livi.digitraffic.tie.metadata.geojson.Point(x, y, z));
        return createCoordinateWithZ(wgs84.getLongitude(), wgs84.getLatitude(), wgs84.getAltitude());
    }


    public static LineString createLineStringWithZ(final List<Coordinate> lineStringCoordinates) {
        if (lineStringCoordinates.size() < 2) {
            throw new IllegalArgumentException("LineString need at least two points, was " + lineStringCoordinates.size());
        }
        return GF.createLineString(lineStringCoordinates.toArray(new Coordinate[0]));
    }

    public static Point createPointWithZ(final Coordinate coordinate) {
        return GF.createPoint(coordinate);
    }

    public static Polygon createSquarePolygonFromMinMax(final double xMin, final double xMax,
                                                 final double yMin, final double yMax) {
        final Coordinate coordinates[] = new Coordinate[] {
            new Coordinate(xMin, yMin, 0), new Coordinate(xMin, yMax, 0),
            new Coordinate(xMax, yMax, 0), new Coordinate(xMax, yMin, 0),
            new Coordinate(xMin, yMin, 0)
        };

        return GF.createPolygon(coordinates);
    }

    public static LineString combineToLinestringWithZ(final Geometry firstGeometry, final Geometry secondGeometry) {
        final Coordinate[] coordinates = ArrayUtils.addAll(firstGeometry.getCoordinates(), secondGeometry.getCoordinates());
        return createLineStringWithZ(Arrays.asList(coordinates));
    }

    public static List<List<Double>> convertToGeoJSONGeometryCoordinates(final LineString lineString) {
        return Arrays.stream(lineString.getCoordinates())
            .map(c -> Arrays.asList(c.getX(), c.getY(), c.getZ()))
            .collect(Collectors.toList());
    }

    public static List<Double> convertToGeoJSONGeometryCoordinates(final Point point) {
        final Coordinate c = point.getCoordinate();
        return Arrays.asList(c.getX(), c.getY(), c.getZ());
    }

    public static fi.livi.digitraffic.tie.metadata.geojson.Geometry convertToGeoJSONGeometry(final Geometry geometry)
        throws IllegalArgumentException {
        if (geometry.getNumPoints() > 1) {
            return new fi.livi.digitraffic.tie.metadata.geojson.LineString(convertToGeoJSONGeometryCoordinates((LineString)geometry));
        } else if (geometry.getNumPoints() == 1) {
            return new fi.livi.digitraffic.tie.metadata.geojson.Point(convertToGeoJSONGeometryCoordinates((Point) geometry));
        }
        throw new IllegalArgumentException("Geometry must be LineString or Point");
    }

    /**
     * Returns the distance between this and given GeoJSON point in kilometers. Doesn't take in account altitude.
     * Based on the following Stack Overflow question:
     * http://stackoverflow.com/questions/27928/calculate-distance-between-two-latitude-longitude-points-haversine-formula,
     * which is based on https://en.wikipedia.org/wiki/Haversine_formula (error rate: ~0.55%).
     */
    public static double distanceBetweenWGS84PointsInKm(final Point from, final Point to) {
        final double fromLon = from.getX();
        final double toLon = to.getX();
        final double fromLat = from.getY();
        final double toLat = to.getY();

        final double diffLat = Math.toRadians(toLat - fromLat);
        final double diffLon = Math.toRadians(toLon - fromLon);

        final double a =
            Math.sin(diffLat / 2) * Math.sin(diffLat / 2) +
                Math.cos(Math.toRadians(fromLat)) * Math.cos(Math.toRadians(toLat)) *
                    Math.sin(diffLon / 2) * Math.sin(diffLon / 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    public static double speedBetweenWGS84PointsInKmH(final Point from, final Point to, final long diffInSeconds) {
        final double distanceKm = distanceBetweenWGS84PointsInKm(from, to);
        final double hours = diffInSeconds / 60.0 / 60.0;
        return distanceKm / hours;
    }
}
