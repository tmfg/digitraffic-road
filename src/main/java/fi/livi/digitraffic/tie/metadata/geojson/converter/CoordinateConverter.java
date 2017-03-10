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

    private static final CoordinateTransform transformerFromETRS89ToWGS84;
    private static final CoordinateTransform transformerFromKKJ3ToWGS84;

    static {
        CRSFactory crsFactory = new CRSFactory();

        // WGS84: http://spatialreference.org/ref/epsg/4326/ -> Proj4
        CoordinateReferenceSystem coordinateTransformToWGS84 = crsFactory.createFromParameters("EPSG:4326",
                "+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs");

        // ETRS89 to WGS84 transformer
        // ETRS89: http://spatialreference.org/ref/epsg/etrs89-etrs-tm35fin/ -> Proj4
        CoordinateReferenceSystem coordinateTransformFromETRS89 = crsFactory.createFromParameters("EPSG:3067",
                "+proj=utm +zone=35 ellps=GRS80 +units=m +no_defs");

        CoordinateTransformFactory coordinateTransformFactory = new CoordinateTransformFactory();
        transformerFromETRS89ToWGS84 = coordinateTransformFactory.createTransform(coordinateTransformFromETRS89, coordinateTransformToWGS84);

        // KKJ3 to WGS84 transformer
        /*
         *  http://latuviitta.org/documents/YKJ-TM35FIN_muunnos_ogr2ogr_cs2cs.txt
         *  Onneksi PROJ.4-käyttäjä voi kuitenkin ottaa tilanteen helposti omaan hallintaansa. Tämä onnistuu käyttämällä koordinaattijärjestelmän määrittelyyn EPSG-koodien sijasta proj-merkkijonoa.
         *  Alla esitetään määrittelyt KKJ-kaistoille 1-4
         *
         *  # KKJ / Finland zone 1 EPSG:2391
         *  proj="tmerc +lat_0=0 +lon_0=21 +k=1 +x_0=1500000 +y_0=0 +ellps=intl +towgs84=-96.0617,-82.4278,-121.7535,4.80107,0.34543,-1.37646,1.4964 +units=m +no_defs"
         *
         *  # KKJ / Finland zone 2 EPSG:2392
         *  +proj="tmerc +lat_0=0 +lon_0=24 +k=1 +x_0=2500000 +y_0=0 +ellps=intl +towgs84=-96.0617,-82.4278,-121.7535,4.80107,0.34543,-1.37646,1.4964 +units=m +no_defs"
         *
         *  # KKJ / Finland Uniform Coordinate System EPSG:2393
         *  +proj="tmerc +lat_0=0 +lon_0=27 +k=1 +x_0=3500000 +y_0=0 +ellps=intl +towgs84=-96.0617,-82.4278,-121.7535,4.80107,0.34543,-1.37646,1.4964 +units=m +no_defs"
         *
         *  # KKJ / Finland zone 4 EPSG:2394
         *  +proj="tmerc +lat_0=0 +lon_0=30 +k=1 +x_0=4500000 +y_0=0 +ellps=intl +towgs84=-96.0617,-82.4278,-121.7535,4.80107,0.34543,-1.37646,1.4964 +units=m +no_defs"
         */
        CoordinateReferenceSystem coordinateTransformFromKKJ3 = crsFactory.createFromParameters("EPSG:2393",
                "+proj=tmerc +lat_0=0 +lon_0=27 +k=1 +x_0=3500000 +y_0=0 +ellps=intl +towgs84=-96.0617,-82.4278,-121.7535,4.80107,0.34543,-1.37646,1.4964 +units=m +no_defs");
        transformerFromKKJ3ToWGS84 = coordinateTransformFactory.createTransform(coordinateTransformFromKKJ3, coordinateTransformToWGS84);
    }

    public static Point convertFromETRS89ToWGS84(Point fromETRS89) {
        return convert(fromETRS89, transformerFromETRS89ToWGS84);
    }

    public static Point convertFromKKJ3ToWGS84(Point fromKkj3) {
        return convert(fromKkj3, transformerFromKKJ3ToWGS84);
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
