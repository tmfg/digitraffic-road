package fi.livi.digitraffic.tie.metadata.converter;

import java.time.ZonedDateTime;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationProperties;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.WeatherStation;

@Component
public final class WeatherStationMetadata2FeatureConverter extends AbstractMetadataToFeatureConverter {

    private static final Log log = LogFactory.getLog( WeatherStationMetadata2FeatureConverter.class );

    @Autowired
    public WeatherStationMetadata2FeatureConverter(final CoordinateConverter coordinateConverter) {
        super(coordinateConverter);
    }

    public WeatherStationFeatureCollection convert(final List<WeatherStation> stations, final ZonedDateTime lastUpdated) {
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
    private WeatherStationFeature convert(final WeatherStation rws) throws NonPublicRoadStationException {
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

        setCoordinates(f, rs);

        return f;
    }
}
