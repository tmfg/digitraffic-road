package fi.livi.digitraffic.tie.service.lam;

import java.util.List;
import java.util.Map;

import fi.livi.digitraffic.tie.MetadataApplication;
import fi.livi.digitraffic.tie.geojson.FeatureCollection;
import fi.livi.digitraffic.tie.model.LamStation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MetadataApplication.class)
@WebAppConfiguration
public class LamStationServiceTest {
    @Autowired
    private LamStationService lamStationService;

    @Test
    public void testFindAll() {
        final List<LamStation> stations = lamStationService.findAll();
        Assert.assertEquals(454, stations.size());
    }

    @Test
    public void testFindAllNonObsoleteLamStationsAsFeatureCollection() {
        final FeatureCollection stations = lamStationService.findAllNonObsoleteLamStationsAsFeatureCollection();
        Assert.assertEquals(454, stations.getFeatures().size());
    }

    @Test
    public void testFindAllLamStationsMappedByByNaturalId() {
        final Map<Long, LamStation> stations = lamStationService.findAllLamStationsMappedByByNaturalId();
        Assert.assertEquals(454, stations.size());
    }

}
