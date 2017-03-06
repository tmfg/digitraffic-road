package fi.livi.digitraffic.tie.data.service.traveltime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.data.service.traveltime.dto.TravelTimeMeasurementsDto;
import fi.livi.digitraffic.tie.data.service.traveltime.dto.TravelTimeMediansDto;

public class TravelTimeClientTest extends AbstractTest {

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    private TravelTimeClient travelTimeClient;

    private MockRestServiceServer server;

    private RestTemplate restTemplate = new RestTemplate();

    private static final ZonedDateTime requestStartTime = ZonedDateTime.now().minusHours(1);
    private static final String expectedUri = "travelTimeUri?starttime=" + TravelTimeClient.getDateString(requestStartTime);

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
    public void getMediansSucceeds() throws IOException {

        final File file = new File(getClass().getClassLoader().getResource("traveltime/pks_medians_response.xml").getFile());
        final String response = FileUtils.readFileToString(file, "UTF-8");

        server.expect(MockRestRequestMatchers.requestTo(expectedUri))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.APPLICATION_XML));

        TravelTimeMediansDto data = travelTimeClient.getMedians(requestStartTime);

        assertEquals(300, data.duration);
        assertEquals(Date.from(ZonedDateTime.of(1975, 2, 6, 10, 0, 0, 0, ZoneId.of("UTC")).toInstant()), data.periodStart);
        assertEquals("FI_FINNRA", data.supplier);
        assertEquals("Helsinki_Traveltimes", data.service);
        assertNotNull(data.lastStaticDataUpdate);
        assertEquals(238, data.medians.size());
        assertEquals(7L, data.medians.get(5).linkNaturalId);
        assertEquals(294L, data.medians.get(5).median);
        assertEquals(1, data.medians.get(5).numberOfObservations);

        server.verify();
    }

    @Test
    public void getMeasurementsSucceeds() throws IOException {

        final File file = new File(getClass().getClassLoader().getResource("traveltime/pks_measurements_response.xml").getFile());
        final String response = FileUtils.readFileToString(file, "UTF-8");

        server.expect(MockRestRequestMatchers.requestTo(expectedUri))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.APPLICATION_XML));

        TravelTimeMeasurementsDto data = travelTimeClient.getMeasurements(requestStartTime);

        assertNotNull(data);
        assertEquals(60, data.duration);
        assertEquals(14, data.measurements.size());
        assertEquals(27, data.measurements.get(0).linkNaturalId);
        assertEquals(145, data.measurements.get(0).measurements.get(0).travelTime);
        assertEquals(2, data.measurements.get(0).measurements.get(0).offset);
    }
}