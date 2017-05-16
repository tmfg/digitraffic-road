package fi.livi.digitraffic.tie.metadata.quartz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuTiesaaPerustiedotServiceEndpoint;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.digitraffic.tie.metadata.service.weather.WeatherStationSensorUpdater;
import fi.livi.digitraffic.tie.metadata.service.weather.WeatherStationService;
import fi.livi.digitraffic.tie.metadata.service.weather.WeatherStationUpdater;
import fi.livi.digitraffic.tie.metadata.service.weather.WeatherStationsSensorsUpdater;

public class WeatherStationUpdateJobTest extends AbstractTest {

    private static final Logger log = LoggerFactory.getLogger(WeatherStationUpdateJobTest.class);

    @Autowired
    private WeatherStationSensorUpdater weatherStationSensorUpdater;

    @Autowired
    private WeatherStationsSensorsUpdater weatherStationsSensorsUpdater;

    @Autowired
    private WeatherStationUpdater weatherStationUpdater;

    @Autowired
    private WeatherStationService weatherStationService;

    @Autowired
    private LotjuTiesaaPerustiedotServiceEndpoint lotjuTiesaaPerustiedotServiceMock;

    @Autowired
    private RoadStationSensorService roadStationSensorService;

    private WeatherStationFeatureCollection allInitial;
    private WeatherStationFeatureCollection allAfterChange;
    private Map<Long, RoadStationSensor> sensorsInitial;
    private Map<Long, RoadStationSensor> sensorsAfterChange;

    @Before
    public void initData() {
        lotjuTiesaaPerustiedotServiceMock.initDataAndService();

        // Update road weather stations to initial state (2 non obsolete stations and 2 obsolete)
        weatherStationSensorUpdater.updateRoadStationSensors();
        weatherStationUpdater.updateWeatherStations();
        weatherStationsSensorsUpdater.updateWeatherStationsSensors();
        sensorsInitial = roadStationSensorService.findAllRoadStationSensorsMappedByLotjuId(RoadStationType.WEATHER_STATION);
        allInitial =
            weatherStationService.findAllPublishableWeatherStationAsFeatureCollection(false);
        assertEquals(2, allInitial.getFeatures().size());
        sensorsInitial.values().forEach(s -> entityManager.detach(s));

        // Now change lotju metadata and update tms stations (3 non obsolete stations and 1 bsolete)
        lotjuTiesaaPerustiedotServiceMock.setStateAfterChange(true);
        weatherStationSensorUpdater.updateRoadStationSensors();
        weatherStationUpdater.updateWeatherStations();
        weatherStationsSensorsUpdater.updateWeatherStationsSensors();
        sensorsAfterChange = roadStationSensorService.findAllRoadStationSensorsMappedByLotjuId(RoadStationType.WEATHER_STATION);
        allAfterChange =
            weatherStationService.findAllPublishableWeatherStationAsFeatureCollection(false);
        assertEquals(3, allAfterChange.getFeatures().size());
    }

    @Test
    public void testUpdateWeatherStations() {


        /*
        <id>34</id>
            <nimi>vt3_Pirkkola_R</nimi> -> <nimi>vt3_Pirkkola_RR</nimi>
            <keruunTila>POISTETTU_TILAPAISESTI</keruunTila> -> <keruunTila>KERUUSSA</keruunTila>
            <lisakuvaus>Helsinki, Pirkkola</lisakuvaus> -> <lisakuvaus>Helsinki, Kirkkola</lisakuvaus>
            <nimiEn>Road 3 Helsinki, Pirkkola</nimiEn> -> Kirkkola
            <nimiFi>Tie 3 Helsinki, Pirkkola</nimiFi> -> Kirkkola
            <nimiSe>Väg 3 Helsingfors, Britas</nimiSe> -> Kyrka
            <etaisyysTieosanAlusta>4915</etaisyysTieosanAlusta> -> <etaisyysTieosanAlusta>5915</etaisyysTieosanAlusta>
            <latitudi>6678800</latitudi> -> <latitudi>6678801</latitudi>
            <longitudi>383971</longitudi> -> <longitudi>383970</longitudi>
            <korkeus>0</korkeus> -> <korkeus>1</korkeus>

        */


        Assert.assertNull(findWithLotjuId(allInitial, 33));
        Assert.assertNotNull(findWithLotjuId(allInitial, 34));
        Assert.assertNull(findWithLotjuId(allInitial, 35));
        Assert.assertNotNull(findWithLotjuId(allInitial, 36));

        Assert.assertNull(findWithLotjuId(allAfterChange, 33));
        Assert.assertNotNull(findWithLotjuId(allAfterChange, 34));
        Assert.assertNotNull(findWithLotjuId(allAfterChange, 35)); // removed temporary -> gathering
        Assert.assertNotNull(findWithLotjuId(allAfterChange, 36));

        final WeatherStationFeature before = findWithLotjuId(allInitial, 34);
        final WeatherStationFeature after = findWithLotjuId(allAfterChange, 34);

        assertEquals(before.getProperties().getName() + "R", after.getProperties().getName());

        assertEquals(after.getProperties().getCollectionStatus(), CollectionStatus.GATHERING);

        assertEquals(before.getProperties().getNames().get("fi"), "Tie 3 Helsinki, Pirkkola");
        assertEquals(before.getProperties().getNames().get("sv"), "Väg 3 Helsingfors, Britas");
        assertEquals(before.getProperties().getNames().get("en"), "Road 3 Helsinki, Pirkkola");

        assertEquals(after.getProperties().getNames().get("fi"), "Tie 3 Helsinki, Kirkkola");
        assertEquals(after.getProperties().getNames().get("sv"), "Väg 3 Helsingfors, Kyrka");
        assertEquals(after.getProperties().getNames().get("en"), "Road 3 Helsinki, Kirkkola");

        assertEquals(before.getProperties().getRoadAddress().getDistanceFromRoadSectionStart(), (Integer) 4915);
        assertEquals(after.getProperties().getRoadAddress().getDistanceFromRoadSectionStart(), (Integer) 5915);

        assertEquals(before.getProperties().getLongitudeETRS89(), 383971.0, 0.01);
        assertEquals(after.getProperties().getLongitudeETRS89(), 383970.0, 0.01);

        assertEquals(before.getProperties().getLatitudeETRS89(), 6678800.0, 0.01);
        assertEquals(after.getProperties().getLatitudeETRS89(), 6678801.0, 0.01);

        assertEquals(before.getProperties().getAltitudeETRS89(), 0.0, 0.01);
        assertEquals(after.getProperties().getAltitudeETRS89(), 1.0, 0.01);

        final WeatherStationFeature initial36 = findWithLotjuId(allInitial, 36);
        final WeatherStationFeature after36 = findWithLotjuId(allAfterChange, 36);

        final RoadStationSensor sensorInitial = findSensorWithLotjuId(initial36, 1, true);
        final RoadStationSensor sensorAfter = findSensorWithLotjuId(after36, 1, false);

        assertEquals("Ilman nopeus", sensorInitial.getDescription());
        assertEquals("Ilman lampotila", sensorAfter.getDescription());

        assertEquals("°CC", sensorInitial.getUnit());
        assertEquals("°C", sensorAfter.getUnit());

        assertEquals(10, sensorInitial.getAccuracy().intValue());
        assertEquals(1, sensorAfter.getAccuracy().intValue());

        final RoadStationSensor sensor2Initial = findSensorWithLotjuId(initial36, 2, true);
        final RoadStationSensor sensor2After = findSensorWithLotjuId(after36, 2, false);

        assertNull(sensor2Initial);
        assertNotNull(sensor2After);

        final RoadStationSensor sensor36Initial = findSensorWithLotjuId(initial36, 3, true);
        final RoadStationSensor sensor36After = findSensorWithLotjuId(after36, 3, false);

        assertNotNull(sensor36Initial);
        assertNull(sensor36After);

        assertEquals(CollectionStatus.GATHERING,
                     findWithLotjuId(allAfterChange, 35).getProperties().getCollectionStatus());
    }

    private WeatherStationFeature findWithLotjuId(final WeatherStationFeatureCollection collection, final long lotjuId) {
        final Optional<WeatherStationFeature> initial =
                collection.getFeatures().stream()
                        .filter(x -> x.getProperties().getLotjuId() == lotjuId)
                        .findFirst();
        return initial.orElse(null);
    }

    private RoadStationSensor findSensorWithLotjuId(final WeatherStationFeature feature, final long lotjuId, final boolean initial) {
        final RoadStationSensor sensor = initial ? sensorsInitial.get(lotjuId) : sensorsAfterChange.get(lotjuId);
        if (sensor != null) {
            final Optional<Long> optional =
                feature.getProperties().getStationSensors().stream()
                    .filter(naturalId -> naturalId == sensor.getNaturalId())
                    .findFirst();
            return optional.isPresent() ? sensor : null;
        }
        return null;
    }
}
