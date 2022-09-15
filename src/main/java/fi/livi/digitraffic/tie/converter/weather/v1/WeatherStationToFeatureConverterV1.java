package fi.livi.digitraffic.tie.converter.weather.v1;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.StationSensorConverterService;
import fi.livi.digitraffic.tie.converter.roadstation.v1.AbstractRoadstationToFeatureConverterV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationFeatureCollectionSimpleV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationFeatureDetailedV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationFeatureSimpleV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationPropertiesDetailedV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationPropertiesSimpleV1;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.v1.RoadStation;
import fi.livi.digitraffic.tie.model.v1.WeatherStation;

@Component
public class WeatherStationToFeatureConverterV1 extends AbstractRoadstationToFeatureConverterV1 {

    private static final Logger log = LoggerFactory.getLogger(WeatherStationToFeatureConverterV1.class);

    private final StationSensorConverterService stationSensorConverterService;

    @Autowired
    private WeatherStationToFeatureConverterV1(final CoordinateConverter coordinateConverter, final StationSensorConverterService stationSensorConverterService) {
        super(coordinateConverter);
        this.stationSensorConverterService = stationSensorConverterService;
    }

    public WeatherStationFeatureCollectionSimpleV1 convertToSimpleFeatureCollection(final List<WeatherStation> stations,
                                                                                    final Instant lastUpdated) {

        final List<WeatherStationFeatureSimpleV1> features =
            stations.stream()
                .map(this::convertToSimpleFeature)
                .collect(Collectors.toList());

        return new WeatherStationFeatureCollectionSimpleV1(lastUpdated, features);
    }

    private WeatherStationFeatureSimpleV1 convertToSimpleFeature(final WeatherStation station) {
        if (log.isDebugEnabled()) {
            log.debug("method=convertToSimpleFeature " + station);
        }

        final WeatherStationPropertiesSimpleV1 properties =
            new WeatherStationPropertiesSimpleV1(station.getRoadStationNaturalId());

        // RoadStation properties
        final RoadStation rs = station.getRoadStation();
        setRoadStationPropertiesSimple(properties, rs, station.getModified());

        return new WeatherStationFeatureSimpleV1(getGeometry(rs), properties);
    }

    public WeatherStationFeatureDetailedV1 convertToDetailedFeature(final WeatherStation station) {

        if (log.isDebugEnabled()) {
            log.debug("method=convertToDetailedFeature " + station);
        }

        final List<Long> sensorsNatualIds =
            stationSensorConverterService.getPublishableSensorsNaturalIdsByRoadStationId(station.getRoadStationId(), RoadStationType.WEATHER_STATION);

        final WeatherStationPropertiesDetailedV1 properties =
            new WeatherStationPropertiesDetailedV1(
                station.getRoadStationNaturalId(),
                station.getWeatherStationType(),
                station.isMaster(),
                sensorsNatualIds);

        // RoadStation properties
        final RoadStation rs = station.getRoadStation();
        setRoadStationPropertiesDetailed(properties, rs, station.getModified());

        return new WeatherStationFeatureDetailedV1(getGeometry(rs), properties);
    }
}
