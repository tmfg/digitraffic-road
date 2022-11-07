package fi.livi.digitraffic.tie.helper;

import static fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingRepository.SIMPLIFY_DOUGLAS_PEUCKER_TOLERANCE;
import static fi.livi.digitraffic.tie.helper.GeometryConstants.JTS_GEOMETRY_FACTORY;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;

/**
 * Uses 6 digits precision for coordinates. That gives about 5 cm accuracy at Finland (lat 60°)
 * @see <a href="https://en.wikipedia.org/wiki/Wikipedia:WikiProject_Geographical_coordinates#Precision">Wikipedia:WikiProject Geographical coordinates - Precision</a>
 */
public class PostgisGeometryUtils {

    private static final Logger log = LoggerFactory.getLogger(PostgisGeometryUtils.class);

    private static final ThreadLocal<GeoJsonWriter> geoJsonWriter =
        ThreadLocal.withInitial(() -> {
            final GeoJsonWriter writer = new GeoJsonWriter(GeometryConstants.COORDINATE_DECIMALS_6_DIGITS);
            writer.setEncodeCRS(false); // We use always EPSG:4326 = WGS84 - World Geodetic System 1984
            return writer;
        });

    private static final ThreadLocal<GeoJsonReader> geoJsonReader =
        ThreadLocal.withInitial(() -> new GeoJsonReader(JTS_GEOMETRY_FACTORY));

    private static final ThreadLocal<WKBReader> wkbGeometryReader =
        ThreadLocal.withInitial(() -> new WKBReader(JTS_GEOMETRY_FACTORY));

    private static final ThreadLocal<WKTReader> wktGeometryReader =
        ThreadLocal.withInitial(() -> new WKTReader(JTS_GEOMETRY_FACTORY));

    private static final ObjectReader dtGeoJsonReader = new ObjectMapper().readerFor(fi.livi.digitraffic.tie.metadata.geojson.Geometry.class);

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
        lineStringCoordinates.forEach(c -> {
            if (!Double.isFinite(c.getZ())) {
                c.setZ(0.0);
            }
        });
        return JTS_GEOMETRY_FACTORY.createLineString(lineStringCoordinates.toArray(new Coordinate[0]));
    }

    public static Point createPointWithZ(final Coordinate coordinate) {
        return JTS_GEOMETRY_FACTORY.createPoint(coordinate);
    }

    public static Polygon createSquarePolygonFromMinMax(final double xMin, final double xMax,
                                                        final double yMin, final double yMax) {
        final Coordinate[] coordinates = new Coordinate[] {
            new Coordinate(xMin, yMin, 0), new Coordinate(xMin, yMax, 0),
            new Coordinate(xMax, yMax, 0), new Coordinate(xMax, yMin, 0),
            new Coordinate(xMin, yMin, 0)
        };

        return JTS_GEOMETRY_FACTORY.createPolygon(coordinates);
    }

    public static LineString combineToLinestringWithZ(final Geometry firstGeometry, final Geometry secondGeometry) {
        final Coordinate[] coordinates = ArrayUtils.addAll(firstGeometry.getCoordinates(), secondGeometry.getCoordinates());
        return createLineStringWithZ(Arrays.asList(coordinates));
    }

    public static List<List<Double>> convertToGeoJSONGeometryCoordinates(final LineString lineString) {
        return Arrays.stream(lineString.getCoordinates())
            .map(c -> {
                if (Double.isFinite(c.getZ())) {
                    return Arrays.asList(c.getX(), c.getY(), c.getZ());
                }
                return Arrays.asList(c.getX(), c.getY());
            })
            .collect(Collectors.toList());
    }

    public static List<Double> convertToGeoJSONGeometryCoordinates(final Point point) {
        final Coordinate c = point.getCoordinate();
        return Arrays.asList(c.getX(), c.getY(), c.getZ());
    }

    public static fi.livi.digitraffic.tie.metadata.geojson.LineString convertToGeoJSONLineString(final Geometry geometry)
        throws IllegalArgumentException {
        if (geometry.getGeometryType().equals(Geometry.TYPENAME_LINESTRING)) {
            return convertGeometryToGeoJSONGeometry(geometry);
        }
        throw new IllegalArgumentException("Geometry must be LineString, but was " + geometry.getGeometryType() + ". Had coordinate count of " + geometry.getNumPoints());
    }

    public static fi.livi.digitraffic.tie.metadata.geojson.MultiLineString convertToGeoJSONMultiLineLineString(final Geometry geometry)
        throws IllegalArgumentException {
        if (geometry.getGeometryType().equals(Geometry.TYPENAME_MULTILINESTRING)) {
            return convertGeometryToGeoJSONGeometry(geometry);
        }
        throw new IllegalArgumentException("Geometry must be MultiLineString, but was " + geometry.getGeometryType() + ". Had coordinate count of " + geometry.getNumPoints());
    }

    public static <T extends fi.livi.digitraffic.tie.metadata.geojson.Geometry<?>> T convertGeometryToGeoJSONGeometry(final org.locationtech.jts.geom.Geometry geometry) {
        final String geoJson = geoJsonWriter.get().write(geometry);
        return convertGeoJSONStringToGeoJSON(geoJson);
    }

    public static <T extends fi.livi.digitraffic.tie.metadata.geojson.Geometry<?>> T convertGeoJSONStringToGeoJSON(final String geoJsonString) {
        try {
            return dtGeoJsonReader.readValue(geoJsonString);
        } catch (final JsonProcessingException e) {
            log.error(MessageFormat.format("method=convertFromGeoJSONStringToGeoJSON Failed to convert {0} to GeoJSON", geoJsonString), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the distance between this and given GeoJSON point in kilometers. Doesn't take in account altitude.
     * Based on the following Stack Overflow question:
     * <a href="http://stackoverflow.com/questions/27928/calculate-distance-between-two-latitude-longitude-points-haversine-formula">
     * Stackoverflow: Calculate distance between two latitude-longitude points? (Haversine formula)</a>,
     * which is based on <a href="https://en.wikipedia.org/wiki/Haversine_formula">Wikipedia: Haversine formula</a> (error rate: ~0.55%).
     */
    public static double distanceBetweenWGS84PointsInKm(final Point from, final Point to) {
        return distanceBetweenWGS84PointsInKm(from.getX(), from.getY(), to.getX(), to.getY());
    }

    public static double distanceBetweenWGS84PointsInKm(final Coordinate from, final Coordinate to) {
        return distanceBetweenWGS84PointsInKm(from.getX(), from.getY(), to.getX(), to.getY());
    }

    public static double distanceBetweenWGS84PointsInKm(final double fromXLon, final double fromYLat, final double toXLon, final double toYLat) {

        final double diffLat = Math.toRadians(toYLat - fromYLat);
        final double diffLon = Math.toRadians(toXLon - fromXLon);

        final double a =
            Math.sin(diffLat / 2) * Math.sin(diffLat / 2) +
                Math.cos(Math.toRadians(fromYLat)) * Math.cos(Math.toRadians(toYLat)) *
                    Math.sin(diffLon / 2) * Math.sin(diffLon / 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return GeometryConstants.EARTH_RADIUS_KM * c;
    }

    public static double speedBetweenWGS84PointsInKmH(final Point from, final Point to, final long diffInSeconds) {
        final double distanceKm = distanceBetweenWGS84PointsInKm(from, to);
        final double hours = diffInSeconds / 60.0 / 60.0;
        return distanceKm / hours;
    }

    public static Geometry union(final List<Geometry> geometryCollection) {
        return JTS_GEOMETRY_FACTORY.buildGeometry(geometryCollection).union();
    }

    public static Geometry convertGeoJsonGeometryToGeometry(final String geometryJson) throws ParseException {
        try {
            return geoJsonReader.get().read(geometryJson);
        } catch (final ParseException e) {
            log.error("Failed to parse geometry: " + geometryJson, e);
            throw e;
        }

    }

    public static Geometry fixGeometry(final Geometry geometry) {
        // This fixes some issues but not all (ie. if there is linestring with two identical points)
        final Geometry g = DouglasPeuckerSimplifier.simplify(geometry, SIMPLIFY_DOUGLAS_PEUCKER_TOLERANCE);
        if (!g.isValid()) {
            // First try keeping collapsed geometries
            final GeometryFixer fixer = new GeometryFixer(g);
            fixer.setKeepCollapsed(true);
            final Geometry fixed = fixer.getResult();
            if ( fixed.getGeometryType().equals(Geometry.TYPENAME_GEOMETRYCOLLECTION) ) {
                // GeometryCollection is not supported
                return GeometryFixer.fix(g); // This should not produce collection as collapsed are not reserved
            }
            return fixed;
        }
        return g;
    }

    public static Geometry simplify(final Geometry geometry) {
        return DouglasPeuckerSimplifier.simplify(geometry, SIMPLIFY_DOUGLAS_PEUCKER_TOLERANCE);
    }

    public static String convertGeometryToGeoJsonString(final Geometry geometry) {
        return geoJsonWriter.get().write(geometry);
    }

    public static Geometry convertWKBToGeometry(final byte[] wkbBytes) throws ParseException {
        return wkbGeometryReader.get().read(wkbBytes);
    }

    public static String convertBoundsCoordinatesToWktPolygon(final Double xMin, final Double xMax, final Double yMin, final Double yMax) {
        if( xMin != null &&
            xMax != null &&
            yMin != null &&
            yMax != null) {

            final String[] names  = new String[] { "xMin", "xMax", "yMin", "yMax" };
            final String[] values = new String[] { xMin.toString(), xMax.toString(), yMin.toString(), yMax.toString() };

            return StringUtils.replaceEach("POLYGON((xMin yMin, xMax yMin, xMax yMax, xMin yMax, xMin yMin))", names, values);
        }

        return null;
    }

    public static Geometry convertWktToGeomety(final String wktGeometry) throws ParseException {
            return wktGeometryReader.get().read(wktGeometry);
    }

    public static fi.livi.digitraffic.tie.metadata.geojson.Geometry<?> convertWktToGeoJSON(final String geometryWkt) throws ParseException {
        final Geometry geometry = convertWktToGeomety(geometryWkt);
        return convertGeometryToGeoJSONGeometry(geometry);
    }
}
