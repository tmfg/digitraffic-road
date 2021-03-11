package fi.livi.digitraffic.tie.service.jms;

import static fi.livi.digitraffic.tie.helper.AssertHelper.assertCollectionSize;
import static fi.livi.digitraffic.tie.helper.DateHelper.withoutNanos;
import static fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType.ROADWORK;
import static fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType.TRAFFIC_INCIDENT;
import static fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType.WEIGHT_RESTRICTION;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.ImsJsonVersion;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.getSituationIdForSituationType;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.readImsMessageResourceContent;
import static org.apache.commons.collections.CollectionUtils.union;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.xml.transform.StringResult;

import fi.livi.digitraffic.tie.conf.jms.ExternalIMSMessage;
import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.Situation;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.datex2.SituationRecord;
import fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.TrafficAnnouncement;
import fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.external.tloik.ims.v1_2_1.ImsMessage;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationType;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.ImsXmlVersion;
import fi.livi.digitraffic.tie.service.jms.marshaller.ImsMessageMarshaller;

public class ImsDatex2JmsMessageListenerTest extends AbstractJmsMessageListenerTest {
    private static final Logger log = LoggerFactory.getLogger(ImsDatex2JmsMessageListenerTest.class);

    @Autowired
    private Datex2Repository datex2Repository;

    @Autowired
    @Qualifier("imsJaxb2Marshaller")
    private Jaxb2Marshaller jaxb2MarshallerimsJaxb2Marshaller;

    @Autowired
    @Qualifier("imsJaxb2Marshaller")
    private Jaxb2Marshaller imsJaxb2Marshaller;

    @Before
    public void cleanDb() {
        datex2Repository.deleteAll();
    }

    @Test
    public void datex2ReceiveImsMessagesAllVersions() throws IOException {
        final JMSMessageListener<ExternalIMSMessage> jmsMessageListener = createImsJmsMessageListener();

        // Only V0_2_12 version is received at the moment
        for (final ImsJsonVersion imsJsonVersion : Arrays.asList(ImsJsonVersion.V0_2_12)) {
            for(final SituationType type : SituationType.values()) {
                cleanDb();
                log.info("Run datex2ReceiveImsMessagesAllVersions with imsJsonVersion={}", imsJsonVersion);
                sendJmsMessage(ImsXmlVersion.V1_2_1, type, imsJsonVersion, jmsMessageListener);
                checkActiveSituations(getSituationIdForSituationType(type));
            }
        }
    }

    public static String readSimpleJsonMessageResourceContent(final ImsJsonVersion jsonVersion) throws IOException {
        return readResourceContent("classpath:tloik/ims/Json" + jsonVersion + ".json");
    }

    private void checkActiveSituations(final String...situationIdsToFind) {
        final List<Situation> situationIncidents = getSituations(getV2Datex2DataService().findActive(0, TRAFFIC_INCIDENT));
        final List<Situation> situationRoadworks = getSituations(getV2Datex2DataService().findActive(0, ROADWORK));
        final List<Situation> situationWeightRestrictions = getSituations(getV2Datex2DataService().findActive(0, WEIGHT_RESTRICTION));
        final Collection<Situation> situations = union(union(situationIncidents, situationRoadworks), situationWeightRestrictions);

        final List<TrafficAnnouncementFeature> featureIncidents =
            getV2Datex2DataService().findActiveJson(0, TRAFFIC_INCIDENT).getFeatures();
        final List<TrafficAnnouncementFeature> featureRoadworks =
            getV2Datex2DataService().findActiveJson(0, ROADWORK).getFeatures();
        final List<TrafficAnnouncementFeature> featureWeightRestrictions =
            getV2Datex2DataService().findActiveJson(0, WEIGHT_RESTRICTION).getFeatures();
        final Collection<TrafficAnnouncementFeature> features = union(union(featureIncidents, featureRoadworks), featureWeightRestrictions);

        assertCollectionSize("Situations size won't match.", situationIdsToFind.length, situations);
        assertCollectionSize("GeoJSON features size won't match.", situationIdsToFind.length, features);

        for (String id : situationIdsToFind) {
            assertTrue(String.format("Situation %s not found in situations", id),situations.stream().anyMatch(s -> s.getId().equals(id)));
            assertTrue(String.format("Situation %s not found in features", id), features.stream().anyMatch(f -> f.getProperties().situationId.equals(id)));
        }

        checkDatex2MatchJson(situationIncidents, featureIncidents);
        for (Situation s : situationIncidents) {
            assertTrue(String.format("Incident situation %s not found in features.", s.getId()),featureIncidents.stream().anyMatch(f -> f.getProperties().situationId.equals(s.getId())));
        }

        for (Situation s : situationWeightRestrictions) {
            assertTrue(String.format("Weight restrictions situation %s not found in features.", s.getId()),featureWeightRestrictions.stream().anyMatch(f -> f.getProperties().situationId.equals(s.getId())));
        }

        for (Situation s : situationRoadworks) {
            assertTrue(String.format("Roadwork situation %s not found in features.", s.getId()),featureRoadworks.stream().anyMatch(f -> f.getProperties().situationId.equals(s.getId())));
        }
    }

    private List<Situation> getSituations(final D2LogicalModel d2) {
        if (d2.getPayloadPublication() == null) {
            return Collections.emptyList();
        }
        final List<Situation> situations = ((SituationPublication) d2.getPayloadPublication()).getSituations();
        return situations != null ? situations : Collections.emptyList();
    }

    private void checkDatex2MatchJson(final List<Situation> situations, final List<TrafficAnnouncementFeature> features) {
        // Assert both contains each other
        for (Situation s : situations) {
            assertTrue(String.format("Situation %s was not found in features.",
                s.getId()), features.stream().anyMatch(f -> f.getProperties().situationId.equals(s.getId())));
        }
        for (TrafficAnnouncementFeature f : features) {
            assertTrue(String.format("Feature %s was not found in situations.",
                f.getProperties().situationId), situations.stream().anyMatch(s -> s.getId().equals(f.getProperties().situationId)));
        }
        // Check Datex2 vs Json content
        for (Situation s : situations) {
            Optional<TrafficAnnouncementFeature> feature =
                features.stream().filter(f -> f.getProperties().situationId.equals(s.getId())).findFirst();
            assertTrue(feature.isPresent());

            final TrafficAnnouncement announcement = feature.get().getProperties().announcements.get(0);
            final SituationRecord situationRecord = s.getSituationRecords().get(0);
            final String situationComment = situationRecord.getGeneralPublicComments().get(0).getComment().getValues().getValues().get(0).getValue();

            assertTrue(String.format("Feature title \"%s\" should exist in situation comment \"%s\"", announcement.title, situationComment),
                       situationComment.contains(announcement.title));
            assertEquals(withoutNanos(announcement.timeAndDuration.startTime.toInstant()),
                         withoutNanos(situationRecord.getValidity().getValidityTimeSpecification().getOverallStartTime()));
        }
    }

    private JMSMessageListener<ExternalIMSMessage> createImsJmsMessageListener() {
        final JMSMessageListener.JMSDataUpdater<ExternalIMSMessage> dataUpdater = (data) ->  getV2Datex2UpdateService().updateTrafficDatex2ImsMessages(data);
        return new JMSMessageListener<>(new ImsMessageMarshaller(jaxb2MarshallerimsJaxb2Marshaller), dataUpdater, false, log);
    }

    private void sendJmsMessage(final ImsXmlVersion xmlVersion, final SituationType situationType, final ImsJsonVersion jsonVersion,
                                final JMSMessageListener<ExternalIMSMessage> messageListener) throws IOException {
        final String xmlImsMessage = readImsMessageResourceContent(xmlVersion, situationType, jsonVersion,
                                                                                                 ZonedDateTime.now().minusHours(1), null);
        createAndSendJmsMessage(xmlImsMessage, messageListener);
    }

    private void createAndSendJmsMessage(final String xmlImsMessage, final JMSMessageListener<ExternalIMSMessage> messageListener) {
        messageListener.onMessage(createTextMessage(xmlImsMessage, getRandomId(1000, 9999).toString()));
    }

    public String convertImsMessageToString(final ImsMessage imsMessage) {
        final StringResult result = new StringResult();
        imsJaxb2Marshaller.marshal(imsMessage, result);
        return result.toString();
    }
}
