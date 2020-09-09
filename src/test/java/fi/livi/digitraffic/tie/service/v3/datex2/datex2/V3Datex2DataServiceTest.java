package fi.livi.digitraffic.tie.service.v3.datex2.datex2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.xml.transform.StringSource;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.conf.jaxb2.XmlMarshallerConfiguration;
import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.Situation;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.external.tloik.ims.ImsMessage;
import fi.livi.digitraffic.tie.helper.AssertHelper;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.Feature;
import fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncement;
import fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeatureCollection;
import fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementProperties;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2DataService;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2UpdateService;
import fi.livi.digitraffic.tie.service.v3.datex2.V3Datex2DataService;

@Import({ V3Datex2DataService.class, V2Datex2DataService.class, V2Datex2UpdateService.class, XmlMarshallerConfiguration.class, JacksonAutoConfiguration.class })
public class V3Datex2DataServiceTest extends AbstractServiceTest {

    private final static String GUID_WITH_JSON = "GUID50001238";
    private final static String GUID_NO_JSON = "GUID50001234";

    @Autowired
    private V3Datex2DataService v3Datex2DataService;

    @Autowired
    private V2Datex2DataService v2Datex2DataService;

    @Autowired
    private Jaxb2Marshaller jaxb2Marshaller;

    @Autowired
    private V2Datex2UpdateService v2Datex2UpdateService;

    @Autowired
    private Datex2Repository datex2Repository;

    @Before
    public void cleanDb() {
        datex2Repository.deleteAll();
    }

    @Test
    public void activeIncidentsDatex2AndJsonEqualsV0_2_4() throws IOException {
        // One active
        initDataFromFile("TrafficIncidentImsMessageV0_2_4.xml");
        activeIncidentsDatex2AndJsonEquals(Version.V0_2_4);
    }

    @Test
    public void activeIncidentsDatex2AndJsonEqualsV0_2_5() throws IOException {
        // One active
        initDataFromFile("TrafficIncidentImsMessageV0_2_5.xml");
        activeIncidentsDatex2AndJsonEquals(Version.V0_2_5);
    }

    @Test
    public void findBySituationIdV0_2_4() throws IOException {
        // One active
        initDataFromFile("TrafficIncidentImsMessageV0_2_4.xml");
        findBySituationId();
    }

    @Test
    public void findBySituationIdV0_2_5() throws IOException {
        // One active
        initDataFromFile("TrafficIncidentImsMessageV0_2_5.xml");
        findBySituationId();
    }

    private void findBySituationId() {
        final D2LogicalModel d2 = v2Datex2DataService.findAllBySituationId(GUID_WITH_JSON, Datex2MessageType.TRAFFIC_INCIDENT);
        final TrafficAnnouncementFeatureCollection jsons = v3Datex2DataService.findAllBySituationIdJson(GUID_WITH_JSON, Datex2MessageType.TRAFFIC_INCIDENT);

        final List<Situation> situations = ((SituationPublication) d2.getPayloadPublication()).getSituations();

        AssertHelper.assertCollectionSize(1, situations);
        AssertHelper.assertCollectionSize(1, jsons.getFeatures());
        final Situation situation = situations.get(0);
        final TrafficAnnouncementFeature situationJson = jsons.getFeatures().get(0);

        assertEquals(GUID_WITH_JSON, situation.getId());
        assertEquals(GUID_WITH_JSON, situationJson.getProperties().situationId);
    }

    @Test
    public void findActiveV0_2_4() throws IOException {
        // One active with json
        initDataFromFile("TrafficIncidentImsMessageV0_2_4.xml");
        // One active without json
        initDataFromFile("TrafficIncidentImsMessageWithoutJson.xml");

        assertActiveMessageFound(GUID_WITH_JSON, true, true);
        assertActiveMessageFound(GUID_NO_JSON, true, false);
    }

    @Test
    public void findActiveV0_2_5() throws IOException {
        // One active with json
        initDataFromFile("TrafficIncidentImsMessageV0_2_5.xml");
        // One active without json
        initDataFromFile("TrafficIncidentImsMessageWithoutJson.xml");

        assertActiveMessageFound(GUID_WITH_JSON, true, true);
        assertActiveMessageFound(GUID_NO_JSON, true, false);
    }

    @Test
    public void findAllBySituationIdV0_2_4() throws IOException {
        // One active with json
        initDataFromFile("TrafficIncidentImsMessageV0_2_4.xml");
        // One active without json
        initDataFromFile("TrafficIncidentImsMessageWithoutJson.xml");

        // Both guid should be found
        assertFoundBySituationId(GUID_WITH_JSON, true, true);
        // Only datex2 is found
        assertActiveMessageFound(GUID_NO_JSON, true, false);
    }

    @Test
    public void findAllBySituationIdV0_2_5() throws IOException {
        // One active with json
        initDataFromFile("TrafficIncidentImsMessageV0_2_5.xml");
        // One active without json
        initDataFromFile("TrafficIncidentImsMessageWithoutJson.xml");

        // Both guid should be found
        assertFoundBySituationId(GUID_WITH_JSON, true, true);
        // Only datex2 is found
        assertActiveMessageFound(GUID_NO_JSON, true, false);
    }

    @Test
    public void findActiveJsonWithoutGeometry() throws IOException {
        // One active with json
        initDataFromFile("TrafficIncidentImsMessageWithNullGeometryV0_2_5.xml");
        assertActiveMessageFound(GUID_WITH_JSON, true, true);
    }

    @Test
    public void findActiveJsonWithoutPropertiesIsNotReturned() throws IOException {
        // One active with json
        initDataFromFile("TrafficIncidentImsMessageWithNullProperties.xml");
        assertActiveMessageFound(GUID_WITH_JSON, true, false);
    }

    enum Version {
        V0_2_4,
        V0_2_5
    }

    private static final String FEATURE_1 = "Nopeusrajoitus";
    private static final String FEATURE_2 = "Huono ajokeli";

    private void activeIncidentsDatex2AndJsonEquals(final Version version) {
        final D2LogicalModel d2 = v2Datex2DataService.findActive(0, Datex2MessageType.TRAFFIC_INCIDENT);
        final List<Situation> activeSituations = ((SituationPublication) d2.getPayloadPublication()).getSituations();
        final TrafficAnnouncementFeatureCollection activeJsons = v3Datex2DataService.findActiveJson(0, Datex2MessageType.TRAFFIC_INCIDENT);

        AssertHelper.assertCollectionSize(1, activeSituations);
        AssertHelper.assertCollectionSize(1, activeJsons.getFeatures());
        final Situation situation = activeSituations.get(0);
        final TrafficAnnouncementFeature situationJson = activeJsons.getFeatures().get(0);

        final TrafficAnnouncementProperties jsonProperties = situationJson.getProperties();
        assertEquals(GUID_WITH_JSON, situation.getId());
        assertEquals(GUID_WITH_JSON, jsonProperties.situationId);

        final Instant start = ZonedDateTime.parse("2019-12-13T14:43:18.388+02:00").toInstant();

        final Instant situationStart = situation.getSituationRecords().get(0).getValidity().getValidityTimeSpecification().getOverallStartTime();
        final ZonedDateTime situationJsonStart = jsonProperties.releaseTime;
        assertEquals(start, situationStart);
        assertEquals(start, situationJsonStart.toInstant());

        final String commentXml = situation.getSituationRecords().get(0).getGeneralPublicComments().get(0).getComment().getValues().getValues().stream()
            .filter(c -> c.getLang().equals("fi")).findFirst().orElseThrow().getValue();
        final TrafficAnnouncement announcement = jsonProperties.announcements.get(0);
        final String descJson = announcement.location.description;
        final String titleJson = announcement.title;
        //{"name": "Nopeusrajoitus", "quantity": 80.0, "unit": "km/h"},
        //{"name": "Huono ajokeli"}
        final List<Feature> features = announcement.features;
        AssertHelper.assertCollectionSize(2, features);

        final Optional<Feature> feature1;
        final Optional<Feature> feature2;
        if (version == Version.V0_2_5) {
            feature1 = features.stream().filter(f -> f.name.equals(FEATURE_1)).findFirst();
            feature2 = features.stream().filter(f -> f.name.equals(FEATURE_2)).findFirst();
            assertTrue(FEATURE_1 + " not found", feature1.isPresent());
            assertTrue(FEATURE_2 + " not found", feature2.isPresent());
            assertEquals(80.0, feature1.get().quantity, 0.01);
            assertEquals("km/h",feature1.get().unit);
        } else { // V0_2_0_4 has only name
            feature1 = features.stream().filter(f -> f.name.equals(FEATURE_1)).findFirst();
            feature2 = features.stream().filter(f -> f.name.equals(FEATURE_2)).findFirst();
            assertTrue(FEATURE_1 + " not found", feature1.isPresent());
            assertTrue(FEATURE_2 + " not found", feature2.isPresent());
            assertNull(feature1.get().quantity);
            assertNull(feature1.get().unit);
        }
        assertNull(feature2.get().quantity);
        assertNull(feature2.get().unit);
        assertTrue(commentXml.contains(titleJson.trim()));
        assertTrue(commentXml.contains(descJson.trim()));
    }
    
    private void assertActiveMessageFound(final String situationId, boolean foundInDatex2, boolean foundInJson) {
        final D2LogicalModel withOrWithoutJson = v2Datex2DataService.findActive(0, Datex2MessageType.TRAFFIC_INCIDENT);
        final SituationPublication situationPublication = ((SituationPublication) withOrWithoutJson.getPayloadPublication());
        final TrafficAnnouncementFeatureCollection withJson = v3Datex2DataService.findActiveJson(0, Datex2MessageType.TRAFFIC_INCIDENT);

        assertEquals(
            foundInDatex2,
            situationPublication.getSituations().stream().anyMatch(s -> s.getId().equals(situationId)));
        assertEquals(
            foundInJson,
            withJson.getFeatures().stream().anyMatch(f -> f.getProperties().situationId.equals(situationId)));
    }

    private void assertFoundBySituationId(final String situationId, boolean foundInDatex2, boolean foundInJson) {
        try {
            final D2LogicalModel withOrWithoutJson = v2Datex2DataService.findAllBySituationId(GUID_WITH_JSON, Datex2MessageType.TRAFFIC_INCIDENT);
            final SituationPublication situationPublication = ((SituationPublication) withOrWithoutJson.getPayloadPublication());
            assertEquals(
                foundInDatex2,
                situationPublication.getSituations().stream().anyMatch(s -> s.getId().equals(situationId)));
        } catch (Exception e) { // not found
            Assert.assertFalse(foundInDatex2);
        }
        try {
            final TrafficAnnouncementFeatureCollection withJson =
                v3Datex2DataService.findAllBySituationIdJson(situationId, Datex2MessageType.TRAFFIC_INCIDENT);
            assertEquals(
                foundInJson,
                withJson.getFeatures().stream().anyMatch(f -> f.getProperties().situationId.equals(situationId)));
        } catch (Exception e) { // not found
            Assert.assertFalse(foundInJson);
        }
    }

    private void initDataFromFile(final String file) throws IOException {
        final ArrayList<String> xmlImsMessages = readResourceContents("classpath:tloik/ims/" + file);
        final ImsMessage ims = (ImsMessage) jaxb2Marshaller.unmarshal(new StringSource(xmlImsMessages.get(0)));
        v2Datex2UpdateService.updateTrafficIncidentImsMessages(Collections.singletonList(ims));
    }
}
