package fi.livi.digitraffic.tie.data.service.datex2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.data.dao.Datex2Repository;
import fi.livi.digitraffic.tie.data.model.Datex2;

@TestPropertySource(properties = "Datex2MessageUrl=Datex2Url")
public class Datex2MessageUpdaterTest extends AbstractTest {
    @Autowired
    private Datex2TrafficAlertMessageUpdater messageUpdater;

    @Autowired
    private Datex2Repository datex2Repository;

    private MockRestServiceServer server;

    @Autowired
    private RestTemplate restTemplate;

    private final String datex2Url = "Datex2Url";

    @Before
    public void before() {
        datex2Repository.deleteAll();

        server = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    @Rollback
    public void updateDatex2MessagesSucceeds() throws IOException {
        server.expect(MockRestRequestMatchers.requestTo(datex2Url + "?F=0&C=N&O=D"))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(MockRestResponseCreators.withSuccess(readResourceContent("classpath:lotju/datex2/datex2FileList1.html"), MediaType.TEXT_HTML));

        server.expect(MockRestRequestMatchers.requestTo(datex2Url + "Datex2_2017-08-10-15-59-34-896.xml"))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(MockRestResponseCreators.withSuccess(readResourceContent("classpath:lotju/datex2/Datex2_2017-08-10-15-59-34-896.xml"), MediaType.APPLICATION_XML));

        messageUpdater.updateDatex2TrafficAlertMessages();
        server.verify();

        List<Datex2> datex2s = datex2Repository.findAll();
        assertEquals(1, datex2s.size());
        assertEquals(1, datex2s.get(0).getSituations().size());
        assertEquals("GUID50013339", datex2s.get(0).getSituations().get(0).getSituationId());
        assertNull(datex2s.get(0).getSituations().get(0).getSituationRecords().get(0).getOverallEndTime());


        server = MockRestServiceServer.createServer(restTemplate);

        server.expect(MockRestRequestMatchers.requestTo(datex2Url + "?F=0&C=N&O=D"))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(MockRestResponseCreators.withSuccess(readResourceContent("classpath:lotju/datex2/datex2FileList2.html"), MediaType.TEXT_HTML));

        server.expect(MockRestRequestMatchers.requestTo(datex2Url + "Datex2_2017-08-10-16-08-32-976.xml"))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(MockRestResponseCreators.withSuccess(readResourceContent("classpath:lotju/datex2/Datex2_2017-08-10-16-08-32-976.xml"), MediaType.APPLICATION_XML));

        server.expect(MockRestRequestMatchers.requestTo(datex2Url + "Datex2_2017-08-10-16-10-01-680.xml"))
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andRespond(MockRestResponseCreators.withSuccess(readResourceContent("classpath:lotju/datex2/Datex2_2017-08-10-16-10-01-680.xml"), MediaType.APPLICATION_XML));

        messageUpdater.updateDatex2TrafficAlertMessages();
        server.verify();

        datex2s = datex2Repository.findAll(new Sort(Sort.Direction.ASC, "importTime"));
        assertEquals(3, datex2s.size());
        assertEquals("GUID50013339", datex2s.get(0).getSituations().get(0).getSituationId());
        assertEquals("GUID50013340", datex2s.get(1).getSituations().get(0).getSituationId());
        assertEquals("GUID50013339", datex2s.get(2).getSituations().get(0).getSituationId());
        assertEquals(ZonedDateTime.parse("2017-08-10T16:09:26.231+03:00").withNano(0).toInstant(), datex2s.get(2).getSituations().get(0).getSituationRecords().get(0).getOverallEndTime().toInstant());
    }
}