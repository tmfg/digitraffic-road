package fi.livi.digitraffic.tie.metadata.converter;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationProperties;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.WeatherStation;

public final class WeatherStationMetadata2FeatureConverter extends AbstractMetadataToFeatureConverter {

    private static final Log log = LogFactory.getLog( WeatherStationMetadata2FeatureConverter.class );

    private WeatherStationMetadata2FeatureConverter() {}

    public static WeatherStationFeatureCollection convert(final List<WeatherStation> stations, final LocalDateTime lastUpdated) {
        final WeatherStationFeatureCollection collection = new WeatherStationFeatureCollection(lastUpdated);

        for(final WeatherStation rws : stations) {
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
    private static WeatherStationFeature convert(final WeatherStation rws) throws NonPublicRoadStationException {
        final WeatherStationFeature f = new WeatherStationFeature();
        if (log.isDebugEnabled()) {
            log.debug("Convert: " + rws);
        }
        f.setId(rws.getRoadStationNaturalId());

        final WeatherStationProperties properties = f.getProperties();

        // weather station properties
        properties.setId(rws.getRoadStationNaturalId());
        properties.setLotjuId(rws.getLotjuId());
        properties.setWeatherStationType(rws.getWeatherStationType());
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
