package fi.livi.digitraffic.tie.data.jms;

import static fi.livi.digitraffic.tie.data.model.Datex2MessageType.TRAFFIC_DISORDER;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.annotation.Rollback;

import fi.livi.digitraffic.tie.data.dao.Datex2Repository;
import fi.livi.digitraffic.tie.data.dto.datex2.Datex2RootDataObjectDto;
import fi.livi.digitraffic.tie.data.jms.marshaller.Datex2MessageMarshaller;
import fi.livi.digitraffic.tie.data.model.Datex2;
import fi.livi.digitraffic.tie.data.service.Datex2DataService;
import fi.livi.digitraffic.tie.data.service.Datex2UpdateService;
import fi.livi.digitraffic.tie.data.service.datex2.Datex2MessageDto;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TimestampedTrafficDisorderDatex2;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TrafficDisordersDatex2Response;

public class Datex2JmsMessageListenerTest extends AbstractJmsMessageListenerTest {
    private static final Logger log = LoggerFactory.getLogger(Datex2JmsMessageListenerTest.class);

    @Autowired
    private Datex2DataService datex2DataService;

    @Autowired
    private Datex2UpdateService datex2UpdateService;

    @Autowired
    private Datex2Repository datex2Repository;

    @Autowired
    private Jaxb2Marshaller jaxb2Marshaller;

    @Test
    public void testDatex2ReceiveMessages() throws JAXBException, DatatypeConfigurationException, IOException {
        log.info("Delete all Datex2 messages");
        datex2Repository.deleteAll();

        final String SITUATION_ID_1 = "GUID50006936";
        final String SITUATION_ID_2 = "GUID50006401";

        // Create listener
        final JMSMessageListener.JMSDataUpdater<Datex2MessageDto> dataUpdater = (data) -> datex2UpdateService.updateTrafficAlerts(data);
        final JMSMessageListener datexJmsMessageListener = new JMSMessageListener(new Datex2MessageMarshaller(jaxb2Marshaller), dataUpdater, false, log);

        final List<Resource> datex2Resources = loadResources("classpath:lotju/datex2/InfoXML_*.xml");
        readAndSendMessages(datex2Resources, datexJmsMessageListener, false);

        final Datex2RootDataObjectDto dto = datex2DataService.findActiveTrafficDisorders(false);
        final List<Datex2> datex2s = dto.getDatex2s();

        assertCollectionSize(1, datex2s);
        assertEquals(SITUATION_ID_1, datex2s.get(0).getSituations().get(0).getSituationId());

        final List<Datex2> bySituationDatex2s = datex2Repository.findBySituationIdAndMessageType(SITUATION_ID_1, TRAFFIC_DISORDER.name());
        assertCollectionSize(1, bySituationDatex2s);
        assertEquals(SITUATION_ID_1, bySituationDatex2s.get(0).getSituations().get(0).getSituationId());

        final List<Datex2> bySituation2Datex2s = datex2Repository.findBySituationIdAndMessageType(SITUATION_ID_2, TRAFFIC_DISORDER.name());
        assertCollectionSize(3, bySituation2Datex2s);

        for (final Datex2 datex2 : bySituation2Datex2s) {
            assertEquals(SITUATION_ID_2, datex2.getSituations().get(0).getSituationId());
        }

        final TrafficDisordersDatex2Response byTimeSituation2 = datex2DataService.findTrafficDisorders(null, 2016, 10);
        final List<TimestampedTrafficDisorderDatex2> byTimeSituation22Datex2s = byTimeSituation2.getDisorder();
        assertCollectionSize(6, byTimeSituation22Datex2s);
    }

    // Just for data importing for testing
    @Ignore
    @Test
    @Rollback(value = false)
    public void testImportData() throws JAXBException, DatatypeConfigurationException, IOException {
        log.info("Delete old messages");
        datex2Repository.deleteAll();

        final JMSMessageListener.JMSDataUpdater<Datex2MessageDto> dataUpdater = (data) -> datex2UpdateService.updateTrafficAlerts(data);

        final JMSMessageListener datexJmsMessageListener =
                new JMSMessageListener(new Datex2MessageMarshaller(jaxb2Marshaller), dataUpdater, false, log);

        log.info("Read Datex2 messages from filesystem");
        final List<Resource> datex2Resources = loadResources("file:/Users/jouniso/tyo/digitraffic/Data/datex2/formatted/ftp.tiehallinto" +
            ".fi/incidents/datex2/InfoXML*.xml");

        readAndSendMessages(datex2Resources, datexJmsMessageListener, true);

        log.info("Persist changes");
    }

    private static void readAndSendMessages(final List<Resource> datex2Resources, final JMSMessageListener messageListener,
        final boolean autoFix) throws IOException {
        log.info("Read and send " + datex2Resources.size() + " Datex2 messages...");
        for (final Resource datex2Resource : datex2Resources) {
            final File datex2file = datex2Resource.getFile();
            final String content = FileUtils.readFileToString(datex2file, StandardCharsets.UTF_8);
            try {
                messageListener.onMessage(createTextMessage(autoFix ?
                                                                        content.replace("Both", "both")
                                                                                .replace("<alertCPoint/>", "") :
                                                                        content,
                                                                  datex2file.getName()));
            } catch (final Exception e) {
                log.error("Error with file " + datex2file.getName());
                throw e;
            }
        }
    }
}
