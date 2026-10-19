package fi.livi.digitraffic.tie.service.jms;

import static fi.livi.digitraffic.tie.TestUtils.getRandomString;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.readImsMessageResourceContent;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.activemq.artemis.jms.client.ActiveMQTextMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.conf.kca.artemis.jms.message.ExternalIMSMessage;
import fi.livi.digitraffic.tie.dao.data.DataIncomingRepository;
import fi.livi.digitraffic.tie.model.data.IncomingDataTypes;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper;
import fi.livi.digitraffic.tie.service.data.DataUpdatingService;
import fi.livi.digitraffic.tie.service.jms.marshaller.ImsJMSMessageMarshaller;
import jakarta.jms.JMSException;

public class ImsJmsMessageHandlerTest extends AbstractJMSMessageHandlerTest {

    @Autowired
    private DataIncomingRepository dataIncomingRepository;

    @Autowired
    private DataUpdatingService dataUpdatingService;

    @Autowired
    @Qualifier("imsJaxb2Marshaller")
    private Jaxb2Marshaller jaxb2MarshallerimsJaxb2Marshaller;

    @Autowired
    private TrafficMessageTestHelper trafficMessageTestHelper;

    @BeforeEach
    public void cleanDbAndInit() {
        dataIncomingRepository.deleteAll();
    }

    /**
     * Every message is persisted, including one with an unknown type. Deciding whether the content
     * is usable is left to the processing stage, which records the reason on the row.
     *
     * <p>The type is stored with the spelling it arrived with, because the generated enum constants
     * are named after the schema values. A type that is not in the schema is stored as null.</p>
     */
    @ParameterizedTest
    @CsvSource(value = {"DATEX2:3.5:DATEX2", "DATEX2:2.3:DATEX2", "DATEX2:1.1:DATEX2", "DATEX:3.5:"}, delimiter = ':')
    public void handeDatex2(final String messageType, final String version, final String storedType) throws IOException {
        final JMSMessageHandler<ExternalIMSMessage> imsJMSMessageHandler = createImsJMSMessageHandler();

        sendJmsMessage(imsJMSMessageHandler, messageType, version);

        final var stored = dataIncomingRepository.findByMessageId("50003469");
        assertEquals(1, stored.size());

        final String expected = "\"type\":" + (storedType == null ? "null" : "\"" + storedType + "\"")
                + ",\"version\":\"" + version + "\"";
        Assertions.assertTrue(stored.getFirst().getData().contains(expected),
                "Stored message should contain " + expected + ", was: " + stored.getFirst().getData());
    }

    /**
     * A message that cannot be processed is no longer dropped silently. It is kept in the database
     * and marked FAILED with the reason, so it can be found without digging through application logs.
     *
     * <p>The test template carries a single sub-message and no SIMPPELI part, so processing stops at
     * the missing simple json. The content checks in
     * {@link fi.livi.digitraffic.tie.service.data.ImsUpdatingService} are covered by
     * {@link fi.livi.digitraffic.tie.service.data.DataServiceTest}.</p>
     */
    @ParameterizedTest
    @CsvSource(value = {"DATEX", "DATEX2"}, delimiter = ':')
    public void unprocessableMessageIsKeptAndMarkedFailed(final String messageType) throws IOException {
        sendJmsMessage(createImsJMSMessageHandler(), messageType, "3.5");

        dataUpdatingService.handleNewData();

        final var stored = dataIncomingRepository.findByMessageId("50003469");
        assertEquals(1, stored.size());
        assertEquals(IncomingDataTypes.DataStatus.FAILED, stored.getFirst().getStatus());
        Assertions.assertTrue(stored.getFirst().getFailureReason().startsWith("No Simple-json found"),
                "Failure reason should be stored, was: " + stored.getFirst().getFailureReason());
    }

    private JMSMessageHandler<ExternalIMSMessage> createImsJMSMessageHandler() {
        final JMSMessageHandler.JMSDataUpdater<ExternalIMSMessage> dataUpdater =
                (data) -> trafficMessageTestHelper.getImsUpdateService().handleImsMessages(data);
        return new JMSMessageHandler<>(JMSMessageHandler.JMSMessageType.TRAFFIC_MESSAGE, dataUpdater,
                new ImsJMSMessageMarshaller(jaxb2MarshallerimsJaxb2Marshaller), lockingService.getInstanceId());
    }

    private void sendJmsMessage(final JMSMessageHandler<ExternalIMSMessage> messageListener, final String messageType, final String version)
            throws IOException {
        final String xmlImsMessage = readImsMessageResourceContent(messageType, version);

        createAndSendJmsMessage(xmlImsMessage, messageListener);
    }

    private void createAndSendJmsMessage(final String xmlImsMessage,
                                         final JMSMessageHandler<ExternalIMSMessage> messageListener) {
        final ActiveMQTextMessage tm = createTextMessage(xmlImsMessage, getRandomString(4));
        try {
            messageListener.onMessage(tm);
        } catch (final JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
