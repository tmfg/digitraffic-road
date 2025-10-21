package fi.livi.digitraffic.tie.helper;

import fi.livi.digitraffic.tie.service.IllegalArgumentException;

import org.locationtech.jts.geom.Polygon;

import static fi.livi.digitraffic.tie.helper.PostgisGeometryUtils.createSquarePolygonFromMinMax;

public final class BoundingBoxUtils {
    private BoundingBoxUtils() {}

    public static final double LIMIT_MIN_X = 20.0;
    public static final double LIMIT_MIN_Y = 59.0;
    public static final double LIMIT_MAX_X = 32.0;
    public static final double LIMIT_MAX_Y = 71.0;

    private static final String ERROR_LIMITS = String.format("Coordinates must be inside[%f, %f, %f, %f]", LIMIT_MIN_X, LIMIT_MIN_Y, LIMIT_MAX_X, LIMIT_MAX_Y);

    public static Polygon getBoundingBox(final Double xMin, final Double xMax, final Double yMin, final Double yMax) {
        if(xMin == null && xMax == null && yMin == null && yMax == null) {
            return null;
        }

        if(xMin == null || xMax == null || yMin == null || yMax == null) {
            throw new IllegalArgumentException("All coordinates must be set");
        }

        if(xMin < LIMIT_MIN_X || xMax > LIMIT_MAX_X || yMin < LIMIT_MIN_Y || yMax > LIMIT_MAX_Y) {
            throw new IllegalArgumentException(ERROR_LIMITS);
        }

        return createSquarePolygonFromMinMax(xMin, xMax, yMin, yMax);
    }
}
