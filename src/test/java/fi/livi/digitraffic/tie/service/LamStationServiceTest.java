package fi.livi.digitraffic.tie.service;

import java.util.List;

import org.geojson.FeatureCollection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import fi.livi.digitraffic.tie.MetadataApplication;
import fi.livi.digitraffic.tie.model.LamStation;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MetadataApplication.class)
@WebAppConfiguration
public class LamStationServiceTest {
    @Autowired
    private LamStationService lamStationService;

    @Test
    public void testFindAll() {
        final List<LamStation> stations = lamStationService.findAll();
        Assert.assertEquals(6, stations.size());
    }

    @Test
    public void testFindAllNonObsoleteLamStationsAsFeatureCollection() {
        final FeatureCollection stations = lamStationService.findAllNonObsoleteLamStationsAsFeatureCollection();
        Assert.assertEquals(5, stations.getFeatures().size());
    }

}
