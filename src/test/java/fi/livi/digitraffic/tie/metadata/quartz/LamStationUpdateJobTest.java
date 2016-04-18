package fi.livi.digitraffic.tie.metadata.quartz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Optional;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.MetadataTest;
import fi.livi.digitraffic.tie.metadata.geojson.lamstation.LamStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.lamstation.LamStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.service.lam.LamStationService;
import fi.livi.digitraffic.tie.metadata.service.lam.LamStationUpdater;
import fi.livi.digitraffic.tie.metadata.service.lotju.LamMetatiedotLotjuServiceMock;

public class LamStationUpdateJobTest extends MetadataTest {

    private static final Logger log = Logger.getLogger(LamStationUpdateJobTest.class);

    @Autowired
    private LamStationUpdater lamStationUpdater;

    @Autowired
    private LamStationService lamStationService;

    @Autowired
    private LamMetatiedotLotjuServiceMock lamMetatiedotLotjuServiceMock;

    @Test
    public void testUpdateLamStations() {

        lamMetatiedotLotjuServiceMock.initDataAndService();

        // Update lamstations to initial state (2 non obsolete stations and 2 obsolete)
        lamStationUpdater.updateLamStations();
        LamStationFeatureCollection allInitial =
                lamStationService.findAllNonObsoleteLamStationsAsFeatureCollection();
        assertEquals(2, allInitial.getFeatures().size());

        // Now change lotju metadata and update lam stations (3 non obsolete stations and 1 bsolete)
        lamMetatiedotLotjuServiceMock.setStateAfterChange(true);
        lamStationUpdater.updateLamStations();
        LamStationFeatureCollection allAfterChange =
                lamStationService.findAllNonObsoleteLamStationsAsFeatureCollection();
        assertEquals(3, allAfterChange.getFeatures().size());


        assertNotNull(findWithLotjuId(allInitial, 1));
        assertNull(findWithLotjuId(allInitial, 2));
        assertNotNull(findWithLotjuId(allInitial, 310));
        assertNull(findWithLotjuId(allInitial, 581));

        assertNotNull(findWithLotjuId(allAfterChange, 1));
        assertNull(findWithLotjuId(allAfterChange, 2));
        assertNotNull(findWithLotjuId(allAfterChange, 310));
        assertNotNull(findWithLotjuId(allAfterChange, 581));

        assertEquals(CollectionStatus.GATHERING, findWithLotjuId(allAfterChange, 581).getProperties().getCollectionStatus());

        /*
        <id>310</id>
            <kuvaus>Liikennemittausasema</kuvaus> -> Liikennemittausasema 1
            <nimi>L_vt5_Iisalmi</nimi> -> L_vt5_Idensalmi
            <korkeus>0</korkeus> -> 1
            <latitudi>7048448</latitudi> -> 7048449
            <longitudi>512364</longitudi> -> 512365
            <tieosoite>
            <etaisyysTieosanAlusta>4750</etaisyysTieosanAlusta> -> 3750
            <tienumero>5</tienumero>
            <tieosa>217</tieosa>
            </tieosoite>
            <tieosoiteId>24</tieosoiteId>
            <keruuVali>300</keruuVali> -> 200
            <keruunTila>KERUUSSA</keruunTila>
            <maakunta>Pohjois-Savo</maakunta> -> Pohjois-Savvoo
            <maakuntaKoodi>11</maakuntaKoodi>
            <nimiEn>Road 5 Iisalmi</nimiEn> -> Road 5 Idensalmi
            <nimiFi>Tie 5 Iisalmi</nimiFi> -> Tie 5 Idensalmi
            <nimiSe>Väg 5 Idensalmi</nimiSe>
            <vanhaId>23826</vanhaId>
            <suunta1Kunta>Kajaani</suunta1Kunta> -> Kajaaniin
            <suunta1KuntaKoodi>205</suunta1KuntaKoodi>
            <suunta2Kunta>Kuopio</suunta2Kunta> -> Kuopioon
            <suunta2KuntaKoodi>297</suunta2KuntaKoodi>
            <tyyppi>DSL</tyyppi>
        */
        LamStationFeature before = findWithLotjuId(allInitial, 310);
        LamStationFeature after = findWithLotjuId(allAfterChange, 310);

        assertEquals("Liikennemittausasema", before.getProperties().getDescription());
        assertEquals("Liikennemittausasema 1", after.getProperties().getDescription());

        assertEquals("L_vt5_Iisalmi", before.getProperties().getName());
        assertEquals("L_vt5_Idensalmi", after.getProperties().getName());

        assertEquals((Double) 7048448.0, before.getGeometry().getCoordinates().get(0));
        assertEquals((Double) 7048449.0, after.getGeometry().getCoordinates().get(0));
        assertEquals((Double) 512364.0, before.getGeometry().getCoordinates().get(1));
        assertEquals((Double) 512365.0, after.getGeometry().getCoordinates().get(1));
        assertEquals((Double) 0.0, before.getGeometry().getCoordinates().get(2));
        assertEquals((Double) 1.0, after.getGeometry().getCoordinates().get(2));

        assertEquals((Integer) 4750, before.getProperties().getDistance());
        assertEquals((Integer) 3750, after.getProperties().getDistance());

        assertEquals((Integer) 300, before.getProperties().getCollectionInterval());
        assertEquals((Integer) 200, after.getProperties().getCollectionInterval());

        assertEquals("Pohjois-Savo", before.getProperties().getProvince());
        assertEquals("Pohjois-Savvoo", after.getProperties().getProvince());

        assertEquals("Tie 5 Iisalmi", before.getProperties().getNames().get("fi"));
        assertEquals("Tie 5 Idensalmi", after.getProperties().getNames().get("fi"));

        assertEquals("Väg 5 Idensalmi", before.getProperties().getNames().get("sv"));
        assertEquals("Väg 5 Idensalmi", after.getProperties().getNames().get("sv"));

        assertEquals("Road 5 Iisalmi", before.getProperties().getNames().get("en"));
        assertEquals("Road 5 Idensalmi", after.getProperties().getNames().get("en"));

        assertEquals("Kajaani", before.getProperties().getDirection1Municipality());
        assertEquals("Kajaaniin", after.getProperties().getDirection1Municipality());

        assertEquals("Kuopio", before.getProperties().getDirection2Municipality());
        assertEquals("Kuopioon", after.getProperties().getDirection2Municipality());

    }

    private LamStationFeature findWithLotjuId(LamStationFeatureCollection collection, long lotjuId) {
        Optional<LamStationFeature> initial =
                collection.getFeatures().stream()
                        .filter(x -> x.getProperties().getLotjuId() == lotjuId)
                        .findFirst();
        return initial.orElse(null);
    }
}
