package fi.livi.digitraffic.tie.helper;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

public class GeometryConstants {

    public static final double EARTH_RADIUS_KM = 6371;

    /**
     * GeoJSON uses WGS84 as Coordinate Reference System and it's spatial reference identifier (SRID) is 4326.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc7946#section-4">The GeoJSON Format –4.  Coordinate Reference System</a>
     * @see <a href="https://epsg.io/4326"></a>
     */
    public static final int SRID = 4326; // = WGS84

    /**
     * WGS84 EPSG ID - World Geodetic System 1984, used in GPS
     * @see <a href="https://epsg.io/4326">WGS84 - World Geodetic System 1984, used in GPS</a>
     */
    public static final String EPSG_ID_FOR_WGS84 = "EPSG:4326";

    /**
     * ETRS89 EPSG ID / TM35FIN(E,N) -- Finland
     * @see <a href="https://epsg.io/3067>TRS89 / TM35FIN(E,N) -- Finland</a>
     */
    public static final String EPSG_ID_FOR_3067 = "EPSG:3067";
    /**
     * Uses 6 digits precision for coordinates. That gives about 5 cm accuracy at Finland (lat 60°).
     */
    public static final int COORDINATE_SCALE_6_DIGITS = 6;
    /**
     * Uses 6 digits precision for coordinates. That gives about 5 cm accuracy at Finland (lat 60°).
     * Amount by which to multiply a coordinate after subtracting the offset, to obtain a precise coordinate.
     * @see org.locationtech.jts.geom.PrecisionModel */
    public static final int COORDINATE_SCALE_6_DIGITS_JTS_PRECISION_MODEL = 1000000;
    public static final double COORDINATE_SCALE_6_DIGITS_POSTGIS = 0.000001;
    public static final PrecisionModel JTS_PRECISION_MODEL = new PrecisionModel(GeometryConstants.COORDINATE_SCALE_6_DIGITS_JTS_PRECISION_MODEL); // 6 decimals

    public static final GeometryFactory JTS_GEOMETRY_FACTORY = new GeometryFactory(JTS_PRECISION_MODEL, SRID);

    public static final double MIN_LENGTH_KM_FOR_LINESTRING = 0.00002; // 2 cm

    public static final double SIMPLIFY_DOUGLAS_PEUCKER_TOLERANCE = 0.00005;

    public static final String POLYGON_OVER_FINLAND = "POLYGON((19.0 59.0, 32.0 59.0, 32.0 72.0, 19.0 72.0, 19.0 59.0))";

    private GeometryConstants() {
        // not possible to make instance
    }
}
