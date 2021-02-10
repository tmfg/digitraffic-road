package fi.livi.digitraffic.tie.service.v3.datex2;

import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.GUID_WITH_JSON;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.ImsXmlVersion;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.getSituationIdForSituationType;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.getVersionTime;
import static fi.livi.digitraffic.tie.service.v2.datex2.RegionGeometryTestHelper.createNewRegionGeometry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.Situation;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TimeAndDuration;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncement;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeatureCollection;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementProperties;
import fi.livi.digitraffic.tie.helper.AssertHelper;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationType;
import fi.livi.digitraffic.tie.model.v1.datex2.TrafficAnnouncementType;
import fi.livi.digitraffic.tie.service.AbstractDatex2DataServiceTest;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.ImsJsonVersion;

public class V3Datex2DataServiceTest extends AbstractDatex2DataServiceTest {
    private static final Logger log = getLogger(V3Datex2DataServiceTest.class);

    @Autowired
    private V3Datex2DataService v3Datex2DataService;

    @Autowired
    private TrafficMessageTestHelper trafficMessageTestHelper;

    @Autowired
    protected ObjectMapper objectMapper;

    private ObjectReader readerForGeometry;

    @SpyBean
    private V3RegionGeometryDataService v3RegionGeometryDataService;

    @Before
    public void init() {
        readerForGeometry = objectMapper.readerFor(Geometry.class);
        when(v3RegionGeometryDataService.getAreaLocationRegionEffectiveOn(eq(0), any())).thenReturn(createNewRegionGeometry(0));
        when(v3RegionGeometryDataService.getAreaLocationRegionEffectiveOn(eq(3), any())).thenReturn(createNewRegionGeometry(3));
        when(v3RegionGeometryDataService.getAreaLocationRegionEffectiveOn(eq(7), any())).thenReturn(createNewRegionGeometry(7));
        when(v3RegionGeometryDataService.getAreaLocationRegionEffectiveOn(eq(14), any())).thenReturn(createNewRegionGeometry(14));
        when(v3RegionGeometryDataService.getAreaLocationRegionEffectiveOn(eq(408), any())).thenReturn(createNewRegionGeometry(408));
        when(v3RegionGeometryDataService.getAreaLocationRegionEffectiveOn(eq(5898), any())).thenReturn(createNewRegionGeometry(5898));
    }

    @Test
    public void findActiveTrafficMessagesDatex2AndJsonEqualsForEveryVersionOfImsAndJson() throws IOException {

        for (final ImsXmlVersion imsXmlVersion : ImsXmlVersion.values()) {
            for (final ImsJsonVersion imsJsonVersion : ImsJsonVersion.values()) {
                for (final SituationType situationType : SituationType.values()) {
                    trafficMessageTestHelper.cleanDb();
                    final ZonedDateTime start = DateHelper.getZonedDateTimeNowWithoutMillisAtUtc().minusHours(1);
                    final ZonedDateTime end = start.plusHours(2);
                    trafficMessageTestHelper.initDataFromStaticImsResourceConent(imsXmlVersion, situationType, imsJsonVersion, start, end);
                    log.info("activeIncidentsDatex2AndJsonEquals with imsXmlVersion={}, imsJsonVersion={} and situationType={}",
                             imsXmlVersion, imsJsonVersion, situationType);
                    activeIncidentsDatex2AndJsonEquals(situationType, imsJsonVersion, getSituationIdForSituationType(situationType), start, end);
                }
            }
        }
    }

    @Test
    public void findTrafficMessagesBySituationIdWorksForEveryVersionOfImsAndJson() throws IOException {
        // One active incident per version
        for (final ImsXmlVersion imsXmlVersion : ImsXmlVersion.values()) {
            for (final ImsJsonVersion imsJsonVersion : ImsJsonVersion.values()) {
                for (final SituationType situationType : SituationType.values()) {
                    trafficMessageTestHelper.cleanDb();
                    trafficMessageTestHelper.initDataFromStaticImsResourceConent(imsXmlVersion, situationType, imsJsonVersion);
                    log.info("checkFindBySituationId with imsXmlVersion={}, imsJsonVersion={} and situationType={}",
                        imsXmlVersion, imsJsonVersion, situationType);
                    checkFindBySituationId(situationType, getSituationIdForSituationType(situationType));
                }
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
                    trafficMessageTestHelper.initDataFromStaticImsResourceConent(imsXmlVersion, situationType, imsJsonVersion, start, end);
                    log.info("activeIncidentsDatex2AndJsonEquals with imsXmlVersion={}, imsJsonVersion={} and situationType={}",
                             imsXmlVersion, imsJsonVersion, situationType);
                }
                activeIncidentsDatex2AndJsonEquals(situationType, ImsJsonVersion.getLatestVersion(), getSituationIdForSituationType(situationType), start, end);
            }
        }
    }

    @Ignore("TODO: Test find with true/false without area geometry parameter and geometry equals")
    @Test
    public void findActiveTrafficMessagesWithAreaGeometries() {
//        trafficMessageTestHelper.cleanDb();
//        // Create announcement with area geometry
//        final ImsGeoJsonFeature ims = ImsJsonMessageFactory
//            .createJsonMessage(fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.TrafficAnnouncementProperties.SituationType.TRAFFIC_ANNOUNCEMENT,
//                               fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.TrafficAnnouncementProperties.TrafficAnnouncementType.GENERAL,
//                               true, ZonedDateTime.now(),
//                               fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.Restriction.Type.NARROW_LANES,
//                               "Nopeusrajoitus", 40.0, "km/h",
//                               fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.Worktype.Type.BRIDGE,
//                       "tloik/ims/regions/00073_Helsinki.json",
//            readerForGeometry);
//
//        final String imsJson = writerForImsGeoJsonFeature.writeValueAsString(ims);
//        // Convert to feature with includeAreaGeometry -parameter true -> should have the geometry
//        final TrafficAnnouncementFeature resultWithGeometry =
//            datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJson, SituationType.TRAFFIC_ANNOUNCEMENT, null, true);
//        // Convert to feature with includeAreaGeometry -parameter false -> should still have the geometry as it's not an area geometry
//        final TrafficAnnouncementFeature resultWithoutGeometry =
//            datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJson, SituationType.TRAFFIC_ANNOUNCEMENT, null, false);
//
//        assertNotNull(resultWithGeometry.getGeometry());
//        assertNotNull(resultWithoutGeometry.getGeometry());
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
        // Not found, as both must exist
        assertActiveMessageFound(GUID_WITH_JSON, false, false);
    }

    private void checkFindBySituationId(final SituationType situationType, final String situationId) {
        final D2LogicalModel d2 = v3Datex2DataService.findAllBySituationId(situationId, situationType);
        final TrafficAnnouncementFeatureCollection jsons =
            v3Datex2DataService.findBySituationIdJson(situationId, true, situationType);

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
        final D2LogicalModel d2 = v3Datex2DataService.findActive(0, situationType);
        final List<Situation> activeSituations = ((SituationPublication) d2.getPayloadPublication()).getSituations();
        final TrafficAnnouncementFeatureCollection activeJsons = v3Datex2DataService.findActiveJson(0, true, situationType);

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
        if (situationType == SituationType.TRAFFIC_ANNOUNCEMENT) {
            assertEquals(TrafficAnnouncementType.GENERAL, jsonProperties.getTrafficAnnouncementType());
        }

        final TrafficAnnouncement announcement = jsonProperties.announcements.get(0);
        assertTrue(commentXml.contains(announcement.title.trim()));
    }

    private void assertActiveMessageFound(final String situationId, boolean foundInDatex2, boolean foundInJson) {
        final D2LogicalModel withOrWithoutJson = v3Datex2DataService.findActive(0);
        final SituationPublication situationPublication = ((SituationPublication) withOrWithoutJson.getPayloadPublication());
        final TrafficAnnouncementFeatureCollection withJson = v3Datex2DataService.findActiveJson(0, true);

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
