package fi.livi.digitraffic.tie.metadata.geojson.converter;

import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.ProjCoordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.metadata.geojson.Point;

@Component
public class CoordinateConverter {

    private static final Logger log = LoggerFactory.getLogger(CoordinateConverter.class);

    private static final CoordinateTransform transformerFromEtrs89Tm35FinToWgs84;

    static {
        CRSFactory crsFactory = new CRSFactory();

        // WGS84: http://spatialreference.org/ref/epsg/4326/ -> Proj4
        CoordinateReferenceSystem wgs84 = crsFactory.createFromName("EPSG:4326");

        // ETRS89-TM35FIN/EUREF-FIN http://spatialreference.org/ref/epsg/etrs89-etrs-tm35fin/ -> Proj4
        CoordinateReferenceSystem etrs89tm35fin = crsFactory.createFromName("EPSG:3067");

        CoordinateTransformFactory coordinateTransformFactory = new CoordinateTransformFactory();
        // ETRS89-TM35FIN to WGS84 transformer
        transformerFromEtrs89Tm35FinToWgs84 = coordinateTransformFactory.createTransform(etrs89tm35fin, wgs84);
    }

    public static Point convertFromETRS89ToWGS84(Point fromETRS89) {
        return convert(fromETRS89, transformerFromEtrs89Tm35FinToWgs84);
    }

    private static Point convert(final Point fromPoint, final CoordinateTransform transformer) {
        ProjCoordinate to = new ProjCoordinate();
        ProjCoordinate from = new ProjCoordinate(fromPoint.getLongitude(),
                                                 fromPoint.getLatitude());
        transformer.transform(from, to);
        Point point = fromPoint.hasAltitude() ?
                      new Point(to.x, to.y, fromPoint.getAltitude()) :
                      new Point(to.x, to.y);

        if (log.isDebugEnabled()) {
            log.debug("From: " + fromPoint + " to " + point);
        }
        return point;
    }
}
