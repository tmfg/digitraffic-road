package fi.livi.digitraffic.tie.metadata.quartz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Optional;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.MetadataTest;
import fi.livi.digitraffic.tie.metadata.geojson.roadweather.RoadStationSensorDto;
import fi.livi.digitraffic.tie.metadata.geojson.roadweather.RoadWeatherStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.roadweather.RoadWeatherStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.service.lotju.TiesaaPerustiedotLotjuServiceMock;
import fi.livi.digitraffic.tie.metadata.service.roadweather.RoadWeatherStationService;
import fi.livi.digitraffic.tie.metadata.service.roadweather.RoadWeatherStationUpdater;

public class RoadWeatherStationUpdateJobTest extends MetadataTest {

    private static final Logger log = Logger.getLogger(RoadWeatherStationUpdateJobTest.class);

    @Autowired
    private RoadWeatherStationUpdater roadWeatherStationUpdater;

    @Autowired
    private RoadWeatherStationService roadWeatherStationService;

    @Autowired
    private TiesaaPerustiedotLotjuServiceMock tiesaaPerustiedotLotjuServiceMock;

    @Test
    public void testUpdateRoadWeatherStations() {

        tiesaaPerustiedotLotjuServiceMock.initDataAndService();

        // Update road weather stations to initial state (2 non obsolete stations and 2 obsolete)
        roadWeatherStationUpdater.updateWeatherStations();
        roadWeatherStationUpdater.updateRoadStationSensors();
        RoadWeatherStationFeatureCollection allInitial =
                roadWeatherStationService.findAllNonObsoleteRoadWeatherStationAsFeatureCollection();
        assertEquals(2, allInitial.getFeatures().size());

        // Now change lotju metadata and update lam stations (3 non obsolete stations and 1 bsolete)
        tiesaaPerustiedotLotjuServiceMock.setStateAfterChange(true);
        roadWeatherStationUpdater.updateWeatherStations();
        roadWeatherStationUpdater.updateRoadStationSensors();
        RoadWeatherStationFeatureCollection allAfterChange =
                roadWeatherStationService.findAllNonObsoleteRoadWeatherStationAsFeatureCollection();
        assertEquals(3, allAfterChange.getFeatures().size());

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

        RoadWeatherStationFeature before = findWithLotjuId(allInitial, 34);
        RoadWeatherStationFeature after = findWithLotjuId(allAfterChange, 34);

        assertEquals(before.getProperties().getName() + "R", after.getProperties().getName());

        assertEquals(after.getProperties().getCollectionStatus(), CollectionStatus.GATHERING);

        assertEquals(before.getProperties().getNames().get("fi"), "Tie 3 Helsinki, Pirkkola");
        assertEquals(before.getProperties().getNames().get("sv"), "Väg 3 Helsingfors, Britas");
        assertEquals(before.getProperties().getNames().get("en"), "Road 3 Helsinki, Pirkkola");

        assertEquals(after.getProperties().getNames().get("fi"), "Tie 3 Helsinki, Kirkkola");
        assertEquals(after.getProperties().getNames().get("sv"), "Väg 3 Helsingfors, Kyrka");
        assertEquals(after.getProperties().getNames().get("en"), "Road 3 Helsinki, Kirkkola");

        assertEquals(before.getProperties().getDistanceFromRoadPartStart(), (Integer) 4915);
        assertEquals(after.getProperties().getDistanceFromRoadPartStart(), (Integer) 5915);

        assertEquals(before.getGeometry().getCoordinates().get(0), (Double) 383971.0);
        assertEquals(after.getGeometry().getCoordinates().get(0), (Double) 383970.0);

        assertEquals(before.getGeometry().getCoordinates().get(1), (Double) 6678800.0);
        assertEquals(after.getGeometry().getCoordinates().get(1), (Double) 6678801.0);

        assertEquals(before.getGeometry().getCoordinates().get(2), (Double) 0.0);
        assertEquals(after.getGeometry().getCoordinates().get(2), (Double) 1.0);

        RoadWeatherStationFeature initial36 = findWithLotjuId(allInitial, 36);
        RoadWeatherStationFeature after36 = findWithLotjuId(allAfterChange, 36);

        RoadStationSensorDto sensorInitial = findSensorWithLotjuId(initial36, 1);
        RoadStationSensorDto sensorAfter = findSensorWithLotjuId(after36, 1);

        assertEquals("Ilman nopeus", sensorInitial.getDescription());
        assertEquals("Ilman lampotila", sensorAfter.getDescription());

        assertEquals("°CC", sensorInitial.getUnit());
        assertEquals("°C", sensorAfter.getUnit());

        assertEquals(10, sensorInitial.getAccuracy().intValue());
        assertEquals(1, sensorAfter.getAccuracy().intValue());

        RoadStationSensorDto sensor2Initial = findSensorWithLotjuId(initial36, 2);
        RoadStationSensorDto sensor2After = findSensorWithLotjuId(after36, 2);

        assertNull(sensor2Initial);
        assertNotNull(sensor2After);

        RoadStationSensorDto sensor3Initial = findSensorWithLotjuId(initial36, 3);
        RoadStationSensorDto sensor3After = findSensorWithLotjuId(after36, 3);

        assertNotNull(sensor3Initial);
        assertNull(sensor3After);

        assertEquals(CollectionStatus.GATHERING,
                     findWithLotjuId(allAfterChange, 35).getProperties().getCollectionStatus());
    }

    private RoadWeatherStationFeature findWithLotjuId(RoadWeatherStationFeatureCollection collection, long lotjuId) {
        Optional<RoadWeatherStationFeature> initial =
                collection.getFeatures().stream()
                        .filter(x -> x.getProperties().getLotjuId() == lotjuId)
                        .findFirst();
        return initial.orElse(null);
    }

    private RoadStationSensorDto findSensorWithLotjuId(RoadWeatherStationFeature feature, long lotjuId) {
        Optional<RoadStationSensorDto> initial =
                feature.getProperties().getSensors().stream()
                        .filter(x -> x.getLotjuId() == lotjuId)
                        .findFirst();
        return initial.orElse(null);
    }
}
