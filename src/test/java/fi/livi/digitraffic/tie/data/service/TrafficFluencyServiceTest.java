package fi.livi.digitraffic.tie.data.service;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.data.dto.trafficfluency.LatestMedianDataDto;
import fi.livi.digitraffic.tie.data.dto.trafficfluency.TrafficFluencyRootDataObjectDto;
import fi.livi.digitraffic.tie.data.model.FluencyClass;

public class TrafficFluencyServiceTest extends AbstractTest {

    @Autowired
    private TrafficFluencyService trafficFluencyService;

    @Test
    public void testListPreviousDayHistoryDataAll() {
        List<FluencyClass> classes = trafficFluencyService.findAllFluencyClassesOrderByLowerLimitDesc();
        Assert.assertTrue(!classes.isEmpty());
    }

    @Test
    public void testListPreviousDayHistoryData() {
        TrafficFluencyRootDataObjectDto fluency =
                trafficFluencyService.listCurrentTrafficFluencyData(false);
        List<LatestMedianDataDto> medians = fluency.getLatestMedians();
        Assert.assertTrue(!fluency.getLatestMedians().isEmpty());
        Assert.assertTrue(fluency.getLatestMedians().get(0).getMeasuredTime() != null);
    }

    @Test
    public void testLinkHistoryYearMonth() {
        TrafficFluencyRootDataObjectDto linkFluency = trafficFluencyService.listCurrentTrafficFluencyData(4);
        Assert.assertTrue(!linkFluency.getLatestMedians().isEmpty());
        Assert.assertTrue(linkFluency.getLatestMedians().get(0).getMeasuredTime() != null);
    }
}
