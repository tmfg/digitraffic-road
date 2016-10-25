package fi.livi.digitraffic.tie.metadata.service.lam;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractMetadataWebTest;
import fi.livi.digitraffic.tie.metadata.geojson.lamstation.LamStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.LamStation;

public class LamStationServiceTest extends AbstractMetadataWebTest {
    @Autowired
    private LamStationService lamStationService;

    @Test
    public void testFindAllNonObsoleteLamStationsAsFeatureCollection() {
        final LamStationFeatureCollection stations = lamStationService.findAllNonObsoletePublicLamStationsAsFeatureCollection(false);
        Assert.assertTrue(stations.getFeatures().size() > 0);
    }

    @Test
    public void testFindAllLamStationsMappedByByNaturalId() {
        final Map<Long, LamStation> stations = lamStationService.findAllLamStationsMappedByByLamNaturalId();
        Assert.assertTrue(stations.size() > 0);
    }

}
