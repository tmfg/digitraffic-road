package fi.livi.digitraffic.tie.converter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.livi.digitraffic.tie.geojson.Crs;
import fi.livi.digitraffic.tie.geojson.Feature;
import fi.livi.digitraffic.tie.geojson.FeatureCollection;
import fi.livi.digitraffic.tie.geojson.Point;
import fi.livi.digitraffic.tie.geojson.Properties;
import fi.livi.digitraffic.tie.geojson.jackson.CrsType;
import fi.livi.digitraffic.tie.model.LamStation;
import fi.livi.digitraffic.tie.model.RoadStation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class LamStationMetadata2FeatureConverter {

    private static final Log log = LogFactory.getLog( LamStationMetadata2FeatureConverter.class );

    private static final Crs crs;

    private LamStationMetadata2FeatureConverter() {}

    static {
        crs = new Crs();
        crs.setType(CrsType.link);
        final Map<String, Object> crsProperties = new HashMap<>();
        // http://docs.jhs-suositukset.fi/jhs-suositukset/JHS180_liite1/JHS180_liite1.html
        // http://www.opengis.net/def/crs/EPSG/0/[code]
        // ETRS89 / TM35-FIN / EUREF-FIN (EPSG:3067)
        // http://www.opengis.net/def/crs/EPSG/0/3067
        // http://spatialreference.org/ref/epsg/3067/
        // WGS84 (EPSG:4326)
        // http://www.opengis.net/def/crs/EPSG/0/4326
        // http://spatialreference.org/ref/epsg/wgs-84/
        crsProperties.put("href", "http://www.opengis.net/def/crs/EPSG/0/3067");
        crsProperties.put("type",  "proj4");
        crs.setProperties(crsProperties);
    }

    public static FeatureCollection convert(final List<LamStation> stations) {
        final FeatureCollection collection = new FeatureCollection();

        for(final LamStation lam : stations) {
            collection.add(convert(lam));
        }
        return collection;
    }

    private static Feature convert(final LamStation lam) {
        final Feature f = new Feature();
        if (log.isDebugEnabled()) {
            log.debug("Convert: " + lam.toString());
        }
        f.setId(Long.toString(lam.getId()));

        Properties properties = f.getProperties();

        // Lam station properties
        properties.setId(lam.getId());
        properties.setLamNaturalId(lam.getNaturalId());
        properties.setDirection1Municipality(lam.getDirection1Municipality());
        properties.setDirection1MunicipalityCode(lam.getDirection1MunicipalityCode());
        properties.setDirection2Municipality(lam.getDirection2Municipality());
        properties.setDirection2MunicipalityCode(lam.getDirection2MunicipalityCode());
        properties.setLamStationType(lam.getLamStationType());
        properties.setName(lam.getName());

        // RoadStation properties
        RoadStation rs = lam.getRoadStation();
        properties.setNaturalId(rs.getNaturalId());
        properties.setCollectionInterval(rs.getCollectionInterval());
        properties.setCollectionStatus(rs.getCollectionStatus());
        properties.setDescription(rs.getDescription());
        properties.setDistance(rs.getDistance());
        properties.setMunicipality(rs.getMunicipality());
        properties.setMunicipalityCode(rs.getMunicipalityCode());

        properties.setProvince(rs.getProvince());
        properties.setProvinceCode(rs.getProvinceCode());
        properties.setRoadNumber(rs.getRoadNumber());
        properties.setRoadPart(rs.getRoadPart());

        properties.addName("fi", rs.getNameFi());
        properties.addName("sv", rs.getNameSv());
        properties.addName("en", rs.getNameEn());


        if (rs.getLatitude() != null && rs.getLongitude() != null) {
            if (rs.getAltitude() != null) {
                f.setGeometry(new Point(rs.getLatitude().longValue(),
                                        rs.getLongitude().longValue(),
                                        rs.getAltitude().longValue()));
            } else {
                f.setGeometry(new Point(rs.getLatitude().longValue(),
                                        rs.getLongitude().longValue()));
            }
            f.getGeometry().setCrs(crs);
        }

        return f;
    }
}
