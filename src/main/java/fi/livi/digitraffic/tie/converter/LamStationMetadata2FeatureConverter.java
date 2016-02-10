package fi.livi.digitraffic.tie.converter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geojson.Crs;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.Point;
import org.geojson.jackson.CrsType;

import fi.livi.digitraffic.tie.model.LamStationMetadata;

public final class LamStationMetadata2FeatureConverter {
    private static final Log LOG = LogFactory.getLog( LamStationMetadata2FeatureConverter.class );

    private LamStationMetadata2FeatureConverter() {}

    public static FeatureCollection convert(final List<LamStationMetadata> stations) {
        final FeatureCollection collection = new FeatureCollection();

        final Crs crs = new Crs();
        crs.setType(CrsType.link);
        final Map<String, Object> crsProperties = new HashMap<>();
        // http://docs.jhs-suositukset.fi/jhs-suositukset/JHS180_liite1/JHS180_liite1.html
        // http://www.opengis.net/def/crs/EPSG/0/[code]
        // ETRS89 / TM35-FIN (EPSG:3067)
        // http://www.opengis.net/def/crs/EPSG/0/3067
        // WGS84 (EPSG:4326)
        // http://www.opengis.net/def/crs/EPSG/0/4326
        crsProperties.put("href", "http://www.opengis.net/def/crs/EPSG/0/3067");
        crsProperties.put("type",  "proj4");
        crs.setProperties(crsProperties);
        collection.setCrs(crs);

        for(final LamStationMetadata lam : stations) {
            collection.add(convert(lam));
        }

        return collection;
    }

    private static Feature convert(final LamStationMetadata lam) {
        final Feature f = new Feature();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Convert: " + lam.toString());
        }
        f.setProperty("lamNumber", lam.getLamId());
        f.setProperty("rwsName", lam.getRwsName());
        f.setProperty("names", getNames(lam));

        f.setGeometry(new Point(lam.getLatitude(), lam.getLongitude(), lam.getElevation()));

        return f;
    }

    private static Map<String, String> getNames(final LamStationMetadata lam) {
        final Map<String, String> map = new HashMap<>();

        map.put("fi", lam.getNameFi());
        map.put("sv", lam.getNameSe());
        map.put("en", lam.getNameEn());

        return map;
    }
}
