package fi.livi.digitraffic.tie.service.trafficmessage.v1;

import static fi.livi.digitraffic.tie.TestUtils.getRandom;
import static fi.livi.digitraffic.tie.dto.trafficmessage.v1.SituationType.TRAFFIC_ANNOUNCEMENT;
import static fi.livi.digitraffic.tie.dto.trafficmessage.v1.SituationType.values;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.GUID_WITH_ACTIVE_ANDPASSIVE_RECORD;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.GUID_WITH_JSON;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.ImsXmlVersion;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.getSituationIdForSituationType;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.getVersionTime;
import static fi.livi.digitraffic.tie.service.trafficmessage.DatexIIHelper.getSituationPublication;
import static fi.livi.digitraffic.tie.service.trafficmessage.RegionGeometryTestHelper.createNewRegionGeometry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import org.apache.commons.compress.utils.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.common.util.TimeUtil;
import fi.livi.digitraffic.test.util.AssertUtil;
import fi.livi.digitraffic.tie.AbstractWebServiceTestWithRegionGeometryServiceAndGitMock;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.Situation;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.SituationPublication;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.SituationType;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TimeAndDuration;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncement;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeatureCollection;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementProperties;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementType;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.ImsJsonVersion;

@Disabled("TODO: needs rewriting or simply delete?")
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
                    final Instant start = TimeUtil.nowWithoutMillis().minus(1, ChronoUnit.HOURS);
                    final Instant end = start.plus(2, ChronoUnit.HOURS);
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
                final Instant start = TimeUtil.nowWithoutMillis().minus(1, ChronoUnit.HOURS);
                final Instant end = start.plus(2, ChronoUnit.HOURS);
                for (final ImsJsonVersion imsJsonVersion : ImsJsonVersion.values()) {
                    trafficMessageTestHelper.initDataFromStaticImsResourceContent(
                            imsXmlVersion, situationType.name(), imsJsonVersion, start, end);
                    log.info("activeIncidentsDatex2AndJsonEquals with imsXmlVersion={}, imsJsonVersion={} and situationType={}",
                             imsXmlVersion, imsJsonVersion, situationType);
                }
                activeIncidentsDatex2AndJsonEquals(
                        situationType,
                        ImsJsonVersion.getLatestVersion(),
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
            Instant.now().minus(2, ChronoUnit.HOURS), Instant.now().minus(1, ChronoUnit.HOURS));
        // One active with json
        trafficMessageTestHelper.initDataFromFile("TrafficIncidentImsMessageWithNullProperties.xml");
        // Not found, as both must exist
        assertActiveMessageFound(GUID_WITH_JSON, false, false);
    }

    /*
    @Rollback(value = false)
    @Test
    public void findActiveTrafficAnnouncementCanceledIsNotReturned() throws IOException {
        trafficMessageTestHelper.initDataFromStaticImsResourceContent(
            ImsXmlVersion.V1_2_1, TRAFFIC_ANNOUNCEMENT.name(), ImsJsonVersion.getLatestVersion(),
            Instant.now().minus(1, ChronoUnit.DAYS), Instant.now().plus(1, ChronoUnit.DAYS), true);
        // Not found, as both must exist
        assertActiveMessageFound(GUID_WITH_JSON, false, false);
    }*/

    @Test
    public void findTrafficAnnouncementWithActiveAndDeactiveSituationRecordIsReturned() throws IOException {
        final Instant start = TimeUtil.nowWithoutMillis().minus(1, ChronoUnit.HOURS);
        final Instant endTime = start.plus(2, ChronoUnit.HOURS);
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
            feature = trafficMessageDataServiceV1.findBySituationIdJson("GUID50390596", false, true).getFeatures().getFirst();
        // Result geometry should only have one linestring and the invalid LineString should have be removed
        assertFeature(feature, "LineString", 42);
    }

    @Test
    public void invalidJsonMultiLineStringGeometryIsFixed2() throws IOException {
        // MultiLineString with single lineString and equal points
        trafficMessageTestHelper.initDataFromFile("TrafficIncidentImsMessageWithInvalidMultiLineStringGeometry2.xml");
        final TrafficAnnouncementFeature
            feature = trafficMessageDataServiceV1.findBySituationIdJson("GUID50390964", false, true).getFeatures().getFirst();
        // Result geometry should only have one linestring and the invalid LineString should have be removed
        assertFeature(feature, "Point", 2);
    }

    @Test
    public void invalidJsonMultiPolygonGeometryIsFixed() throws IOException {
        trafficMessageTestHelper.initDataFromFile("TrafficIncidentImsMessageWithInvalidMultiPolygonGeometry.xml");
        final TrafficAnnouncementFeature feature = trafficMessageDataServiceV1.findBySituationIdJson("GUID50379978", false, true).getFeatures().getFirst();
        // Result geometry should only have one linestring and the invalid LineString should have be removed
        assertFeature(feature, "Polygon", 2);
    }

    @Test
    public void findBySituationIdLatest() {
        trafficMessageTestHelper.cleanDb();
        final ImsXmlVersion imsXmlVersion = ImsXmlVersion.getLatestVersion();
        final int count = getRandom(5, 15);
        final Instant initialTime = TimeUtil.nowWithoutMillis().minus(count, ChronoUnit.HOURS);

        // 1. create multiple versions for one situation
        final AtomicReference<Instant> latestStart = new AtomicReference<>();
        IntStream.range(0, count).forEach(i -> {
            latestStart.set(initialTime.plus(i, ChronoUnit.HOURS));
            final Instant end = latestStart.get().plus(1, ChronoUnit.HOURS);
            try {
                trafficMessageTestHelper.initDataFromStaticImsResourceContent(imsXmlVersion, TRAFFIC_ANNOUNCEMENT.name(), ImsJsonVersion.getLatestVersion(),
                                                                              latestStart.get(), end);
            } catch (final IOException e) {
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
        assertEquals(latestStart.get(), latestJson.getFeatures().getFirst().getProperties().announcements.getFirst().timeAndDuration.startTime);
        assertEquals(getVersionTime(latestStart.get(), ImsJsonVersion.getLatestVersion()).getEpochSecond(),
                     getSituationPublication(latestDatex).getSituations().getFirst().getSituationRecords().getFirst()
                         .getSituationRecordVersionTime().getEpochSecond());
        assertEquals(latestStart.get().getEpochSecond(),
                     getSituationPublication(latestDatex).getSituations().getFirst().getSituationRecords().getFirst()
                         .getValidity().getValidityTimeSpecification().getOverallStartTime().getEpochSecond());
        assertEquals(1, latestJson.getFeatures().size());
        assertEquals(1, getSituationPublication(latestDatex).getSituations().size());
    }

    private void checkFindBySituationId(final String situationId) {
        final D2LogicalModel d2 = trafficMessageDataServiceV1.findBySituationId(situationId, false).getLeft();
        final TrafficAnnouncementFeatureCollection jsons =
            trafficMessageDataServiceV1.findBySituationIdJson(situationId, true, false);

        final List<Situation> situations = ((SituationPublication) d2.getPayloadPublication()).getSituations();

        AssertUtil.assertCollectionSize(1, situations);
        AssertUtil.assertCollectionSize(1, jsons.getFeatures());
        final Situation situation = situations.getFirst();
        final TrafficAnnouncementFeature situationJson = jsons.getFeatures().getFirst();

        assertEquals(situationId, situation.getId());
        assertEquals(situationId, situationJson.getProperties().situationId);
    }

    private void activeIncidentsDatex2AndJsonEquals(final SituationType situationType, final ImsJsonVersion imsJsonVersion, final String situationId,
                                                    final Instant start, final Instant end) {
        final D2LogicalModel d2 = trafficMessageDataServiceV1.findActive(0, situationType).getLeft();
        final List<Situation> activeSituations = ((SituationPublication) d2.getPayloadPublication()).getSituations();
        final TrafficAnnouncementFeatureCollection activeJsons = trafficMessageDataServiceV1.findActiveJson(0, true, situationType);

        AssertUtil.assertCollectionSize(1, activeSituations);
        AssertUtil.assertCollectionSize(1, activeJsons.getFeatures());
        final Situation situation = activeSituations.getFirst();
        final TrafficAnnouncementFeature situationJson = activeJsons.getFeatures().getFirst();

        final TrafficAnnouncementProperties jsonProperties = situationJson.getProperties();
        assertEquals(situationId, situation.getId());
        assertEquals(situationId, jsonProperties.situationId);

        final Instant situationVersionTime = situation.getSituationRecords().getFirst().getSituationRecordVersionTime();
        final Instant situationStart = situation.getSituationRecords().getFirst().getValidity().getValidityTimeSpecification().getOverallStartTime();
        final Instant situationEnd = situation.getSituationRecords().getFirst().getValidity().getValidityTimeSpecification().getOverallEndTime();
        final TimeAndDuration jsonTimeAndDuration = jsonProperties.announcements.getFirst().timeAndDuration;

        assertEquals(getVersionTime(start, imsJsonVersion.intVersion), situationVersionTime);
        assertEquals(getVersionTime(start, imsJsonVersion.intVersion), jsonProperties.releaseTime);

        assertEquals(start, situationStart);
        assertEquals(start, jsonTimeAndDuration.startTime);

        assertEquals(end, situationEnd);
        assertEquals(end, jsonTimeAndDuration.endTime);

        final String commentXml =
            situation.getSituationRecords().getFirst().getGeneralPublicComments().getFirst().getComment().getValues().getValues().stream()
                .filter(c -> c.getLang().equals("fi")).findFirst().orElseThrow().getValue();

        assertEquals(situationType, jsonProperties.getSituationType());
        if (situationType == TRAFFIC_ANNOUNCEMENT) {
            assertTrue(Sets.newHashSet(TrafficAnnouncementType.values()).contains(jsonProperties.getTrafficAnnouncementType()));
        }

        final TrafficAnnouncement announcement = jsonProperties.announcements.getFirst();
        assertTrue(commentXml.contains(announcement.title.trim()));
    }

    private void assertActiveMessageFound(final String situationId, final boolean foundInDatex2, final boolean foundInJson) {
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
            AssertUtil.assertCollectionSize(0, withJson.getFeatures());
        }
    }
}
