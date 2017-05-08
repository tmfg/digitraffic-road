package fi.livi.digitraffic.tie.metadata.converter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.metadata.dao.WeatherStationRepository;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationProperties;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.WeatherStation;

@Component
public final class WeatherStationMetadata2FeatureConverter extends AbstractMetadataToFeatureConverter {
    private static final Log log = LogFactory.getLog( WeatherStationMetadata2FeatureConverter.class );

    private WeatherStationRepository weatherStationRepository;

    @Autowired
    public WeatherStationMetadata2FeatureConverter(final CoordinateConverter coordinateConverter,
        final WeatherStationRepository weatherStationRepository) {
        super(coordinateConverter);
        this.weatherStationRepository = weatherStationRepository;
    }

    public WeatherStationFeatureCollection convert(final List<WeatherStation> stations, final ZonedDateTime lastUpdated) {
        final WeatherStationFeatureCollection collection = new WeatherStationFeatureCollection(lastUpdated);
        final Map<Long, List<Long>> sensorMap = createSensorMap();

        for(final WeatherStation rws : stations) {
            try {
                collection.add(convert(sensorMap, rws));
            } catch (final NonPublicRoadStationException nprse) {
                //Skip non public roadstation
                log.warn("Skipping: " + nprse.getMessage());
                continue;
            }
        }
        return collection;
    }

    private Map<Long, List<Long>> createSensorMap() {
        final List<Object[]> list = weatherStationRepository.listWeatherStationSensors();
        final Map<Long, List<Long>> sensorMap = new HashMap<>();

        list.stream().forEach(oo -> {
            final Long rsId = ((BigDecimal)oo[0]).longValue();
            final String sensorList = (String)oo[1];

            sensorMap.put(rsId, sensorList(sensorList));
        });

        return sensorMap;
    }

    private static List<Long> sensorList(final String sensorList) {
        return Stream.of(sensorList.split(",")).map(Long::valueOf).collect(Collectors.toList());
    }
    
    private WeatherStationFeature convert(final Map<Long, List<Long>> sensorMap, final WeatherStation rws) throws NonPublicRoadStationException {
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
            final List<Long> sensorList = sensorMap.get(rws.getRoadStationId());

            properties.setStationSensors(ObjectUtils.firstNonNull(sensorList, Collections.emptyList()));
        }

        // RoadStation properties
        final RoadStation rs = rws.getRoadStation();
        setRoadStationProperties(properties, rs);

        setCoordinates(f, rs);

        return f;
    }
}
