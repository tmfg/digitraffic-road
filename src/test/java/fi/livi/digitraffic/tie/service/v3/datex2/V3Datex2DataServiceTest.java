package fi.livi.digitraffic.tie.service.v3.datex2;

import static fi.livi.digitraffic.tie.model.v1.datex2.Datex2DetailedMessageType.TRAFFIC_ANNOUNCEMENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.Situation;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.helper.AssertHelper;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2DetailedMessageType;
import fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.Area;
import fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.Feature;
import fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncement;
import fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeatureCollection;
import fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementProperties;
import fi.livi.digitraffic.tie.service.AbstractDatex2DateServiceTest;

@Import({ V3Datex2DataService.class})
public class V3Datex2DataServiceTest extends AbstractDatex2DateServiceTest {
    private static final Logger log = getLogger(V3Datex2DataServiceTest.class);

    @Autowired
    private V3Datex2DataService v3Datex2DataService;

    @Test
    public void activeIncidentsDatex2AndJsonEqualsForEveryVersionOfImsAndJson() throws IOException {
        // One active incident
        for (final ImsXmlVersion imsXmlVersion : ImsXmlVersion.values()) {
            for (final ImsJsonVersion imsJsonVersion : ImsJsonVersion.values()) {
                cleanDb();
                initDataFromFile(imsXmlVersion, imsJsonVersion);
                log.warn("Run activeIncidentsDatex2AndJsonEquals with imsXmlVersion={} and imsJsonVersion={}", imsXmlVersion, imsJsonVersion);
                activeIncidentsDatex2AndJsonEquals(imsJsonVersion);
            }
        }
    }

    @Test
    public void findBySituationIdWorksForEveryVersionOfImsAndJson() throws IOException {
        // One active incident
        for (final ImsXmlVersion imsXmlVersion : ImsXmlVersion.values()) {
            for (final ImsJsonVersion imsJsonVersion : ImsJsonVersion.values()) {
                cleanDb();
                initDataFromFile(imsXmlVersion, imsJsonVersion);
                log.info("Run checkFindBySituationId with imsXmlVersion={} and imsJsonVersion={}", imsXmlVersion, imsJsonVersion);
                checkFindBySituationId();
            }
        }
    }

    @Test
    public void findActiveV0_2_4() throws IOException {
        // One active with json
        initDataFromFile(ImsXmlVersion.V1_2_1, ImsJsonVersion.V0_2_4);
        // One active without json
        initDataFromFile("TrafficIncidentImsMessageWithoutJson.xml");

        assertActiveMessageFound(GUID_WITH_JSON, true, true);
        assertActiveMessageFound(GUID_NO_JSON, true, false);
    }

    @Test
    public void findActiveV0_2_6() throws IOException {
        // One active with json
        initDataFromFile(ImsXmlVersion.V1_2_1, ImsJsonVersion.V0_2_6);
        // One active without json
        initDataFromFile("TrafficIncidentImsMessageWithoutJson.xml");

        assertActiveMessageFound(GUID_WITH_JSON, true, true);
        assertActiveMessageFound(GUID_NO_JSON, true, false);
    }

    @Test
    public void findAllBySituationIdV0_2_4() throws IOException {
        // One active with json
        initDataFromFile(ImsXmlVersion.V1_2_1, ImsJsonVersion.V0_2_4);
        // One active without json
        initDataFromFile("TrafficIncidentImsMessageWithoutJson.xml");

        // Both guid should be found
        assertFoundBySituationId(GUID_WITH_JSON, true, true);
        // Only datex2 is found
        assertActiveMessageFound(GUID_NO_JSON, true, false);
    }

    @Test
    public void findAllBySituationIdV0_2_6() throws IOException {
        // One active with json
        initDataFromFile(ImsXmlVersion.V1_2_1, ImsJsonVersion.V0_2_6);
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

    private void checkFindBySituationId() {
        final D2LogicalModel d2 = v3Datex2DataService.findAllBySituationId(GUID_WITH_JSON, TRAFFIC_ANNOUNCEMENT);
        final TrafficAnnouncementFeatureCollection jsons =
            v3Datex2DataService.findAllBySituationIdJson(GUID_WITH_JSON, TRAFFIC_ANNOUNCEMENT);

        final List<Situation> situations = ((SituationPublication) d2.getPayloadPublication()).getSituations();

        AssertHelper.assertCollectionSize(1, situations);
        AssertHelper.assertCollectionSize(1, jsons.getFeatures());
        final Situation situation = situations.get(0);
        final TrafficAnnouncementFeature situationJson = jsons.getFeatures().get(0);

        assertEquals(GUID_WITH_JSON, situation.getId());
        assertEquals(GUID_WITH_JSON, situationJson.getProperties().situationId);
    }

    private void activeIncidentsDatex2AndJsonEquals(final ImsJsonVersion imsJsonVersion) {
        final D2LogicalModel d2 = v3Datex2DataService.findActive(0, Datex2DetailedMessageType.allValues());
        final List<Situation> activeSituations = ((SituationPublication) d2.getPayloadPublication()).getSituations();
        final TrafficAnnouncementFeatureCollection activeJsons = v3Datex2DataService.findActiveJson(0, Datex2DetailedMessageType.TRAFFIC_ANNOUNCEMENT);

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

        final Optional<Feature> feature1 = features.stream().filter(f -> f.name.equals(FEATURE_1)).findFirst();
        final Optional<Feature> feature2 = features.stream().filter(f -> f.name.equals(FEATURE_2)).findFirst();
        if (imsJsonVersion == ImsJsonVersion.V0_2_4){ // V0_2_0_4 has only name
            assertTrue(FEATURE_1 + " not found", feature1.isPresent());
            assertTrue(FEATURE_2 + " not found", feature2.isPresent());
            assertNull(feature1.get().quantity);
            assertNull(feature1.get().unit);
        // V0_2_6 and V0_2_8 has also quantity and unit
        } else if (imsJsonVersion == ImsJsonVersion.V0_2_6 || imsJsonVersion == ImsJsonVersion.V0_2_8 || imsJsonVersion == ImsJsonVersion.V0_2_9) {
            assertTrue(FEATURE_1 + " not found", feature1.isPresent());
            assertTrue(FEATURE_2 + " not found", feature2.isPresent());
            assertEquals(80.0, feature1.get().quantity, 0.01);
            assertEquals("km/h",feature1.get().unit);

            assertTrue(announcement.roadWorkPhases.get(0).locationDetails.areaLocation.areas.stream().map(a -> a.type).collect(Collectors.toList())
                       .containsAll(Arrays.asList(Area.Type.WEATHER_REGION, Area.Type.REGIONAL_STATE_ADMINISTRATIVE_AGENCY)));
        } else {
            throw new IllegalArgumentException("imsJsonVersion " + imsJsonVersion + " not tested");
        }
        if (imsJsonVersion == ImsJsonVersion.V0_2_9) {
            assertEquals(TrafficAnnouncementProperties.SituationType.TRAFFIC_ANNOUNCEMENT, jsonProperties.situationType);
            assertEquals(TrafficAnnouncementProperties.TrafficAnnouncementType.ACCIDENT_REPORT, jsonProperties.trafficAnnouncementType);
        }
        assertNull(feature2.get().quantity);
        assertNull(feature2.get().unit);

        assertTrue(commentXml.contains(titleJson.trim()));
        assertTrue(commentXml.contains(descJson.trim()));
    }

    private void assertActiveMessageFound(final String situationId, boolean foundInDatex2, boolean foundInJson) {
        final D2LogicalModel withOrWithoutJson = v3Datex2DataService.findActive(0, Datex2DetailedMessageType.allValues());
        final SituationPublication situationPublication = ((SituationPublication) withOrWithoutJson.getPayloadPublication());
        final TrafficAnnouncementFeatureCollection withJson = v3Datex2DataService.findActiveJson(0, Datex2DetailedMessageType.allValues());

        assertEquals(
            foundInDatex2,
            situationPublication.getSituations().stream().anyMatch(s -> s.getId().equals(situationId)));
        assertEquals(
            foundInJson,
            withJson.getFeatures().stream().anyMatch(f -> f.getProperties().situationId.equals(situationId)));
    }

    private void assertFoundBySituationId(final String situationId, boolean foundInDatex2, boolean foundInJson) {
        try {
            final D2LogicalModel withOrWithoutJson = v3Datex2DataService.findAllBySituationId(GUID_WITH_JSON, Datex2DetailedMessageType.allValues());
            final SituationPublication situationPublication = ((SituationPublication) withOrWithoutJson.getPayloadPublication());
            assertEquals(
                foundInDatex2,
                situationPublication.getSituations().stream().anyMatch(s -> s.getId().equals(situationId)));
        } catch (Exception e) { // not found
            Assert.assertFalse(foundInDatex2);
        }
        try {
            final TrafficAnnouncementFeatureCollection withJson =
                v3Datex2DataService.findAllBySituationIdJson(situationId, Datex2DetailedMessageType.allValues());
            assertEquals(
                foundInJson,
                withJson.getFeatures().stream().anyMatch(f -> f.getProperties().situationId.equals(situationId)));
        } catch (Exception e) { // not found
            Assert.assertFalse(foundInJson);
        }
    }
}
