package fi.livi.digitraffic.tie.helper;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;

public class PostgisGeometryHelper {

    public static final PrecisionModel PRECISION_MODEL = new PrecisionModel(10);
    public static final int SRID = 4326; // = WGS84 http://www.epsg-registry.org/
    public static final GeometryFactory GF = new GeometryFactory(PRECISION_MODEL, SRID);

    public static Point createPointWithZ(final double x, final double y, final Double z) {
        // PostGIS PointZ can't have null Z-coordinate
        final Coordinate coordinate =  new Coordinate(x, y, z != null ? z : 0);
        return GF.createPoint(coordinate);
    }

    public static Coordinate createCoordinateWithZ(final double x, final double y, final Double z) {
        // PostGIS PointZ can't have null Z-coordinate
        return new Coordinate(x, y, z != null ? z : 0);
    }

    public static Coordinate createCoordinateWithZFromETRS89ToWGS84(double x, double y, Double z) {
        fi.livi.digitraffic.tie.metadata.geojson.Point wgs84 =
            CoordinateConverter.convertFromETRS89ToWGS84(new fi.livi.digitraffic.tie.metadata.geojson.Point(x, y, z));
        return createCoordinateWithZ(wgs84.getLongitude(), wgs84.getLatitude(), wgs84.getAltitude());
    }


    public static LineString createLineStringZ(final List<Coordinate> lineStringCoordinates) {
        if (lineStringCoordinates.size() < 2) {
            throw new IllegalArgumentException("LineString need at least two points, was " + lineStringCoordinates.size());
        }
        return GF.createLineString(lineStringCoordinates.toArray(new Coordinate[0]));
    }
}
