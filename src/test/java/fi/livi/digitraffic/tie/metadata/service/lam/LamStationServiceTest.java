package fi.livi.digitraffic.tie.metadata.service.lam;

import java.util.List;
import java.util.Map;

import fi.livi.digitraffic.tie.MetadataTest;
import fi.livi.digitraffic.tie.geojson.lamstation.LamStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.LamStation;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class LamStationServiceTest extends MetadataTest {
    @Autowired
    private LamStationService lamStationService;

    @Test
    public void testFindAll() {
        final List<LamStation> stations = lamStationService.findAll();
        Assert.assertEquals(454, stations.size());
    }

    @Test
    public void testFindAllNonObsoleteLamStationsAsFeatureCollection() {
        final LamStationFeatureCollection stations = lamStationService.findAllNonObsoleteLamStationsAsFeatureCollection();
        Assert.assertEquals(454, stations.getFeatures().size());
    }

    @Test
    public void testFindAllLamStationsMappedByByNaturalId() {
        final Map<Long, LamStation> stations = lamStationService.findAllLamStationsMappedByByNaturalId();
        Assert.assertEquals(454, stations.size());
    }

}
