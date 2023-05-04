package fi.livi.digitraffic.tie.service.trafficmessage.v1;

import static fi.livi.digitraffic.tie.TestUtils.getRandom;
import static fi.livi.digitraffic.tie.dto.trafficmessage.v1.SituationType.TRAFFIC_ANNOUNCEMENT;
import static fi.livi.digitraffic.tie.dto.trafficmessage.v1.SituationType.values;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.GUID_WITH_ACTIVE_ANDPASSIVE_RECORD;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.GUID_WITH_JSON;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.ImsXmlVersion;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.getSituationIdForSituationType;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.getVersionTime;
import static fi.livi.digitraffic.tie.service.trafficmessage.Datex2Helper.getSituationPublication;
import static fi.livi.digitraffic.tie.service.v2.datex2.RegionGeometryTestHelper.createNewRegionGeometry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import org.apache.commons.compress.utils.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.AbstractWebServiceTestWithRegionGeometryServiceAndGitMock;
import fi.livi.digitraffic.tie.controller.ResponseEntityWithLastModifiedHeader;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.Situation;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.SituationType;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TimeAndDuration;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncement;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeatureCollection;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementProperties;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementType;
import fi.livi.digitraffic.tie.helper.AssertHelper;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.ImsJsonVersion;

public class TrafficMessageDataServiceV1Test extends AbstractWebServiceTestWithRegionGeometryServiceAndGitMock {
    private static final Logger log = getLogger(TrafficMessageDataServiceV1Test.class);

    @Autowired
    private TrafficMessageDataServiceV1 trafficMessageDataServiceV1;

    @Autowired
    private TrafficMessageTestHelper trafficMessageTestHelper;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    public void init() {
        trafficMessageTestHelper.cleanDb();
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(0));
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(3));
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(7));
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(14));
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(408));
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(5898));
    }

    @Test
    public void findActiveTrafficMessagesDatex2AndJsonEqualsForEveryVersionOfImsAndJson() throws IOException {

        for (final ImsXmlVersion imsXmlVersion : ImsXmlVersion.values()) {
            for (final ImsJsonVersion imsJsonVersion : ImsJsonVersion.values()) {
                for (final SituationType situationType : values()) {
                    trafficMessageTestHelper.cleanDb();
                    final ZonedDateTime start = DateHelper.getZonedDateTimeNowWithoutMillisAtUtc().minusHours(1);
                    final ZonedDateTime end = start.plusHours(2);
                    trafficMessageTestHelper.initDataFromStaticImsResourceContent(imsXmlVersion, situationType.name(), imsJsonVersion, start, end);
                    log.info("activeIncidentsDatex2AndJsonEquals with imsXmlVersion={}, imsJsonVersion={} and situationType={}",
                             imsXmlVersion, imsJsonVersion, situationType);
                    activeIncidentsDatex2AndJsonEquals(situationType, imsJsonVersion, getSituationIdForSituationType(situationType.name()), start, end);
                }
            }
        }
    }

    @Test
    public void findTrafficMessagesBySituationIdWorksForEveryVersionOfImsAndJson() throws IOException {
        // One active incident per version
        for (final ImsXmlVersion imsXmlVersion : ImsXmlVersion.values()) {
            for (final ImsJsonVersion imsJsonVersion : ImsJsonVersion.values()) {
                for (final SituationType situationType : values()) {
                    trafficMessageTestHelper.cleanDb();
                    trafficMessageTestHelper.initDataFromStaticImsResourceContent(imsXmlVersion, situationType.name(), imsJsonVersion);
                    log.info("checkFindBySituationId with imsXmlVersion={}, imsJsonVersion={} and situationType={}",
                        imsXmlVersion, imsJsonVersion, situationType);
                    checkFindBySituationId(getSituationIdForSituationType(situationType.name()));
                }
            }
        }
    }

    @Test
    public void findActiveTrafficMessagesDatex2AndJsonEqualsForEveryVersionOfImsAndJsonWhenMultipleVersionsIn() throws IOException {
        trafficMessageTestHelper.cleanDb();
        for (final ImsXmlVersion imsXmlVersion : ImsXmlVersion.values()) {
            for (final SituationType situationType : values()) {
                final ZonedDateTime start = DateHelper.getZonedDateTimeNowWithoutMillisAtUtc().minusHours(1);
                final ZonedDateTime end = start.plusHours(2);
                for (final ImsJsonVersion imsJsonVersion : ImsJsonVersion.values()) {
                    trafficMessageTestHelper.initDataFromStaticImsResourceContent(imsXmlVersion, situationType.name(), imsJsonVersion, start, end);
                    log.info("activeIncidentsDatex2AndJsonEquals with imsXmlVersion={}, imsJsonVersion={} and situationType={}",
                             imsXmlVersion, imsJsonVersion, situationType);
                }
                activeIncidentsDatex2AndJsonEquals(situationType, ImsJsonVersion.getLatestVersion(),
                                                   getSituationIdForSituationType(situationType.name()), start, end);
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
        // One not active to get last modified field from db
        trafficMessageTestHelper.initDataFromStaticImsResourceContent(
            ImsXmlVersion.getLatestVersion(), TRAFFIC_ANNOUNCEMENT.name(), ImsJsonVersion.getLatestVersion(),
            ZonedDateTime.now().minusHours(2), ZonedDateTime.now().minusHours(1));
        // One active with json
        trafficMessageTestHelper.initDataFromFile("TrafficIncidentImsMessageWithNullProperties.xml");
        // Not found, as both must exist
        assertActiveMessageFound(GUID_WITH_JSON, false, false);
    }

    @Test
    public void findActiveTrafficAnnouncementCanceledIsNotReturned() throws IOException {
        trafficMessageTestHelper.initDataFromStaticImsResourceContent(
            ImsXmlVersion.V1_2_1, TRAFFIC_ANNOUNCEMENT.name(), ImsJsonVersion.getLatestVersion(),
            ZonedDateTime.now().minusDays(1), ZonedDateTime.now().plusDays(1), true);
        // Not found, as both must exist
        assertActiveMessageFound(GUID_WITH_JSON, false, false);
    }

    @Test
    public void findTrafficAnnouncementWithActiveAndDeactiveSituationRecordIsReturned() throws IOException {
        final ZonedDateTime start = DateHelper.getZonedDateTimeNowAtUtc().minusHours(1);
        final ZonedDateTime endTime = start.plusHours(2);
        // One active with json
        trafficMessageTestHelper.initImsDataFromFile("TrafficIncidentImsMessageV1_2_1WithActiveAndPassiveSituationRecord.xml",
                                                     ImsJsonVersion.getLatestVersion(), start, endTime, false);
        assertActiveMessageFound(GUID_WITH_ACTIVE_ANDPASSIVE_RECORD, true, true);
    }

    @Test
    public void invalidJsonMultiLineStringGeometryIsFixed() throws IOException {
        // Message guid GUID50390596 that has MultiLineString with valid LineString and invalid LineString with two equal points
        trafficMessageTestHelper.initDataFromFile("TrafficIncidentImsMessageWithInvalidMultiLineStringGeometry.xml");
        final TrafficAnnouncementFeature
            feature = trafficMessageDataServiceV1.findBySituationIdJson("GUID50390596", false, true).getFeatures().get(0);
        // Result geometry should only have one linestring and the invalid LineString should have be removed
        assertEquals("LineString", feature.getGeometry().getType().toString());
        assertTrue(feature.getGeometry().getCoordinates().size() > 2);
    }

    @Test
    public void invalidJsonMultiLineStringGeometryIsFixed2() throws IOException {
        // MultiLineString with single lineString and equal points
        trafficMessageTestHelper.initDataFromFile("TrafficIncidentImsMessageWithInvalidMultiLineStringGeometry2.xml");
        final TrafficAnnouncementFeature
            feature = trafficMessageDataServiceV1.findBySituationIdJson("GUID50390964", false, true).getFeatures().get(0);
        // Result geometry should only have one linestring and the invalid LineString should have be removed
        assertEquals("Point", feature.getGeometry().getType().toString());
        assertTrue(feature.getGeometry().getCoordinates().size() > 1);
    }

    @Test
    public void invalidJsonMultiPolygonGeometryIsFixed() throws IOException {
        trafficMessageTestHelper.initDataFromFile("TrafficIncidentImsMessageWithInvalidMultiPolygonGeometry.xml");
        final TrafficAnnouncementFeature feature = trafficMessageDataServiceV1.findBySituationIdJson("GUID50379978", false, true).getFeatures().get(0);
        // Result geometry should only have one linestring and the invalid LineString should have be removed
        assertEquals("Polygon", feature.getGeometry().getType().toString());
        assertEquals(2, feature.getGeometry().getCoordinates().size());
    }

    @Test
    public void findBySituationIdLatest() {
        trafficMessageTestHelper.cleanDb();
        final ImsXmlVersion imsXmlVersion = ImsXmlVersion.getLatestVersion();
        final int count = getRandom(5, 15);
        final ZonedDateTime initialTime = DateHelper.getZonedDateTimeNowWithoutMillisAtUtc().minusHours(count);

        // 1. create multiple versions for one situation
        final AtomicReference<ZonedDateTime> latestStart = new AtomicReference<>();
        IntStream.range(0, count).forEach(i -> {
            latestStart.set(initialTime.plusHours(i));
            final ZonedDateTime end = latestStart.get().plusHours(1);
            try {
                trafficMessageTestHelper.initDataFromStaticImsResourceContent(imsXmlVersion, TRAFFIC_ANNOUNCEMENT.name(), ImsJsonVersion.getLatestVersion(),
                                                                              latestStart.get(), end);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        final String situationId = getSituationIdForSituationType(TRAFFIC_ANNOUNCEMENT.name());

        // Make sure all versions are saved
        assertEquals(count, trafficMessageDataServiceV1.findBySituationIdJson(situationId, false, false).getFeatures().size());
        assertEquals(count, getSituationPublication(
            Objects.requireNonNull(trafficMessageDataServiceV1.findBySituationId(situationId, false).getLeft())).getSituations().size());

        // Get latest versions
        final TrafficAnnouncementFeatureCollection latestJson = trafficMessageDataServiceV1.findBySituationIdJson(situationId, false, true);
        final D2LogicalModel latestDatex = trafficMessageDataServiceV1.findBySituationId(situationId, true).getLeft();

        // Make sure only the latest version is returned
        assertEquals(latestStart.get(), latestJson.getFeatures().get(0).getProperties().announcements.get(0).timeAndDuration.startTime);
        assertEquals(getVersionTime(latestStart.get(), ImsJsonVersion.getLatestVersion()).toEpochSecond(),
                     getSituationPublication(latestDatex).getSituations().get(0).getSituationRecords().get(0)
                         .getSituationRecordVersionTime().getEpochSecond());
        assertEquals(latestStart.get().toEpochSecond(),
                     getSituationPublication(latestDatex).getSituations().get(0).getSituationRecords().get(0)
                         .getValidity().getValidityTimeSpecification().getOverallStartTime().getEpochSecond());
        assertEquals(1, latestJson.getFeatures().size());
        assertEquals(1, getSituationPublication(latestDatex).getSituations().size());
    }

    private void checkFindBySituationId(final String situationId) {
        final D2LogicalModel d2 = trafficMessageDataServiceV1.findBySituationId(situationId, false).getLeft();
        final TrafficAnnouncementFeatureCollection jsons =
            trafficMessageDataServiceV1.findBySituationIdJson(situationId, true, false);

        final List<Situation> situations = ((SituationPublication) d2.getPayloadPublication()).getSituations();

        AssertHelper.assertCollectionSize(1, situations);
        AssertHelper.assertCollectionSize(1, jsons.getFeatures());
        final Situation situation = situations.get(0);
        final TrafficAnnouncementFeature situationJson = jsons.getFeatures().get(0);

        assertEquals(situationId, situation.getId());
        assertEquals(situationId, situationJson.getProperties().situationId);
    }

    private void activeIncidentsDatex2AndJsonEquals(final SituationType situationType, final ImsJsonVersion imsJsonVersion, final String situationId,
                                                    final ZonedDateTime start, final ZonedDateTime end) {
        final D2LogicalModel d2 = trafficMessageDataServiceV1.findActive(0, situationType).getLeft();
        final List<Situation> activeSituations = ((SituationPublication) d2.getPayloadPublication()).getSituations();
        final TrafficAnnouncementFeatureCollection activeJsons = trafficMessageDataServiceV1.findActiveJson(0, true, situationType);

        AssertHelper.assertCollectionSize(1, activeSituations);
        AssertHelper.assertCollectionSize(1, activeJsons.getFeatures());
        final Situation situation = activeSituations.get(0);
        final TrafficAnnouncementFeature situationJson = activeJsons.getFeatures().get(0);

        final TrafficAnnouncementProperties jsonProperties = situationJson.getProperties();
        assertEquals(situationId, situation.getId());
        assertEquals(situationId, jsonProperties.situationId);

        final Instant situationVersionTime = situation.getSituationRecords().get(0).getSituationRecordVersionTime();
        final Instant situationStart = situation.getSituationRecords().get(0).getValidity().getValidityTimeSpecification().getOverallStartTime();
        final Instant situationEnd = situation.getSituationRecords().get(0).getValidity().getValidityTimeSpecification().getOverallEndTime();
        final TimeAndDuration jsonTimeAndDuration = jsonProperties.announcements.get(0).timeAndDuration;

        assertEquals(getVersionTime(start, imsJsonVersion.intVersion).toInstant(), situationVersionTime);
        assertEquals(getVersionTime(start, imsJsonVersion.intVersion).toInstant(), jsonProperties.releaseTime.toInstant());

        assertEquals(start.toInstant(), situationStart);
        assertEquals(start.toInstant(), jsonTimeAndDuration.startTime.toInstant());

        assertEquals(end.toInstant(), situationEnd);
        assertEquals(end.toInstant(), jsonTimeAndDuration.endTime.toInstant());

        final String commentXml =
            situation.getSituationRecords().get(0).getGeneralPublicComments().get(0).getComment().getValues().getValues().stream()
                .filter(c -> c.getLang().equals("fi")).findFirst().orElseThrow().getValue();

        assertEquals(situationType, jsonProperties.getSituationType());
        if (situationType == TRAFFIC_ANNOUNCEMENT) {
            assertTrue(Sets.newHashSet(TrafficAnnouncementType.values()).contains(jsonProperties.getTrafficAnnouncementType()));
        }

        final TrafficAnnouncement announcement = jsonProperties.announcements.get(0);
        assertTrue(commentXml.contains(announcement.title.trim()));
    }

    private void assertActiveMessageFound(final String situationId, boolean foundInDatex2, boolean foundInJson) {
        final D2LogicalModel withOrWithoutJson = trafficMessageDataServiceV1.findActive(0).getLeft();
        final SituationPublication situationPublication = ((SituationPublication) withOrWithoutJson.getPayloadPublication());
        final TrafficAnnouncementFeatureCollection withJson = trafficMessageDataServiceV1.findActiveJson(0, true);

        if (foundInDatex2) {
            assertEquals(
                foundInDatex2,
                situationPublication.getSituations().stream().anyMatch(s -> s.getId().equals(situationId)));
        } else {
            assertNull(situationPublication);
        }
        if (foundInJson) {
            assertEquals(
                foundInJson,
                withJson.getFeatures().stream().anyMatch(f -> f.getProperties().situationId.equals(situationId)));
        } else {
            AssertHelper.assertCollectionSize(0, withJson.getFeatures());
        }
    }
}
