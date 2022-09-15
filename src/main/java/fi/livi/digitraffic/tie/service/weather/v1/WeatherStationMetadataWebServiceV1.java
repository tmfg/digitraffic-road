package fi.livi.digitraffic.tie.service.weather.v1;

import static fi.livi.digitraffic.tie.helper.DateHelper.getNewest;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.controller.RoadStationState;
import fi.livi.digitraffic.tie.converter.weather.v1.WeatherStationToFeatureConverterV1;
import fi.livi.digitraffic.tie.dao.v1.WeatherStationRepository;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationFeatureCollectionSimpleV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationFeatureDetailedV1;
import fi.livi.digitraffic.tie.model.CollectionStatus;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v1.WeatherStation;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;

@ConditionalOnWebApplication
@Service
public class WeatherStationMetadataWebServiceV1 {

    private final WeatherStationRepository weatherStationRepository;
    private final DataStatusService dataStatusService;
    private final WeatherStationToFeatureConverterV1 weatherStationToFeatureConverterV1;

    @Autowired
    public WeatherStationMetadataWebServiceV1(final WeatherStationRepository weatherStationRepository,
                                              final DataStatusService dataStatusService,
                                              final WeatherStationToFeatureConverterV1 weatherStationToFeatureConverterV1) {
        this.weatherStationRepository = weatherStationRepository;
        this.dataStatusService = dataStatusService;
        this.weatherStationToFeatureConverterV1 = weatherStationToFeatureConverterV1;
    }

    @Transactional(readOnly = true)
    public WeatherStationFeatureCollectionSimpleV1 findAllPublishableWeatherStationsAsSimpleFeatureCollection(final boolean onlyUpdateInfo,
                                                                                                              final RoadStationState roadStationState) {
        final List<WeatherStation> stations = onlyUpdateInfo ? Collections.emptyList() : findPublishableStations(roadStationState);

        return weatherStationToFeatureConverterV1.convertToSimpleFeatureCollection(
            stations,
            getMetadataLastUpdated());
    }

    @Transactional(readOnly = true)
    public WeatherStationFeatureDetailedV1 getWeatherStationById(final Long id) {
        final WeatherStation station = weatherStationRepository.findByRoadStationIsPublicIsTrueAndRoadStation_NaturalId(id);
        if(station == null) {
            throw new ObjectNotFoundException(WeatherStation.class, id);
        }

        return weatherStationToFeatureConverterV1.convertToDetailedFeature(station);
    }

    private List<WeatherStation> findPublishableStations(final RoadStationState roadStationState) {
        switch(roadStationState) {
        case ACTIVE:
            return weatherStationRepository.findByRoadStationPublishableIsTrueOrderByRoadStation_NaturalId();
        case REMOVED:
            return weatherStationRepository.findByRoadStationIsPublicIsTrueAndRoadStationCollectionStatusIsOrderByRoadStation_NaturalId
                (CollectionStatus.REMOVED_PERMANENTLY);
        case ALL:
            return weatherStationRepository.findByRoadStationIsPublicIsTrueOrderByRoadStation_NaturalId();
        default:
            throw new IllegalArgumentException();
        }
    }

    private Instant getMetadataLastUpdated() {
        final Instant sensorsUpdated = dataStatusService.findDataUpdatedInstant(DataType.WEATHER_STATION_SENSOR_METADATA);
        final Instant stationsUpdated = dataStatusService.findDataUpdatedInstant(DataType.WEATHER_STATION_METADATA);
        return getNewest(sensorsUpdated, stationsUpdated);
    }

    private Instant getMetadataLastChecked() {
        final Instant sensorsUpdated = dataStatusService.findDataUpdatedInstant(DataType.WEATHER_STATION_SENSOR_METADATA_CHECK);
        final Instant stationsUpdated = dataStatusService.findDataUpdatedInstant(DataType.WEATHER_STATION_METADATA_CHECK);
        return getNewest(sensorsUpdated, stationsUpdated);
    }
}
