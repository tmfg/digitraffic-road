package fi.livi.digitraffic.tie.metadata.service.tms;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.base.MetadataIntegrationTest;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.TmsStation;

public class TmsStationServiceTest extends MetadataIntegrationTest {

    @Autowired
    private TmsStationService tmsStationService;

    @Test
    public void testFindAllNonObsoleteTmsStationsAsFeatureCollection() {
        final TmsStationFeatureCollection stations = tmsStationService.findAllNonObsoletePublicTmsStationsAsFeatureCollection(false);
        Assert.assertTrue(stations.getFeatures().size() > 0);
    }

    @Test
    public void testFindAllTmsStationsMappedByByNaturalId() {
        final Map<Long, TmsStation> stations = tmsStationService.findAllTmsStationsMappedByByTmsNaturalId();
        Assert.assertTrue(stations.size() > 0);
    }

    @Test
    public void testFindAllTmsStationsByMappedByLotjuId() {
        final Map<Long, TmsStation> stations = tmsStationService.findAllTmsStationsByMappedByLotjuId();
        Assert.assertTrue(stations.size() > 0);
    }

}
