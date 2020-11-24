package fi.livi.digitraffic.tie.service.v2.datex2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.Situation;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.helper.AssertHelper;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncement;
import fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeatureCollection;
import fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementProperties;
import fi.livi.digitraffic.tie.service.AbstractDatex2DateServiceTest;

public class V2Datex2DataServiceTest extends AbstractDatex2DateServiceTest {
    private static final Logger log = getLogger(V2Datex2DataServiceTest.class);

    @Autowired
    private V2Datex2DataService v2Datex2DataService;

    @Test
    public void activeIncidentsDatex2AndJsonEqualsForEveryVersionOfImsAndJson()throws IOException {
        // One active incident
        for (final ImsXmlVersion imsXmlVersion : ImsXmlVersion.values()) {
            for (final ImsJsonVersion imsJsonVersion : ImsJsonVersion.values()) {
                cleanDb();
                initDataFromFile(imsXmlVersion, imsJsonVersion);
                log.info("Run activeIncidentsDatex2AndJsonEquals with imsXmlVersion={} and imsJsonVersion={}", imsXmlVersion, imsJsonVersion);
                activeIncidentsDatex2AndJsonEquals();
            }
        }
    }

    @Test
    public void findBySituationId() throws IOException {
        // One active
        initDataFromFile(ImsXmlVersion.V1_2_0, ImsJsonVersion.V0_2_4);

        final D2LogicalModel d2 = v2Datex2DataService.findAllBySituationId(GUID_WITH_JSON, Datex2MessageType.TRAFFIC_INCIDENT);
        final TrafficAnnouncementFeatureCollection jsons = v2Datex2DataService.findAllBySituationIdJson(GUID_WITH_JSON, Datex2MessageType.TRAFFIC_INCIDENT);

        final List<Situation> situations = ((SituationPublication) d2.getPayloadPublication()).getSituations();

        AssertHelper.assertCollectionSize(1, situations);
        AssertHelper.assertCollectionSize(1, jsons.getFeatures());
        final Situation situation = situations.get(0);
        final TrafficAnnouncementFeature situationJson = jsons.getFeatures().get(0);

        assertEquals(GUID_WITH_JSON, situation.getId());
        assertEquals(GUID_WITH_JSON, situationJson.getProperties().situationId);
    }

    @Test
    public void findActive() throws IOException {
        // One active with json
        initDataFromFile(ImsXmlVersion.V1_2_0, ImsJsonVersion.V0_2_4);
        // One active without json
        initDataFromFile("TrafficIncidentImsMessageWithoutJson.xml");

        assertActiveMessageFound(GUID_WITH_JSON, true, true);
        assertActiveMessageFound(GUID_NO_JSON, true, false);
    }

    @Test
    public void findAllBySituationId() throws IOException {
        // One active with json
        initDataFromFile(ImsXmlVersion.V1_2_0, ImsJsonVersion.V0_2_4);
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
        initDataFromFile("TrafficIncidentImsMessageWithNullGeometryV0_2_6.xml");
        assertActiveMessageFound(GUID_WITH_JSON, true, true);
    }

    @Test
    public void findActiveJsonWithoutPropertiesIsNotReturned() throws IOException {
        // One active with json
        initDataFromFile("TrafficIncidentImsMessageWithNullProperties.xml");
        assertActiveMessageFound(GUID_WITH_JSON, true, false);
    }

    private void activeIncidentsDatex2AndJsonEquals() {
        final D2LogicalModel d2 = v2Datex2DataService.findActive(0, Datex2MessageType.TRAFFIC_INCIDENT);
        final List<Situation> activeSituations = ((SituationPublication) d2.getPayloadPublication()).getSituations();
        final TrafficAnnouncementFeatureCollection activeJsons = v2Datex2DataService.findActiveJson(0, Datex2MessageType.TRAFFIC_INCIDENT);

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
        AssertHelper.assertCollectionSize(2, announcement.features);
        AssertHelper.collectionContains(FEATURE_1, announcement.features);
        AssertHelper.collectionContains(FEATURE_2, announcement.features);

        assertTrue(commentXml.contains(titleJson.trim()));
        assertTrue(commentXml.contains(descJson.trim()));
    }

    private void assertActiveMessageFound(final String situationId, boolean foundInDatex2, boolean foundInJson) {
        final D2LogicalModel withOrWithoutJson = v2Datex2DataService.findActive(0, Datex2MessageType.TRAFFIC_INCIDENT);
        final SituationPublication situationPublication = ((SituationPublication) withOrWithoutJson.getPayloadPublication());
        final TrafficAnnouncementFeatureCollection withJson = v2Datex2DataService.findActiveJson(0, Datex2MessageType.TRAFFIC_INCIDENT);

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
            assertFalse(foundInDatex2);
        }
        try {
            final TrafficAnnouncementFeatureCollection withJson =
                v2Datex2DataService.findAllBySituationIdJson(situationId, Datex2MessageType.TRAFFIC_INCIDENT);
            assertEquals(
                foundInJson,
                withJson.getFeatures().stream().anyMatch(f -> f.getProperties().situationId.equals(situationId)));
        } catch (Exception e) { // not found
            assertFalse(foundInJson);
        }
    }
}
