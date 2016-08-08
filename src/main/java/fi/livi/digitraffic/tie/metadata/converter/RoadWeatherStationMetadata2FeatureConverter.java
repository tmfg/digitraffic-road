package fi.livi.digitraffic.tie.metadata.converter;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.roadweather.RoadWeatherStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.roadweather.RoadWeatherStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.roadweather.RoadWeatherStationProperties;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadWeatherStation;

public final class RoadWeatherStationMetadata2FeatureConverter extends AbstractMetadataToFeatureConverter {

    private static final Log log = LogFactory.getLog( RoadWeatherStationMetadata2FeatureConverter.class );

    private RoadWeatherStationMetadata2FeatureConverter() {}

    public static RoadWeatherStationFeatureCollection convert(final List<RoadWeatherStation> stations, final LocalDateTime lastUpdated) {
        final RoadWeatherStationFeatureCollection collection = new RoadWeatherStationFeatureCollection(lastUpdated);

        for(final RoadWeatherStation rws : stations) {
            try {
                collection.add(convert(rws));
            } catch (final NonPublicRoadStationException nprse) {
                //Skip non public roadstation
                log.warn("Skipping: " + nprse.getMessage());
                continue;
            }
        }
        return collection;
    }

    /**
     *
     * @param rws
     * @return
     * @throws NonPublicRoadStationException If road station is non public exception is thrown
     */
    private static RoadWeatherStationFeature convert(final RoadWeatherStation rws) throws NonPublicRoadStationException {
        final RoadWeatherStationFeature f = new RoadWeatherStationFeature();
        if (log.isDebugEnabled()) {
            log.debug("Convert: " + rws);
        }
        f.setId(rws.getRoadStationNaturalId());

        final RoadWeatherStationProperties properties = f.getProperties();

        // road weather station properties
        properties.setId(rws.getRoadStationNaturalId());
        properties.setLotjuId(rws.getLotjuId());
        properties.setRoadWeatherStationType(rws.getRoadWeatherStationType());
        properties.setMaster(rws.isMaster());

        if (rws.getRoadStation() != null) {
            for (final fi.livi.digitraffic.tie.metadata.model.RoadStationSensor rSSensor : rws.getRoadStation().getRoadStationSensors()) {
                properties.addSensor(rSSensor);
            }
        }

        // RoadStation properties
        final RoadStation rs = rws.getRoadStation();
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
