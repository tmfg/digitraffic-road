package fi.livi.digitraffic.tie.metadata.geojson.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.sound.sampled.Line;

import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.ProjCoordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.metadata.geojson.LineString;
import fi.livi.digitraffic.tie.metadata.geojson.Point;

@Component
public class CoordinateConverter {

    private static final Logger log = LoggerFactory.getLogger(CoordinateConverter.class);

    private static final CoordinateTransform transformerFromEtrs89Tm35FinToWgs84;

    private static final CoordinateTransform transformerFromWgs84ToEtrs89Tm35Fin;

    static {
        CRSFactory crsFactory = new CRSFactory();

        // WGS84: http://spatialreference.org/ref/epsg/4326/ -> Proj4
        CoordinateReferenceSystem wgs84 = crsFactory.createFromName("EPSG:4326");

        // ETRS89-TM35FIN/EUREF-FIN http://spatialreference.org/ref/epsg/etrs89-etrs-tm35fin/ -> Proj4
        CoordinateReferenceSystem etrs89tm35fin = crsFactory.createFromName("EPSG:3067");

        CoordinateTransformFactory coordinateTransformFactory = new CoordinateTransformFactory();
        // ETRS89-TM35FIN to WGS84 transformer
        transformerFromEtrs89Tm35FinToWgs84 = coordinateTransformFactory.createTransform(etrs89tm35fin, wgs84);
        transformerFromWgs84ToEtrs89Tm35Fin = coordinateTransformFactory.createTransform(wgs84, etrs89tm35fin);
    }

    public static Point convertFromETRS89ToWGS84(Point fromETRS89) {
        return convert(fromETRS89, transformerFromEtrs89Tm35FinToWgs84);
    }

    public static Point convertFromWGS84ToETRS89(Point fromWGS84) {
        return convert(fromWGS84, transformerFromWgs84ToEtrs89Tm35Fin);
    }

    // TODO: change List<List<Double>> to List<List<Integer>> and remove int->double conversion from HavaintoToObservationFeatureConverter and do it here.
    public static LineString convertLineStringFromETRS89ToWGS84(List<List<Double>> fromETRS89Coordinates) {
        List<List<Double>> coords =
            fromETRS89Coordinates.stream()
                .map(l -> (List<Double>) new ArrayList(convertFromETRS89ToWGS84(new Point(l)).getCoordinates()))
                .collect(Collectors.toList());
        return new LineString(coords);
    }

    public static LineString convertLineStringFromWGS84ToETRS89(List<List<Double>> fromWGS84Coordinates) {
        List<List<Double>> coords =
            fromWGS84Coordinates.stream()
                .map(l -> (List<Double>) new ArrayList(convertFromWGS84ToETRS89(new Point(l)).getCoordinates()))
                .collect(Collectors.toList());
        return new LineString(coords);
    }

    private static Point convert(final Point fromPoint, final CoordinateTransform transformer) {
        ProjCoordinate to = new ProjCoordinate();
        ProjCoordinate from = new ProjCoordinate(fromPoint.getLongitude(),
                                                 fromPoint.getLatitude());
        transformer.transform(from, to);
        Point point = new Point(to.x, to.y, fromPoint.getAltitude());

        if (log.isDebugEnabled()) {
            log.debug("From: " + fromPoint + " to " + point);
        }
        return point;
    }
}
