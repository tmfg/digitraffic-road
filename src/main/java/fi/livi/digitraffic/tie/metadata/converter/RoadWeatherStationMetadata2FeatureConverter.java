package fi.livi.digitraffic.tie.metadata.converter;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.roadweather.RoadWeatherStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.roadweather.RoadWeatherStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.roadweather.RoadWeatherStationProperties;
import fi.livi.digitraffic.tie.metadata.geojson.roadweather.RoadWeatherStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadWeatherSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadWeatherStation;

public final class RoadWeatherStationMetadata2FeatureConverter extends AbstractMetadataToFeatureConverter {

    private static final Log log = LogFactory.getLog( RoadWeatherStationMetadata2FeatureConverter.class );

    private RoadWeatherStationMetadata2FeatureConverter() {}

    public static RoadWeatherStationFeatureCollection convert(final List<RoadWeatherStation> stations) {
        final RoadWeatherStationFeatureCollection collection = new RoadWeatherStationFeatureCollection();

        for(final RoadWeatherStation rws : stations) {
            collection.add(convert(rws));
        }
        return collection;
    }

    private static RoadWeatherStationFeature convert(final RoadWeatherStation rws) {
        final RoadWeatherStationFeature f = new RoadWeatherStationFeature();
        if (log.isDebugEnabled()) {
            log.debug("Convert: " + rws);
        }
        f.setId(Long.toString(rws.getRoadStationNaturalId()));

        final RoadWeatherStationProperties properties = f.getProperties();

        // road weather station properties
        properties.setId(rws.getRoadStationNaturalId());
        properties.setRoadWeatherStationType(rws.getRoadWeatherStationType());

        for (RoadWeatherSensor rwSensor : rws.getRoadWeatherSensors()) {
            properties.addSensor(convert(rwSensor));
        }

        // RoadStation properties
        final RoadStation rs = rws.getRoadStation();
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

    private static RoadWeatherStationSensor convert(RoadWeatherSensor roadWeatherSensor) {
        RoadWeatherStationSensor meta = new RoadWeatherStationSensor();
        meta.setId(roadWeatherSensor.getId());
        meta.setAltitude(roadWeatherSensor.getAltitude());
        meta.setDescription(roadWeatherSensor.getDescription());
        meta.setName(roadWeatherSensor.getName());
        meta.setSensorTypeId(roadWeatherSensor.getSensorTypeId());
        return meta;
    }
}
