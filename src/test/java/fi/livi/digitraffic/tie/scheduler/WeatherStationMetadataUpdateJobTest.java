package fi.livi.digitraffic.tie.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.controller.RoadStationState;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationFeatureCollectionSimpleV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationFeatureDetailedV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationFeatureSimpleV1;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.model.CollectionStatus;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.v1.RoadStationSensor;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.v1.lotju.LotjuTiesaaPerustiedotServiceEndpointMock;
import fi.livi.digitraffic.tie.service.v1.lotju.LotjuWeatherStationMetadataClient;
import fi.livi.digitraffic.tie.service.v1.weather.WeatherStationSensorUpdater;
import fi.livi.digitraffic.tie.service.v1.weather.WeatherStationUpdater;
import fi.livi.digitraffic.tie.service.v1.weather.WeatherStationsSensorsUpdater;
import fi.livi.digitraffic.tie.service.weather.v1.WeatherStationMetadataWebServiceV1;

public class WeatherStationMetadataUpdateJobTest extends AbstractMetadataUpdateJobTest {

    @Autowired
    private WeatherStationSensorUpdater weatherStationSensorUpdater;

    @Autowired
    private WeatherStationsSensorsUpdater weatherStationsSensorsUpdater;

    @Autowired
    private WeatherStationUpdater weatherStationUpdater;

    @Autowired
    private LotjuTiesaaPerustiedotServiceEndpointMock lotjuTiesaaPerustiedotServiceMock;

    @Autowired
    private RoadStationSensorService roadStationSensorService;

    @Autowired
    private LotjuWeatherStationMetadataClient lotjuWeatherStationMetadataClient;
    private WeatherStationMetadataWebServiceV1 weatherStationMetadataWebServiceV1;

    private WeatherStationFeatureCollectionSimpleV1 stationsBefore;
    private WeatherStationFeatureCollectionSimpleV1 stationsAfter;
    private Map<Long, RoadStationSensor> sensorsInitial;
    private Map<Long, RoadStationSensor> sensorsAfterChange;
    private WeatherStationFeatureDetailedV1 station1Before;
    private WeatherStationFeatureDetailedV1 station1After;
    private WeatherStationFeatureDetailedV1 station2Before;
    private WeatherStationFeatureDetailedV1 station2After;


    @BeforeEach
    public void setUpLotjuClientAndInitData() {
        if (!isBeanRegistered(WeatherStationMetadataWebServiceV1.class)) {
            final WeatherStationMetadataWebServiceV1 weatherStationMetadataWebServiceV1 = beanFactory.createBean(WeatherStationMetadataWebServiceV1.class);
            beanFactory.registerSingleton(weatherStationMetadataWebServiceV1.getClass().getCanonicalName(), weatherStationMetadataWebServiceV1);
            this.weatherStationMetadataWebServiceV1 = weatherStationMetadataWebServiceV1;
        }

        setLotjuClientFirstDestinationProviderAndSaveOriginalToMap(lotjuWeatherStationMetadataClient);

        lotjuTiesaaPerustiedotServiceMock.initStateAndService();

        // Update road weather stations to initial state (2 non obsolete stations and 2 obsolete)
        weatherStationSensorUpdater.updateRoadStationSensors();
        weatherStationUpdater.updateWeatherStations();
        weatherStationsSensorsUpdater.updateWeatherStationsSensors();
        sensorsInitial = roadStationSensorService.findAllRoadStationSensorsMappedByLotjuId(RoadStationType.WEATHER_STATION);

        stationsBefore =
            weatherStationMetadataWebServiceV1.findAllPublishableWeatherStationsAsSimpleFeatureCollection(false, RoadStationState.ACTIVE);
        station1Before = weatherStationMetadataWebServiceV1.getWeatherStationById(1034L);
        station2Before = weatherStationMetadataWebServiceV1.getWeatherStationById(1036L);
        assertEquals(2, stationsBefore.getFeatures().size());
        sensorsInitial.values().forEach(s -> entityManager.detach(s));

        // Now change lotju metadata and update tms stations (3 non obsolete stations and 1 bsolete)
        lotjuTiesaaPerustiedotServiceMock.setStateAfterChange(true);
        weatherStationSensorUpdater.updateRoadStationSensors();
        weatherStationUpdater.updateWeatherStations();
        weatherStationsSensorsUpdater.updateWeatherStationsSensors();

        sensorsAfterChange = roadStationSensorService.findAllRoadStationSensorsMappedByLotjuId(RoadStationType.WEATHER_STATION);
        stationsAfter =
            weatherStationMetadataWebServiceV1.findAllPublishableWeatherStationsAsSimpleFeatureCollection(false, RoadStationState.ACTIVE);
        assertEquals(3, stationsAfter.getFeatures().size());
        station1After = weatherStationMetadataWebServiceV1.getWeatherStationById(1034L);
        station2After = weatherStationMetadataWebServiceV1.getWeatherStationById(1036L);
    }

    @AfterEach
    public void restoreOriginalDestinationProviderForLotjuClients() {
        restoreLotjuClientDestinationProvider(lotjuWeatherStationMetadataClient);
    }
    // (name_fi ASC, road_station_type ASC, natural_id ASC, obsolete_date ASC);
    @Test
    public void testUpdateWeatherStations() {
        assertNull(findWithNaturalId(stationsBefore, 1033));
        assertNotNull(findWithNaturalId(stationsBefore, 1034));
        assertNull(findWithNaturalId(stationsBefore, 1035));
        assertNotNull(findWithNaturalId(stationsBefore, 1036));

        assertNull(findWithNaturalId(stationsAfter, 1033));
        assertNotNull(findWithNaturalId(stationsAfter, 1034));
        assertNotNull(findWithNaturalId(stationsAfter, 1035)); // removed temporary -> gathering
        assertNotNull(findWithNaturalId(stationsAfter, 1036));

        assertEquals(station1Before.getProperties().getName() + "1", station1After.getProperties().getName());

        assertEquals(station1After.getProperties().getCollectionStatus(), CollectionStatus.GATHERING);

        assertEquals(station1Before.getProperties().getNames().get("fi"), "Tie 3 Helsinki, Pirkkola");
        assertEquals(station1Before.getProperties().getNames().get("sv"), "Väg 3 Helsingfors, Britas");
        assertEquals(station1Before.getProperties().getNames().get("en"), "Road 3 Helsinki, Pirkkola");

        assertEquals(station1After.getProperties().getNames().get("fi"), "Tie 3 Helsinki, Kirkkola");
        assertEquals(station1After.getProperties().getNames().get("sv"), "Väg 3 Helsingfors, Kritas");
        assertEquals(station1After.getProperties().getNames().get("en"), "Road 3 Helsinki, Kirkkola");

        assertEquals(station1Before.getProperties().getRoadAddress().distanceFromRoadSectionStart, 4715);
        assertEquals(station1After.getProperties().getRoadAddress().distanceFromRoadSectionStart, 4716);

        // For conversions https://www.retkikartta.fi/
        final Point geomBefore = station1Before.getGeometry();
        final Point geomAfter = station1After.getGeometry();

        assertEquals(geomBefore.getLongitude(), 24.905725, 0.000001);
        assertEquals(geomAfter.getLongitude(), 24.905742, 0.000001);

        assertEquals(geomBefore.getLatitude(), 60.228845, 0.000001);
        assertEquals(geomAfter.getLatitude(), 60.228854, 0.000001);

        assertEquals(geomBefore.getAltitude(), 0.0, 0.01);
        assertEquals(geomAfter.getAltitude(), 1.0, 0.01);


        final RoadStationSensor sensorInitial = findSensorWithLotjuId(station2Before, 1, true);
        final RoadStationSensor sensorAfter = findSensorWithLotjuId(station2After, 1, false);

        assertNotNull(sensorInitial);
        assertEquals("EsitysFi", sensorInitial.getPresentationNameFi());
        assertEquals("EsitysFi2", sensorAfter.getPresentationNameFi());

        assertEquals("EsitysSe", sensorInitial.getPresentationNameSv());
        assertEquals("EsitysSe2", sensorAfter.getPresentationNameSv());

        assertEquals("EsitysEn", sensorInitial.getPresentationNameEn());
        assertEquals("EsitysEn2", sensorAfter.getPresentationNameEn());

        assertEquals("Ilman nopeus", sensorInitial.getDescriptionFi());
        assertEquals("Ilman lämpötila", sensorAfter.getDescriptionFi());

        assertEquals("Luft hastighet", sensorInitial.getDescriptionSv());
        assertEquals("Luft temperatur", sensorAfter.getDescriptionSv());

        assertEquals("Air velocity", sensorInitial.getDescriptionEn());
        assertEquals("Air temperature", sensorAfter.getDescriptionEn());

        assertEquals("°CC", sensorInitial.getUnit());
        assertEquals("°C", sensorAfter.getUnit());

        assertEquals(10, sensorInitial.getAccuracy().intValue());
        assertEquals(1, sensorAfter.getAccuracy().intValue());

        final RoadStationSensor sensor2Initial = findSensorWithLotjuId(station2Before, 2, true);
        final RoadStationSensor sensor2After = findSensorWithLotjuId(station2After, 2, false);

        assertNull(sensor2Initial);
        assertNotNull(sensor2After);

        final RoadStationSensor sensor36Initial = findSensorWithLotjuId(station2Before, 3, true);
        final RoadStationSensor sensor36After = findSensorWithLotjuId(station2After, 3, false);

        assertNotNull(sensor36Initial);
        assertNull(sensor36After);

        assertEquals(CollectionStatus.GATHERING,
                     findWithNaturalId(stationsAfter, 1035).getProperties().getCollectionStatus());
    }

    private WeatherStationFeatureSimpleV1 findWithNaturalId(final WeatherStationFeatureCollectionSimpleV1 collection, final long naturalId) {
        return collection.getFeatures().stream()
                        .filter(x -> x.getProperties().id == naturalId)
                        .findFirst().orElse(null);
    }

    private RoadStationSensor findSensorWithLotjuId(final WeatherStationFeatureDetailedV1 feature, final long lotjuId, final boolean initial) {
        final RoadStationSensor sensor = initial ? sensorsInitial.get(lotjuId) : sensorsAfterChange.get(lotjuId);
        if (sensor != null) {
            final Optional<Long> optional =
                feature.getProperties().sensors.stream()
                    .filter(naturalId -> naturalId == sensor.getNaturalId())
                    .findFirst();
            return optional.isPresent() ? sensor : null;
        }
        return null;
    }
}
