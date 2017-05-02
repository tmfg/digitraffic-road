package fi.livi.digitraffic.tie.metadata.service.tms;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.TmsStation;

public class TmsStationServiceTest extends AbstractTest {

    @Autowired
    private TmsStationService tmsStationService;

    @Test
    public void findAllPublishableTmsStationsAsFeatureCollection() {
        final TmsStationFeatureCollection stations = tmsStationService.findAllPublishableTmsStationsAsFeatureCollection(false, TmsStationService.TmsListType.ACTIVE);
        Assert.assertTrue(stations.getFeatures().size() > 0);
    }

    @Test
    public void findPermanentlyRemovedStations() {
        final TmsStationFeatureCollection stations = tmsStationService.findAllPublishableTmsStationsAsFeatureCollection(false, TmsStationService.TmsListType.REMOVED);
        Assert.assertTrue(stations.getFeatures().size() == 0);
    }

    @Test
    public void findAllStations() {
        final TmsStationFeatureCollection stations = tmsStationService.findAllPublishableTmsStationsAsFeatureCollection(false,
            TmsStationService.TmsListType.BOTH);
        Assert.assertTrue(stations.getFeatures().size() > 0);
    }

    @Test
    public void findAllTmsStationsMappedByByTmsNaturalId() {
        final Map<Long, TmsStation> stations = tmsStationService.findAllTmsStationsMappedByByTmsNaturalId();
        Assert.assertTrue(stations.size() > 0);
    }

    @Test
    public void findAllTmsStationsByMappedByLotjuId() {
        final Map<Long, TmsStation> stations = tmsStationService.findAllTmsStationsByMappedByLotjuId();
        Assert.assertTrue(stations.size() > 0);
    }

}
