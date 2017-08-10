package fi.livi.digitraffic.tie.data.service.datex2;

import java.io.IOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.data.dao.Datex2Repository;
import fi.livi.digitraffic.tie.data.service.Datex2DataService;

public class Datex2MessageIntegrationTest extends AbstractTest {

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    private Datex2MessageUpdater datex2MessageService;

    @MockBean
    private Datex2HttpClient datex2HttpClient;

    @Autowired
    private Datex2Repository datex2Repository;

    @Autowired
    private Jaxb2Marshaller jaxb2Marshaller;

    @Autowired
    private Datex2DataService datex2DataService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RetryTemplate retryTemplate;

    @Before
    public void before() throws IOException {
        datex2HttpClient = new Datex2HttpClient("https://ava.liikennevirasto.fi/incidents/datex2/", restTemplate, retryTemplate);
        datex2MessageService = new Datex2MessageUpdater(datex2Repository, datex2HttpClient, jaxb2Marshaller, datex2DataService);
    }

    @Test
    @Ignore(value = "For manual integration testing")
    public void updateDatex2MessagesSucceeds() {
        datex2MessageService.updateDatex2TrafficAlertMessages();
    }
}
