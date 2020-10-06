package fi.livi.digitraffic.tie.service.jms;

import static fi.livi.digitraffic.tie.helper.AssertHelper.assertCollectionSize;
import static fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType.ROADWORK;
import static fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType.TRAFFIC_INCIDENT;
import static fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType.WEIGHT_RESTRICTION;
import static org.apache.commons.collections.CollectionUtils.union;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.conf.jms.ExternalIMSMessage;
import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.datex2.Situation;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.datex2.SituationRecord;
import fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncement;
import fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.service.jms.marshaller.ImsMessageMarshaller;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2DataService;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2UpdateService;

public class ImsDatex2JmsMessageListenerTest extends AbstractJmsMessageListenerTest {
    private static final Logger log = LoggerFactory.getLogger(ImsDatex2JmsMessageListenerTest.class);

    @Autowired
    private V2Datex2DataService v2Datex2DataService;

    @Autowired
    private Datex2Repository datex2Repository;

    @Autowired
    private Jaxb2Marshaller jaxb2Marshaller;

    @Autowired
    private V2Datex2UpdateService v2Datex2UpdateService;

    @Before
    public void cleanDbBefore() {
        datex2Repository.deleteAll();
    }

    @Test
    public void datex2ReceiveImsMessagesV1_2_0JsonV0_2_4() throws IOException {
        final String SITUATION_ID_1 = "GUID50001238";
        final JMSMessageListener<ExternalIMSMessage> datexJmsMessageListener = createImsJmsMessageListener();

        final List<Resource> imsResources = loadResources("classpath:tloik/ims/TrafficIncidentImsMessageV1_2_0JsonV0_2_4.xml");
        readAndSendMessages(imsResources, datexJmsMessageListener);
        checkActiveSituations(SITUATION_ID_1);
    }

    @Test
    public void datex2ReceiveImsMessagesV1_2_1JsonV0_2_4() throws IOException {
        datex2Repository.deleteAll();

        final String SITUATION_ID_1 = "GUID50001238";
        final JMSMessageListener<ExternalIMSMessage> datexJmsMessageListener = createImsJmsMessageListener();

        final List<Resource> imsResources = loadResources("classpath:tloik/ims/TrafficIncidentImsMessageV1_2_1JsonV0_2_4.xml");
        readAndSendMessages(imsResources, datexJmsMessageListener);
        checkActiveSituations(SITUATION_ID_1);
    }

    @Test
    public void datex2ReceiveImsMessagesV1_2_0JsonV0_2_6() throws IOException {
        datex2Repository.deleteAll();

        final String SITUATION_ID_1 = "GUID50001238";
        final JMSMessageListener<ExternalIMSMessage> datexJmsMessageListener = createImsJmsMessageListener();

        final List<Resource> imsResources = loadResources("classpath:tloik/ims/TrafficIncidentImsMessageV1_2_0JsonV0_2_6.xml");
        readAndSendMessages(imsResources, datexJmsMessageListener);
        checkActiveSituations(SITUATION_ID_1);
    }

    @Test
    public void datex2ReceiveImsMessagesV1_2_1JsonV0_2_6() throws IOException {
        datex2Repository.deleteAll();

        final String SITUATION_ID_1 = "GUID50001238";
        final JMSMessageListener<ExternalIMSMessage> datexJmsMessageListener = createImsJmsMessageListener();

        final List<Resource> imsResources = loadResources("classpath:tloik/ims/TrafficIncidentImsMessageV1_2_1JsonV0_2_6.xml");
        readAndSendMessages(imsResources, datexJmsMessageListener);
        checkActiveSituations(SITUATION_ID_1);
    }

    @Test
    public void datex2ReceiveImsMessagesV1_2_1JsonV0_2_6WithMultipleMessages() throws IOException {
        datex2Repository.deleteAll();

        final JMSMessageListener<ExternalIMSMessage> datexJmsMessageListener = createImsJmsMessageListener();

        final List<Resource> imsResources = loadResources("classpath:tloik/ims/TrafficIncidentImsMessageV1_2_1JsonV0_2_6MultipleMessages.xml");
        readAndSendMessages(imsResources, datexJmsMessageListener);
        checkActiveSituations("GUID00000001", "GUID00000002", "GUID00000003", "GUID00000004", "GUID00000005", "GUID00000006", "GUID00000007");
    }

    private void checkActiveSituations(final String...situationIdsToFind) {
        final List<Situation> situationIncidents = ((SituationPublication)v2Datex2DataService.findActive(0, TRAFFIC_INCIDENT)
            .getPayloadPublication()).getSituations();
        final List<Situation> situationRoadworks = ((SituationPublication)v2Datex2DataService.findActive(0, ROADWORK)
            .getPayloadPublication()).getSituations();
        final List<Situation> situationWeightRestrictions = ((SituationPublication)v2Datex2DataService.findActive(0, WEIGHT_RESTRICTION)
            .getPayloadPublication()).getSituations();
        final Collection<Situation> situations = union(union(situationIncidents, situationRoadworks), situationWeightRestrictions);

        final List<TrafficAnnouncementFeature> featureIncidents =
            v2Datex2DataService.findActiveJson(0, TRAFFIC_INCIDENT).getFeatures();
        final List<TrafficAnnouncementFeature> featureRoadworks =
            v2Datex2DataService.findActiveJson(0, ROADWORK).getFeatures();
        final List<TrafficAnnouncementFeature> featureWeightRestrictions =
            v2Datex2DataService.findActiveJson(0, WEIGHT_RESTRICTION).getFeatures();
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
            assertEquals(announcement.timeAndDuration.startTime.toInstant(),
                         situationRecord.getValidity().getValidityTimeSpecification().getOverallStartTime());
        }
    }

    private JMSMessageListener<ExternalIMSMessage> createImsJmsMessageListener() {
        final JMSMessageListener.JMSDataUpdater<ExternalIMSMessage> dataUpdater = (data) ->  v2Datex2UpdateService.updateTrafficDatex2ImsMessages(data);
        return new JMSMessageListener<>(new ImsMessageMarshaller(jaxb2Marshaller), dataUpdater, false, log);
    }

    private void readAndSendMessages(final List<Resource> imsResources, final JMSMessageListener<ExternalIMSMessage> messageListener) throws IOException {
        readAndSendMessages(imsResources, messageListener, null, null);
    }

    private void readAndSendMessages(final List<Resource> imsResources, final JMSMessageListener<ExternalIMSMessage> messageListener,
                                     final String  placeholderName, final String replacement) throws IOException {
        log.info("Read and send " + imsResources.size() + " IMS Datex2 messages...");
        for (final Resource datex2Resource : imsResources) {
            final File datex2file = datex2Resource.getFile();
            log.info("Datex2file={}", datex2file.getName());
            String content = FileUtils.readFileToString(datex2file, StandardCharsets.UTF_8);
            if (placeholderName != null && replacement != null) {
                log.info("Replace {} with {}", placeholderName, replacement);
                content = content.replace(placeholderName, replacement);
            }
            try {
                messageListener.onMessage(createTextMessage(content,
                                                            datex2file.getName()));
            } catch (final Exception e) {
                log.error("Error with file " + datex2file.getName());
                throw e;
            }
        }
    }
}
