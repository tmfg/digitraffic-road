package fi.livi.digitraffic.tie.data.service.traveltime;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import fi.livi.digitraffic.tie.base.AbstractMetadataIntegrationTest;
import fi.livi.digitraffic.tie.data.dao.DayDataRepository;
import fi.livi.digitraffic.tie.data.dao.TrafficFluencyRepository;
import fi.livi.digitraffic.tie.data.dto.daydata.LinkMeasurementDataDto;
import fi.livi.digitraffic.tie.data.dto.trafficfluency.LatestMedianDataDto;

public class TravelTimeUpdaterIntegrationTest extends AbstractMetadataIntegrationTest {

    @Autowired
    private TravelTimeUpdater travelTimeUpdater;

    @Autowired
    private TrafficFluencyRepository trafficFluencyRepository;

    @Autowired
    private DayDataRepository dayDataRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    private TravelTimeClient travelTimeClient;

    private MockRestServiceServer server;

    private RestTemplate restTemplate = new RestTemplate();

    private final static ZonedDateTime requestStartTime = ZonedDateTime.of(1975, 2, 6, 10, 00, 0, 0, ZoneId.of("UTC"));
    private final static String expectedUri = "travelTimeUri?starttime=" + TravelTimeClient.getDateString(requestStartTime);

    @SuppressWarnings("Duplicates")
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
    public void updateLatestMediansSucceeds() throws IOException {

        server.expect(MockRestRequestMatchers.requestTo(expectedUri))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(getMediansResponse(), MediaType.APPLICATION_XML));

        travelTimeUpdater.updateMedians(requestStartTime);
        server.verify();

        List<LatestMedianDataDto> latestMedians = trafficFluencyRepository.findLatestMediansForLink(6);
        assertEquals(1, latestMedians.size());
        assertEquals(1167L, latestMedians.get(0).getMedianJourneyTime().longValue());
        assertEquals(new BigDecimal("26.767"), latestMedians.get(0).getMedianSpeed());
        assertEquals(2, latestMedians.get(0).getNobs().intValue());
    }

    @Test
    public void updateIndividualMeasurementsSucceeds() throws IOException {
        final File file = new File(getClass().getClassLoader().getResource("traveltime/pks_measurements_response.xml").getFile());
        final String response = FileUtils.readFileToString(file, "UTF-8");

        server.expect(MockRestRequestMatchers.requestTo(expectedUri))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.APPLICATION_XML));

        travelTimeUpdater.updateIndividualMeasurements(requestStartTime);

        final List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM JOURNEYTIME_MEASUREMENT ORDER BY END_TIMESTAMP ASC");
        final Map<String, Object> first = rows.get(0);
        assertEquals("209", first.get("TRAVEL_TIME").toString());
        assertEquals("148", first.get("LINK_ID").toString());
        final Timestamp end_timestamp = (Timestamp) first.get("END_TIMESTAMP");
        assertEquals(ZonedDateTime.of(1975, 2, 6, 12, 0, 0, 0, ZoneId.systemDefault()).toInstant(), end_timestamp.toInstant());
    }

    private String getMediansResponse() throws IOException {
        final File file = new File(getClass().getClassLoader().getResource("traveltime/pks_medians_response.xml").getFile());
        return FileUtils.readFileToString(file, "UTF-8");
    }

}
