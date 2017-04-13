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
        final TmsStationFeatureCollection stations = tmsStationService.findAllPublishableTmsStationsAsFeatureCollection(false);
        Assert.assertTrue(stations.getFeatures().size() > 0);
    }

    @Test
    public void findAllPublishableTmsStationsAsFeatureCollectionObsolete() {
        final TmsStationFeatureCollection stations = tmsStationService.findAllPublicObsoleteTmsStationsAsFeatureCollection(false);
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
