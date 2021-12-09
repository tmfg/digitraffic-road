package fi.livi.digitraffic.tie.service.v2.datex2;

import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.GUID_WITH_JSON;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.getSituationIdForSituationType;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.getVersionTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.Situation;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.TrafficAnnouncement;
import fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.TrafficAnnouncementFeatureCollection;
import fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.TrafficAnnouncementProperties;
import fi.livi.digitraffic.tie.helper.AssertHelper;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationType;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.ImsJsonVersion;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.ImsXmlVersion;

public class V2Datex2DataServiceTest extends AbstractRestWebTest {
    private static final Logger log = getLogger(V2Datex2DataServiceTest.class);

    @Autowired
    private V2Datex2DataService v2Datex2DataService;

    @Autowired
    private TrafficMessageTestHelper trafficMessageTestHelper;

    @Test
    public void activeIncidentsDatex2AndJsonEqualsForEveryVersionOfImsAndJson()throws IOException {
        // One active incident per version
        final ZonedDateTime start = DateHelper.getZonedDateTimeNowWithoutMillisAtUtc().minusHours(1);
        final ZonedDateTime end = start.plusHours(2);
        for (final ImsXmlVersion imsXmlVersion : ImsXmlVersion.values()) {
            for (final ImsJsonVersion imsJsonVersion : ImsJsonVersion.values()) {
                trafficMessageTestHelper.cleanDb();
                trafficMessageTestHelper.initDataFromStaticImsResourceContent(imsXmlVersion, SituationType.TRAFFIC_ANNOUNCEMENT.name(), imsJsonVersion, start, end);
                log.info("Run activeIncidentsDatex2AndJsonEquals with imsXmlVersion={} and imsJsonVersion={}", imsXmlVersion, imsJsonVersion);
                activeIncidentsDatex2AndJsonEquals(SituationType.TRAFFIC_ANNOUNCEMENT, start.toInstant(), imsJsonVersion);
            }
        }
    }

    @Test
    public void findActiveTrafficMessagesDatex2AndJsonEqualsForEveryVersionOfImsAndJsonWhenMultipleVersionsIn() throws IOException {
        trafficMessageTestHelper.cleanDb();
        for (final ImsXmlVersion imsXmlVersion : ImsXmlVersion.values()) {
            for (final SituationType situationType : SituationType.values()) {
                final ZonedDateTime start = DateHelper.getZonedDateTimeNowWithoutMillisAtUtc().minusHours(1);
                final ZonedDateTime end = start.plusHours(2);
                for (final ImsJsonVersion imsJsonVersion : ImsJsonVersion.values()) {
                    trafficMessageTestHelper.initDataFromStaticImsResourceContent(imsXmlVersion, situationType.name(), imsJsonVersion, start, end);
                    log.info("activeIncidentsDatex2AndJsonEquals with imsXmlVersion={}, imsJsonVersion={} and situationType={}",
                        imsXmlVersion, imsJsonVersion, situationType);
                }
                activeIncidentsDatex2AndJsonEquals(situationType, start.toInstant(), ImsJsonVersion.getLatestVersion());
            }
        }
    }

    @Test
    public void findBySituationId() throws IOException {
        final SituationType situationType = SituationType.TRAFFIC_ANNOUNCEMENT;
        final String situationId = getSituationIdForSituationType(situationType.name());

        final ZonedDateTime start = DateHelper.getZonedDateTimeNowWithoutMillisAtUtc().minusHours(1);
        final ZonedDateTime end = start.plusHours(2);

        for (final ImsXmlVersion imsXmlVersion : ImsXmlVersion.values()) {
            for (final ImsJsonVersion imsJsonVersion : ImsJsonVersion.values()) {
                trafficMessageTestHelper.cleanDb();
                trafficMessageTestHelper.initDataFromStaticImsResourceContent(imsXmlVersion, situationType.name(), imsJsonVersion, start, end);
                trafficMessageTestHelper.initDataFromStaticImsResourceContent(imsXmlVersion, SituationType.ROAD_WORK.name(), imsJsonVersion, start, end);
                log.info("Run findBySituationId with imsXmlVersion={} and imsJsonVersion={}", imsXmlVersion, imsJsonVersion);
                assertFindBySituationId(situationId, situationType);
            }
        }
    }

    @Test
    public void findActiveJsonWithoutGeometry() throws IOException {
        // One active with json
        trafficMessageTestHelper.initDataFromFile("TrafficIncidentImsMessageWithNullGeometryV0_2_6.xml");
        assertActiveMessageFound(GUID_WITH_JSON, true, true);
    }

    @Test
    public void findActiveJsonWithoutPropertiesIsNotReturned() throws IOException {
        // One active with json
        trafficMessageTestHelper.initDataFromFile("TrafficIncidentImsMessageWithNullProperties.xml");
        assertActiveMessageFound(GUID_WITH_JSON, false, false);
    }

    private void assertFindBySituationId(final String situationId, final SituationType situationType) {
        final D2LogicalModel d2 = v2Datex2DataService.findAllBySituationId(situationId, situationType.getDatex2MessageType());
        final TrafficAnnouncementFeatureCollection jsons = v2Datex2DataService.findAllBySituationIdJson(situationId, situationType.getDatex2MessageType());

        final List<Situation> situations = ((SituationPublication) d2.getPayloadPublication()).getSituations();

        AssertHelper.assertCollectionSize(1, situations);
        AssertHelper.assertCollectionSize(1, jsons.getFeatures());
        final Situation situation = situations.get(0);
        final TrafficAnnouncementFeature situationJson = jsons.getFeatures().get(0);

        assertEquals(situationId, situation.getId());
        assertEquals(situationId, situationJson.getProperties().situationId);
    }

    private void activeIncidentsDatex2AndJsonEquals(final SituationType situationType, final Instant start,
                                                    final ImsJsonVersion jsonVersion) {

        final D2LogicalModel d2 = v2Datex2DataService.findActive(0, situationType.getDatex2MessageType());
        final List<Situation> activeSituations = ((SituationPublication) d2.getPayloadPublication()).getSituations();
        final TrafficAnnouncementFeatureCollection activeJsons = v2Datex2DataService.findActiveJson(0, situationType.getDatex2MessageType());

        if (situationType == SituationType.EXEMPTED_TRANSPORT) {
            AssertHelper.assertCollectionSize(2, activeSituations);
            AssertHelper.assertCollectionSize(2, activeJsons.getFeatures());
        } else {
            AssertHelper.assertCollectionSize(1, activeSituations);
            AssertHelper.assertCollectionSize(1, activeJsons.getFeatures());
        }
        final String situationId = getSituationIdForSituationType(situationType.name());
        final Situation situation = activeSituations.stream().filter(s -> s.getId().equals(situationId)).findFirst().orElseThrow();
        final TrafficAnnouncementFeature situationJson = activeJsons.getFeatures().stream().filter(s -> s.getProperties().situationId.equals(situationId)).findFirst().orElseThrow();


        final TrafficAnnouncementProperties jsonProperties = situationJson.getProperties();
        assertEquals(situationId, situation.getId());
        assertEquals(situationId, jsonProperties.situationId);

        final Instant versionTime = getVersionTime(start.atZone(ZoneId.systemDefault()), jsonVersion.intVersion).toInstant();

        assertEquals(versionTime, situation.getSituationRecords().get(0).getSituationRecordVersionTime());
        assertEquals(versionTime, jsonProperties.releaseTime.toInstant());

        assertEquals(start, situation.getSituationRecords().get(0).getValidity().getValidityTimeSpecification().getOverallStartTime());
        assertEquals(start, jsonProperties.announcements.get(0).timeAndDuration.startTime.toInstant());

        final String commentXml = situation.getSituationRecords().get(0).getGeneralPublicComments().get(0).getComment().getValues().getValues().stream()
            .filter(c -> c.getLang().equals("fi")).findFirst().orElseThrow().getValue();
        final TrafficAnnouncement announcement = jsonProperties.announcements.get(0);
        final String descJson = announcement.location.description;
        final String titleJson = announcement.title;

        if (situationType == SituationType.WEIGHT_RESTRICTION) {
            AssertHelper.assertCollectionSize(1, announcement.features);
        } else {
            AssertHelper.assertCollectionSize(2, announcement.features);
        }

        assertTrue(commentXml.contains(titleJson.trim()));
        assertTrue(commentXml.contains(descJson.trim()));
    }

    private void assertActiveMessageFound(final String situationId, boolean foundInDatex2, boolean foundInJson) {
        final D2LogicalModel withOrWithoutJson = v2Datex2DataService.findActive(0, Datex2MessageType.TRAFFIC_INCIDENT);
        final SituationPublication situationPublication = ((SituationPublication) withOrWithoutJson.getPayloadPublication());
        final TrafficAnnouncementFeatureCollection withJson = v2Datex2DataService.findActiveJson(0, Datex2MessageType.TRAFFIC_INCIDENT);

        if (foundInDatex2 || situationPublication != null) {
            assertEquals(
                foundInDatex2,
                situationPublication.getSituations().stream().anyMatch(s -> s.getId().equals(situationId)));
        }
        if (foundInJson || withJson.getFeatures().size() > 0) {
            assertEquals(
                foundInJson,
                withJson.getFeatures().stream().anyMatch(f -> f.getProperties().situationId.equals(situationId)));
        }
    }
}
