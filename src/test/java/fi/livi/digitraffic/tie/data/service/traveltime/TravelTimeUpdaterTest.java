package fi.livi.digitraffic.tie.data.service.traveltime;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import fi.livi.digitraffic.tie.base.MetadataTestBase;
import fi.livi.digitraffic.tie.data.dao.DayDataRepository;
import fi.livi.digitraffic.tie.data.dao.TrafficFluencyRepository;
import fi.livi.digitraffic.tie.data.dto.daydata.LinkMeasurementDataDto;
import fi.livi.digitraffic.tie.data.dto.trafficfluency.LatestMedianDataDto;

public class TravelTimeUpdaterTest extends MetadataTestBase {

    @Autowired
    private TravelTimeUpdater travelTimeUpdater;

    @Autowired
    private TrafficFluencyRepository trafficFluencyRepository;

    @Autowired
    private DayDataRepository dayDataRepository;

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    private TravelTimeClient travelTimeClient;

    private MockRestServiceServer server;

    private RestTemplate restTemplate = new RestTemplate();

    private final static ZonedDateTime requestStartTime = ZonedDateTime.of(1975, 2, 6, 10, 00, 0, 0, ZoneId.of("UTC"));
    private final static String expectedUri = "travelTimeUri?starttime=" + TravelTimeClient.getDateString(requestStartTime);

    @Before
    public void before() {
        ReflectionTestUtils.setField(travelTimeClient, "mediansUrl", "travelTimeUri");
        ReflectionTestUtils.setField(travelTimeClient, "individualMeasurementUrl", "travelTimeUri");
        ReflectionTestUtils.setField(travelTimeClient, "username", "username");
        ReflectionTestUtils.setField(travelTimeClient, "password", "password");
        ReflectionTestUtils.setField(travelTimeClient, "restTemplate", restTemplate);
        server = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    @Transactional
    @Rollback
    public void updateMediansSucceeds() throws IOException {

        List<LinkMeasurementDataDto> medianTravelTimes =
                dayDataRepository.getAllMedianTravelTimesForLink(6, requestStartTime.getYear(), requestStartTime.getMonthValue());
        assertEquals(0, medianTravelTimes.size());

        server.expect(MockRestRequestMatchers.requestTo(expectedUri))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(getMediansResponse(), MediaType.APPLICATION_XML));

        travelTimeUpdater.updateMedians(requestStartTime);
        server.verify();

        medianTravelTimes = dayDataRepository.getAllMedianTravelTimesForLink(6, requestStartTime.getYear(), requestStartTime.getMonthValue());

        assertEquals(1, medianTravelTimes.size());
        assertEquals(6, medianTravelTimes.get(0).getLinkId());
        assertEquals(1167, medianTravelTimes.get(0).getMedianTravelTime());
        assertEquals(3, medianTravelTimes.get(0).getFluencyClass());
        assertEquals(26.767, medianTravelTimes.get(0).getAverageSpeed(), 0.001);
        assertEquals(ZonedDateTime.of(1975, 2, 6, 10, 5, 0, 0, ZoneId.of("UTC")), medianTravelTimes.get(0).getMeasuredTime().withZoneSameInstant(ZoneId.of("UTC")));
    }

    @Test
    @Transactional
    @Rollback
    public void updateLatestMediansSucceeds() throws IOException {

        List<LatestMedianDataDto> latestMedians = trafficFluencyRepository.findLatestMediansForLink(6);
        assertEquals(0, latestMedians.size());

        server.expect(MockRestRequestMatchers.requestTo(expectedUri))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(getMediansResponse(), MediaType.APPLICATION_XML));

        travelTimeUpdater.updateMedians(requestStartTime);
        server.verify();

        latestMedians = trafficFluencyRepository.findLatestMediansForLink(6); // linkId? naturalId?
        assertEquals(1, latestMedians.size());
        assertEquals(1167L, latestMedians.get(0).getMedianJourneyTime().longValue());
        assertEquals(new BigDecimal("26.767"), latestMedians.get(0).getMedianSpeed());
        assertEquals(2, latestMedians.get(0).getNobs().intValue());
    }

    private String getMediansResponse() throws IOException {
        final File file = new File(getClass().getClassLoader().getResource("traveltime/pks_medians_response.xml").getFile());
        return FileUtils.readFileToString(file, "UTF-8");
    }

}
