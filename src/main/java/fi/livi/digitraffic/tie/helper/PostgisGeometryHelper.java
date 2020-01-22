package fi.livi.digitraffic.tie.helper;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;

public class PostgisGeometryHelper {

    public static final PrecisionModel PRECISION_MODEL = new PrecisionModel(10);
    public static final int SRID = 4326; // = WGS84 http://www.epsg-registry.org/
    public static final GeometryFactory GF = new GeometryFactory(PRECISION_MODEL, SRID);

    public static Point createPointZ(double x, double y, Double z) {
        // PostGIS PointZ can't have null Z-coordinate
        final Coordinate coordinate =  new Coordinate(x, y, z != null ? z : 0);
        return GF.createPoint(coordinate);
    }

    public static Point createPointZFromETRS89ToWGS84(double x, double y, Double z) {
        fi.livi.digitraffic.tie.metadata.geojson.Point converted =
            CoordinateConverter.convertFromETRS89ToWGS84(new fi.livi.digitraffic.tie.metadata.geojson.Point(x, y, z));
        return createPointZ(converted.getLongitude(), converted.getLatitude(), converted.getAltitude());
    }

}
