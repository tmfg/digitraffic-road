package fi.livi.digitraffic.tie.data.service.traveltime;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
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

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.metadata.dao.LinkRepository;
import fi.livi.digitraffic.tie.metadata.model.Link;
import fi.livi.digitraffic.tie.metadata.service.traveltime.TravelTimeLinkMetadataUpdater;

public class TravelTimeLinkMetadataUpdaterTest extends AbstractTest {

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private TravelTimeLinkMetadataUpdater travelTimeLinkMetadataUpdater;

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    private TravelTimeClient travelTimeClient;

    private MockRestServiceServer server;

    private RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("Duplicates")
    @Before
    public void before() {
        ReflectionTestUtils.setField(travelTimeClient, "mediansUrl", "travelTimeUri");
        ReflectionTestUtils.setField(travelTimeClient, "individualMeasurementUrl", "travelTimeUri");
        ReflectionTestUtils.setField(travelTimeClient, "metadataUrl", "metadataUrl");
        ReflectionTestUtils.setField(travelTimeClient, "username", "username");
        ReflectionTestUtils.setField(travelTimeClient, "password", "password");
        ReflectionTestUtils.setField(travelTimeClient, "restTemplate", restTemplate);
        server = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    @Transactional
    @Rollback
    public void updateLinkMetadataSucceeds() throws IOException {

        server.expect(MockRestRequestMatchers.requestTo("metadataUrl"))
              .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
              .andRespond(MockRestResponseCreators.withSuccess(getResponse(), MediaType.APPLICATION_XML));

        travelTimeLinkMetadataUpdater.updateLinkMetadata();

        final List<Link> linksAfter = linkRepository.findByOrderByNaturalId();

        assertEquals("Otaniemi → Konala", linksAfter.get(0).getName());
    }

    private String getResponse() throws IOException {
        final File file = new File(getClass().getClassLoader().getResource("traveltime/pks_static12.xml").getFile());
        return FileUtils.readFileToString(file, "UTF-8");
    }
}
