package fi.livi.digitraffic.tie.metadata.quartz;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.MetadataTest;
import fi.livi.digitraffic.tie.metadata.geojson.roadweather.RoadWeatherStationFeatureCollection;
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
    public void testUpdateLamStations() {

        // Update road weather stations to initial state (2 non obsolete stations and 2 obsolete)
        roadWeatherStationUpdater.updateWeatherStations();
        roadWeatherStationUpdater.updateRoadWeatherSensors();
        RoadWeatherStationFeatureCollection allInitial =
                roadWeatherStationService.findAllNonObsoleteRoadWeatherStationAsFeatureCollection();
        Assert.assertEquals(2, allInitial.getFeatures().size());


        // Now change lotju metadata and update lam stations (3 non obsolete stations and 1 bsolete)
        tiesaaPerustiedotLotjuServiceMock.setStateAfterChange(true);
        roadWeatherStationUpdater.updateWeatherStations();
        roadWeatherStationUpdater.updateRoadWeatherSensors();
        RoadWeatherStationFeatureCollection allAfterChange =
                roadWeatherStationService.findAllNonObsoleteRoadWeatherStationAsFeatureCollection();
        Assert.assertEquals(3, allAfterChange.getFeatures().size());
    }
}
