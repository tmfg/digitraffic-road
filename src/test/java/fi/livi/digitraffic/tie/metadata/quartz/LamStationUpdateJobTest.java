package fi.livi.digitraffic.tie.metadata.quartz;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.MetadataTest;
import fi.livi.digitraffic.tie.metadata.geojson.lamstation.LamStationFeatureCollection;
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

        // Update lamstations to initial state (2 non obsolete stations and 2 obsolete)
        lamStationUpdater.updateLamStations();
        LamStationFeatureCollection allInitial =
                lamStationService.findAllNonObsoleteLamStationsAsFeatureCollection();
        Assert.assertEquals(2, allInitial.getFeatures().size());

        // Now change lotju metadata and update lam stations (3 non obsolete stations and 1 bsolete)
        lamMetatiedotLotjuServiceMock.setStateAfterChange(true);
        lamStationUpdater.updateLamStations();
        LamStationFeatureCollection allAfterChange =
                lamStationService.findAllNonObsoleteLamStationsAsFeatureCollection();
        Assert.assertEquals(3, allAfterChange.getFeatures().size());
    }
}
