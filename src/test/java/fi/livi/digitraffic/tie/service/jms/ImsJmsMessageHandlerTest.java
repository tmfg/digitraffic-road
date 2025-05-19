package fi.livi.digitraffic.tie.service.jms;

import static fi.livi.digitraffic.common.util.TimeUtil.withoutMillis;
import static fi.livi.digitraffic.test.util.AssertUtil.assertCollectionSize;
import static fi.livi.digitraffic.tie.TestUtils.entityManagerFlushAndClear;
import static fi.livi.digitraffic.tie.TestUtils.getRandomString;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.ImsJsonVersion;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.getSituationIdForSituationType;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.readImsMessageResourceContent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.activemq.artemis.jms.client.ActiveMQTextMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.conf.kca.artemis.jms.message.ExternalIMSMessage;
import fi.livi.digitraffic.tie.dao.trafficmessage.datex2.Datex2Repository;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.Situation;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.SituationPublication;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.SituationRecord;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.SituationType;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncement;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.ImsXmlVersion;
import fi.livi.digitraffic.tie.service.jms.marshaller.ImsJMSMessageMarshaller;
import fi.livi.digitraffic.tie.service.trafficmessage.v1.TrafficMessageDataServiceV1;
import jakarta.jms.JMSException;

public class ImsJmsMessageHandlerTest extends AbstractJMSMessageHandlerTest {
    private static final Logger log = LoggerFactory.getLogger(ImsJmsMessageHandlerTest.class);

    @Autowired
    private Datex2Repository datex2Repository;

    @Autowired
    @Qualifier("imsJaxb2Marshaller")
    private Jaxb2Marshaller jaxb2MarshallerimsJaxb2Marshaller;

    @Autowired
    private TrafficMessageTestHelper trafficMessageTestHelper;

    @BeforeEach
    public void cleanDbAndInit() {
        datex2Repository.deleteAll();
    }

    @Test
    public void datex2ReceiveImsMessagesAllVersions() throws IOException {
        final JMSMessageHandler<ExternalIMSMessage> imsJMSMessageHandler = createImsJMSMessageHandler();

        // Only V0_2_12 version is received at the moment
        for (final ImsJsonVersion imsJsonVersion : List.of(ImsJsonVersion.getLatestVersion())) {
            for (final SituationType type : SituationType.values()) {
                cleanDbAndInit();
                log.info("Run datex2ReceiveImsMessagesAllVersions with imsJsonVersion={}", imsJsonVersion);
                sendJmsMessage(ImsXmlVersion.V1_2_1, type, imsJsonVersion, imsJMSMessageHandler);
                entityManagerFlushAndClear(entityManager);
                checkActiveSituations(type, getSituationIdForSituationType(type.name()));
            }
        }
    }

    private void checkActiveSituations(final SituationType type, final String... situationIdsToFind) {
        final List<Situation> situations = getSituations(
                Objects.requireNonNull(getV2Datex2DataService().findActive(0, type).getLeft()));

        final List<TrafficAnnouncementFeature> features =
                getV2Datex2DataService().findActiveJson(0, false, type).getFeatures();

        assertCollectionSize("Situations size won't match for type " + type, situationIdsToFind.length, situations);
        assertCollectionSize("GeoJSON features size won't match for type " + type, situationIdsToFind.length, features);

        for (final String id : situationIdsToFind) {
            assertTrue(situations.stream().anyMatch(s -> s.getId().equals(id)),
                    String.format("Situation %s not found in situations", id));
            assertTrue(features.stream().anyMatch(f -> f.getProperties().situationId.equals(id)),
                    String.format("Situation %s not found in features", id));
        }

        checkDatex2MatchJson(situations, features);
    }

    private List<Situation> getSituations(final D2LogicalModel d2) {
        if (d2.getPayloadPublication() == null) {
            return Collections.emptyList();
        }
        final List<Situation> situations = ((SituationPublication) d2.getPayloadPublication()).getSituations();
        return situations != null ? situations : Collections.emptyList();
    }

    private void checkDatex2MatchJson(final List<Situation> situations,
                                      final List<TrafficAnnouncementFeature> features) {
        // Assert both contains each other
        for (final Situation s : situations) {
            assertTrue(features.stream().anyMatch(f -> f.getProperties().situationId.equals(s.getId())),
                    String.format("Situation %s was not found in features.", s.getId()));
        }
        for (final TrafficAnnouncementFeature f : features) {
            assertTrue(situations.stream().anyMatch(s -> s.getId().equals(f.getProperties().situationId)),
                    String.format("Feature %s was not found in situations.", f.getProperties().situationId));
        }
        // Check Datex2 vs Json content
        for (final Situation s : situations) {
            final Optional<TrafficAnnouncementFeature> feature =
                    features.stream().filter(f -> f.getProperties().situationId.equals(s.getId())).findFirst();
            assertTrue(feature.isPresent());

            final TrafficAnnouncement announcement = feature.get().getProperties().announcements.getFirst();
            final SituationRecord situationRecord = s.getSituationRecords().getFirst();
            final String situationComment =
                    situationRecord.getGeneralPublicComments().getFirst().getComment().getValues().getValues().getFirst()
                            .getValue();

            assertTrue(situationComment.contains(announcement.title),
                    String.format("Feature title \"%s\" should exist in situation comment \"%s\"", announcement.title,
                            situationComment));
            assertEquals(withoutMillis(announcement.timeAndDuration.startTime),
                    withoutMillis(situationRecord.getValidity().getValidityTimeSpecification().getOverallStartTime()));
        }
    }

    private JMSMessageHandler<ExternalIMSMessage> createImsJMSMessageHandler() {
        final JMSMessageHandler.JMSDataUpdater<ExternalIMSMessage> dataUpdater =
                (data) -> trafficMessageTestHelper.getV2Datex2UpdateService().updateTrafficDatex2ImsMessages(data);
        return new JMSMessageHandler<>(JMSMessageHandler.JMSMessageType.TRAFFIC_MESSAGE, dataUpdater,
                new ImsJMSMessageMarshaller(jaxb2MarshallerimsJaxb2Marshaller), lockingService.getInstanceId());
    }

    private void sendJmsMessage(final ImsXmlVersion xmlVersion, final SituationType situationType,
                                final ImsJsonVersion jsonVersion,
                                final JMSMessageHandler<ExternalIMSMessage> messageListener) throws IOException {
        final String xmlImsMessage = readImsMessageResourceContent(xmlVersion, situationType.name(), jsonVersion,
                Instant.now().minus(1, ChronoUnit.HOURS), null, false);
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

    private TrafficMessageDataServiceV1 getV2Datex2DataService() {
        return trafficMessageTestHelper.getTrafficMessageDataServiceV1();
    }
}
