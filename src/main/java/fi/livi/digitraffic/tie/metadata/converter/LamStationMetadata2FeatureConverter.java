package fi.livi.digitraffic.tie.metadata.converter;

import java.time.LocalDateTime;
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

    public static LamStationFeatureCollection convert(final List<LamStation> stations, final LocalDateTime lastUpdated) {
        final LamStationFeatureCollection collection = new LamStationFeatureCollection(lastUpdated);

        for(final LamStation lam : stations) {
            try {
                collection.add(convert(lam));
            } catch (final NonPublicRoadStationException nprse) {
                //Skip non public roadstation
                log.warn("Skipping: " + nprse.getMessage());
            }
        }
        return collection;
    }

    /**
     *
     * @param lam
     * @return
     * @throws NonPublicRoadStationException If road station is non public exception is thrown
     */
    private static LamStationFeature convert(final LamStation lam) throws NonPublicRoadStationException {
        final LamStationFeature f = new LamStationFeature();
        if (log.isDebugEnabled()) {
            log.debug("Convert: " + lam);
        }
        f.setId(lam.getRoadStationNaturalId());

        final LamStationProperties properties = f.getProperties();

        // Lam station properties
        properties.setId(lam.getId());
        properties.setLotjuId(lam.getLotjuId());
        properties.setLamNaturalId(lam.getNaturalId());
        properties.setDirection1Municipality(lam.getDirection1Municipality());
        properties.setDirection1MunicipalityCode(lam.getDirection1MunicipalityCode());
        properties.setDirection2Municipality(lam.getDirection2Municipality());
        properties.setDirection2MunicipalityCode(lam.getDirection2MunicipalityCode());
        properties.setLamStationType(lam.getLamStationType());
        properties.setCalculatorDeviceType(lam.getCalculatorDeviceType());
        properties.setName(lam.getName());

        // RoadStation properties
        final RoadStation rs = lam.getRoadStation();
        setRoadStationProperties(properties, rs);



        if (rs.getLatitude() != null && rs.getLongitude() != null) {
            if (rs.getAltitude() != null) {
                f.setGeometry(new Point(rs.getLongitude().longValue(),
                                        rs.getLatitude().longValue(),
                                        rs.getAltitude().longValue()));
            } else {
                f.setGeometry(new Point(rs.getLongitude().longValue(),
                                        rs.getLatitude().longValue()));
            }
            f.getGeometry().setCrs(crs);
        }

        return f;
    }
}
