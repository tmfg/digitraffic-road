package fi.livi.digitraffic.tie.metadata.converter;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.lamstation.LamStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.lamstation.LamStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.lamstation.LamStationProperties;
import fi.livi.digitraffic.tie.metadata.model.LamStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;

public final class LamStationMetadata2FeatureConverter extends AbstractMetadataToFeatureConverter {

    private static final Log log = LogFactory.getLog( LamStationMetadata2FeatureConverter.class );

    private LamStationMetadata2FeatureConverter() {}

    public static LamStationFeatureCollection convert(final List<LamStation> stations) {
        final LamStationFeatureCollection collection = new LamStationFeatureCollection();

        for(final LamStation lam : stations) {
            collection.add(convert(lam));
        }
        return collection;
    }

    private static LamStationFeature convert(final LamStation lam) {
        final LamStationFeature f = new LamStationFeature();
        if (log.isDebugEnabled()) {
            log.debug("Convert: " + lam);
        }
        f.setId(Long.toString(lam.getId()));

        final LamStationProperties properties = f.getProperties();

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
        final RoadStation rs = lam.getRoadStation();
        setRoadStationProperties(properties, rs);



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
