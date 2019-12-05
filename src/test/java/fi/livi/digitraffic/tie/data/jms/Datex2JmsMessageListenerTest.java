package fi.livi.digitraffic.tie.data.jms;

import static fi.livi.digitraffic.tie.data.model.Datex2MessageType.TRAFFIC_INCIDENT;
import static fi.livi.digitraffic.tie.helper.AssertHelper.assertCollectionSize;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.data.dao.Datex2Repository;
import fi.livi.digitraffic.tie.data.jms.marshaller.Datex2MessageMarshaller;
import fi.livi.digitraffic.tie.data.model.Datex2;
import fi.livi.digitraffic.tie.data.service.Datex2DataService;
import fi.livi.digitraffic.tie.data.service.Datex2UpdateService;
import fi.livi.digitraffic.tie.data.service.datex2.Datex2MessageDto;
import fi.livi.digitraffic.tie.data.service.datex2.Datex2SimpleMessageUpdater;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.Situation;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.SituationPublication;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.response.TimestampedTrafficDisorderDatex2;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.response.TrafficDisordersDatex2Response;

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

    @Autowired
    private Datex2SimpleMessageUpdater datex2SimpleMessageUpdater;

    @Test
    public void datex2ReceiveMessages() throws IOException {
        datex2Repository.deleteAll();

        final String SITUATION_ID_1 = "GUID50006936";
        final String SITUATION_ID_2 = "GUID50006401";
        final JMSMessageListener datexJmsMessageListener = createJmsMessageListener();

        final List<Resource> datex2Resources = loadResources("classpath:lotju/datex2/InfoXML_*.xml");
        readAndSendMessages(datex2Resources, datexJmsMessageListener, false);

        final D2LogicalModel active = datex2DataService.findActive(0, TRAFFIC_INCIDENT);

        List<Situation> situations = ((SituationPublication) active.getPayloadPublication()).getSituations();

        assertCollectionSize(1, situations);
        assertEquals(SITUATION_ID_1, situations.get(0).getId());

        final List<Datex2> bySituationDatex2s = datex2Repository.findBySituationIdAndMessageType(SITUATION_ID_1, TRAFFIC_INCIDENT.name());
        assertCollectionSize(1, bySituationDatex2s);
        assertEquals(SITUATION_ID_1, bySituationDatex2s.get(0).getSituations().get(0).getSituationId());

        final List<Datex2> bySituation2Datex2s = datex2Repository.findBySituationIdAndMessageType(SITUATION_ID_2, TRAFFIC_INCIDENT.name());
        assertCollectionSize(3, bySituation2Datex2s);

        for (final Datex2 datex2 : bySituation2Datex2s) {
            assertEquals(SITUATION_ID_2, datex2.getSituations().get(0).getSituationId());
        }

        final TrafficDisordersDatex2Response byTimeSituation2 = datex2DataService.findTrafficDisorders(null, 2016, 10);
        final List<TimestampedTrafficDisorderDatex2> byTimeSituation22Datex2s = byTimeSituation2.getDisorders();
        assertCollectionSize(6, byTimeSituation22Datex2s);
    }

    @Test
    public void combinedMessageUpdated() throws IOException {
        datex2Repository.deleteAll();

        final String SITUATION1_ID = "GUID50365428";
        final String SITUATION2_ID = "GUID50365429";
        final String SITUATION2_END_PLACEHOLDER = "DISORDER2_END_PLACEHOLDER";
        final JMSMessageListener datexJmsMessageListener = createJmsMessageListener();

        final List<Resource> datex2Resources = loadResources("classpath:lotju/datex2/Datex2_2019-11-26-14-35-08-487.xml");
        readAndSendMessages(datex2Resources, datexJmsMessageListener, false, SITUATION2_END_PLACEHOLDER,
                            DateHelper.toXMLGregorianCalendarAtUtc(Instant.now().plusSeconds(600)).toString());

        final List<Situation> active = getActiveSituations(0);
        assertCollectionSize(2, active);
        assertEquals(SITUATION1_ID, active.get(0).getId());
        assertEquals(SITUATION2_ID, active.get(1).getId());
        final List<Resource> datex2Resources2Updated = loadResources("classpath:lotju/datex2/Datex2_2019-11-26-15-35-08-487.xml");

        readAndSendMessages(datex2Resources2Updated, datexJmsMessageListener, false, SITUATION2_END_PLACEHOLDER,
            DateHelper.toXMLGregorianCalendarAtUtc(Instant.now().minusSeconds(600)).toString());

        final List<Situation> activeAfterUpdate = getActiveSituations(0);
        assertCollectionSize(1, activeAfterUpdate);
        assertEquals(SITUATION1_ID, activeAfterUpdate.  get(0).getId());
    }

    private List<Situation> getActiveSituations(final int inactiveHours) {
        final D2LogicalModel active = datex2DataService.findActive(inactiveHours, TRAFFIC_INCIDENT);
        return ((SituationPublication) active.getPayloadPublication()).getSituations();
    }
    private JMSMessageListener createJmsMessageListener() {
        final JMSMessageListener.JMSDataUpdater<Datex2MessageDto> dataUpdater = (data) -> datex2UpdateService.updateTrafficAlerts(data);
        return new JMSMessageListener(new Datex2MessageMarshaller(jaxb2Marshaller, datex2SimpleMessageUpdater), dataUpdater, false, log);
    }

    private void readAndSendMessages(final List<Resource> datex2Resources, final JMSMessageListener messageListener,
                                            final boolean autoFix) throws IOException {
        readAndSendMessages(datex2Resources, messageListener, autoFix, null, null);
    }

    private void readAndSendMessages(final List<Resource> datex2Resources, final JMSMessageListener messageListener,
        final boolean autoFix, final String  placeholderName, final String replacement) throws IOException {
        log.info("Read and send " + datex2Resources.size() + " Datex2 messages...");
        for (final Resource datex2Resource : datex2Resources) {
            final File datex2file = datex2Resource.getFile();
            log.info("Datex2file={}", datex2file.getName());
            String content = FileUtils.readFileToString(datex2file, StandardCharsets.UTF_8);
            if (placeholderName != null && replacement != null) {
                log.info("Replace {} with {}", placeholderName, replacement);
                content = content.replace(placeholderName, replacement);
            }
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
