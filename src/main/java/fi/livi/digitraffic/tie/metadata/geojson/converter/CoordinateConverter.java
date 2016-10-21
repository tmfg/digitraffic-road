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

    private final CoordinateTransform transformerFromETRS89ToWGS84;

    public CoordinateConverter() {
        CRSFactory crsFactory = new CRSFactory();

        // ETRS89 to WGS84 transformer
        // ETRS89: http://spatialreference.org/ref/epsg/etrs89-etrs-tm35fin/ -> Proj4
        CoordinateReferenceSystem coordinateTransformFrom = crsFactory.createFromParameters("EPSG:3067",
                "+proj=utm +zone=35 ellps=GRS80 +units=m +no_defs");
        // WGS84: http://spatialreference.org/ref/epsg/4326/ -> Proj4
        CoordinateReferenceSystem coordinateTransformTo = crsFactory.createFromParameters("EPSG:4326",
                "+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs");
        CoordinateTransformFactory coordinateTransformFactory = new CoordinateTransformFactory();
        transformerFromETRS89ToWGS84 = coordinateTransformFactory.createTransform(coordinateTransformFrom, coordinateTransformTo);
    }

    public Point convertFromETRS89ToWGS84(Point fromETRS89) {
        ProjCoordinate to = new ProjCoordinate();

        ProjCoordinate from = new ProjCoordinate(fromETRS89.getLongitude(),
                                                 fromETRS89.getLatitude());
        transformerFromETRS89ToWGS84.transform(from, to);
        Point point = fromETRS89.hasAltitude() ?
                      new Point(to.x, to.y, fromETRS89.getAltitude()) :
                      new Point(to.x, to.y);

        if (log.isDebugEnabled()) {
            log.debug("From: " + fromETRS89 + " to " + point);
        }
        return point;
    }
}
