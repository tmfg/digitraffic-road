package fi.livi.digitraffic.tie.data.service.datex2;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.data.dao.Datex2Repository;
import fi.livi.digitraffic.tie.data.model.Datex2;
import fi.livi.digitraffic.tie.data.service.Datex2DataService;

public class Datex2MessageServiceTest extends AbstractTest {

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    private Datex2MessageService datex2MessageService;

    @MockBean
    private Datex2HttpClient datex2HttpClient;

    @Autowired
    private Datex2Repository datex2Repository;

    @Autowired
    private Jaxb2Marshaller jaxb2Marshaller;

    @Autowired
    private Datex2DataService datex2DataService;

    private String datex2Content1;
    private String datex2Content2;
    private ZonedDateTime datex2PublicationTime1;
    private ZonedDateTime datex2PublicationTime2;

    @Before
    public void before() throws IOException {
        datex2MessageService = new Datex2MessageService(datex2Repository, datex2HttpClient, jaxb2Marshaller, datex2DataService);

        datex2Repository.deleteAll();

        datex2Content1 = readResourceContent("classpath:lotju/datex2/InfoXML_2016-09-12-20-51-24-602.xml");
        datex2PublicationTime1 = ZonedDateTime.parse("2016-09-12T20:51:00.587+03:00");
        datex2Content2 = readResourceContent("classpath:lotju/datex2/InfoXML_2016-11-17-18-34-36-299.xml");
        datex2PublicationTime2 = ZonedDateTime.parse("2016-11-17T18:32:05.334+02:00");
    }

    @Test
    public void updateDatex2MessagesSucceeds() {

        when(datex2HttpClient.getDatex2MessagesFrom(Matchers.any())).thenReturn(Arrays.asList(datex2Content1));
        datex2MessageService.updateDatex2Messages();

        List<Datex2> datex2s = datex2Repository.findByPublicationTime(datex2PublicationTime1.withNano(0));
        assertEquals(1, datex2s.size());
        assertEquals(1, datex2s.get(0).getSituations().size());
        assertEquals("GUID50005166", datex2s.get(0).getSituations().get(0).getSituationId());

        datex2s = datex2Repository.findByPublicationTime(datex2PublicationTime2.withNano(0));
        assertEquals(0, datex2s.size());

        when(datex2HttpClient.getDatex2MessagesFrom(Matchers.any())).thenReturn(Arrays.asList(datex2Content1, datex2Content2));
        datex2MessageService.updateDatex2Messages();

        // datex2Content1 is persisted only once
        datex2s = datex2Repository.findByPublicationTime(datex2PublicationTime1.withNano(0));
        assertEquals(1, datex2s.size());
        assertEquals(1, datex2s.get(0).getSituations().size());
        assertEquals("GUID50005166", datex2s.get(0).getSituations().get(0).getSituationId());

        datex2s = datex2Repository.findByPublicationTime(datex2PublicationTime2.withNano(0));
        assertEquals(1, datex2s.size());
        assertEquals(1, datex2s.get(0).getSituations().size());
        assertEquals("GUID50006936", datex2s.get(0).getSituations().get(0).getSituationId());
    }
}