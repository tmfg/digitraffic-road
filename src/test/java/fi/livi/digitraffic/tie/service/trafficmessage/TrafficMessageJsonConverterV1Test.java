package fi.livi.digitraffic.tie.service.trafficmessage;

import static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.TrafficAnnouncementProperties.SituationType.TRAFFIC_ANNOUNCEMENT;
import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.Type.MultiPolygon;
import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.Type.Point;
import static fi.livi.digitraffic.tie.model.v1.datex2.TrafficAnnouncementType.GENERAL;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.getJsonVersionString;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.readStaticImsJmessageResourceContent;
import static fi.livi.digitraffic.tie.service.v2.datex2.RegionGeometryTestHelper.createNewRegionGeometry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import fi.livi.digitraffic.tie.AbstractWebServiceTestWithRegionGeometryServiceAndGitMock;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.Area;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.AreaType;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.ItineraryRoadLeg;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.Restriction;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.RoadAddressLocation.Direction;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.RoadWorkPhase;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncement;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementProperties;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.WeekdayTimePeriod;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.WorkType;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.ImsGeoJsonFeature;
import fi.livi.digitraffic.tie.helper.AssertHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationType;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.ImsJsonVersion;

/**
 * Tests reads Json traffic messages with different schema versions from src/test/resources/tloik/ims/versions/
 * and test converted result to be expected.
 */
public class TrafficMessageJsonConverterV1Test extends AbstractWebServiceTestWithRegionGeometryServiceAndGitMock {
    private static final Logger log = getLogger(TrafficMessageJsonConverterV1Test.class);

    @Autowired
    private TrafficMessageJsonConverterV1 datex2JsonConverterV1;

    @Autowired
    protected ObjectMapper objectMapper;
    private ObjectWriter writerForImsGeoJsonFeature;
    private ObjectReader readerForGeometry;

    @BeforeEach
    public void init() {
        writerForImsGeoJsonFeature = objectMapper.writerFor(ImsGeoJsonFeature.class);
        readerForGeometry = objectMapper.readerFor(Geometry.class);
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(0));
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(3));
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(7));
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(14));
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(73));
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(408));
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(419));
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(5898));

        when(regionGeometryDataServiceV1.getGeoJsonGeometryUnion(any(), any())).thenCallRealMethod();
    }

    @Test
    public void convertImsSimpleJsonVersionToGeoJsonFeatureObject_V1() throws IOException {
        for (final ImsJsonVersion jsonVersion : ImsJsonVersion.values()) {
            for (final SituationType st : SituationType.values()) {
                final String json = readStaticImsJmessageResourceContent(jsonVersion, st.name(), ZonedDateTime.now().minusHours(1), ZonedDateTime.now().plusHours(1), false);
                final Instant now = Instant.now();
                log.info("Try to convert SituationType {} from json version {} to TrafficAnnouncementFeature V2", st, jsonVersion);
                final TrafficAnnouncementFeature ta =
                    datex2JsonConverterV1.convertToFeatureJsonObject_V1(json, st, GENERAL, true, now);
                validateImsSimpleJsonVersionToGeoJsonFeatureObjectV3(st, jsonVersion, ta, now);
                log.info("Converted SituationType {} from json version {} to TrafficAnnouncementFeature V2", st, jsonVersion);
            }
        }
    }

    @Test
    public void convertImsSimpleJsonWithNullGeometryAndMultipleAreaAnnouncementsToGeoJsonFeatureObjectV3MergesAreas() throws IOException {
        final ImsJsonVersion jsonVersion = ImsJsonVersion.getLatestVersion();
        final SituationType situationType = SituationType.EXEMPTED_TRANSPORT;
        final String json = readStaticImsJmessageResourceContent(
            "classpath:tloik/ims/versions/" + getJsonVersionString(jsonVersion) + "/" + situationType + "_WITH_MULTIPLE_ANOUNCEMENTS.json",
            ImsJsonVersion.V0_2_12, ZonedDateTime.now().minusHours(1), ZonedDateTime.now().plusHours(1), false);
        final Instant now = Instant.now();
        log.info("Try to convert SituationType {} from json version {} to TrafficAnnouncementFeature V2", situationType, jsonVersion);
        final TrafficAnnouncementFeature ta =
            datex2JsonConverterV1.convertToFeatureJsonObject_V1(json, situationType, GENERAL, true, now);
        // _WITH_MULTIPLE_ANOUNCEMENTS.json contains five areas in 1. anouncement and one area in 2. anouncement.
        // Should be merged to MultiPolygon
        assertGeometry(ta.getGeometry(), MultiPolygon);
        assertEquals(5+1, ta.getGeometry().getCoordinates().size());
    }

    @Test
    public void convertImsSimpleJsonWithNullGeometryAndMultipleAreaAnnouncementsToGeoJsonFeatureObjectV3NotMergesAreas() throws IOException {
        final ImsJsonVersion jsonVersion = ImsJsonVersion.getLatestVersion();
        final SituationType situationType = SituationType.EXEMPTED_TRANSPORT;
        final String json = readStaticImsJmessageResourceContent(
            "classpath:tloik/ims/versions/" + getJsonVersionString(jsonVersion) + "/" + situationType + "_WITH_MULTIPLE_ANOUNCEMENTS.json",
            ImsJsonVersion.V0_2_12, ZonedDateTime.now().minusHours(1), ZonedDateTime.now().plusHours(1), false);
        final Instant now = Instant.now();
        log.info("Try to convert SituationType {} from json version {} to TrafficAnnouncementFeature V2", situationType, jsonVersion);
        final TrafficAnnouncementFeature ta =
            datex2JsonConverterV1.convertToFeatureJsonObject_V1(json, situationType, GENERAL, false, now);
        // _WITH_MULTIPLE_ANOUNCEMENTS.json contains five areas in 1. anouncement and one area in 2. anouncement.
        // Should be merged to MultiPolygon
        assertNull(ta.getGeometry());
    }

    @Test
    public void convertRoadWorkToFeatureJsonObjectWithAndWithoutGeometry() throws IOException {

        // Create announcement with area geometry
        final ImsGeoJsonFeature ims = ImsJsonMessageTestFactory
            .createTrafficAnnouncementJsonMessage(
                TRAFFIC_ANNOUNCEMENT,
                true, readerForGeometry);

        final String imsJson = writerForImsGeoJsonFeature.writeValueAsString(ims);
        final Instant now = Instant.now();
        // Convert to feature with includeAreaGeometry -parameter true -> should have the geometry
        final TrafficAnnouncementFeature resultWithGeometry =
            datex2JsonConverterV1.convertToFeatureJsonObject_V1(imsJson, SituationType.ROAD_WORK, null, true, now);
        // Convert to feature with includeAreaGeometry -parameter false -> should not have the area geometry
        final TrafficAnnouncementFeature resultWithoutGeometry =
            datex2JsonConverterV1.convertToFeatureJsonObject_V1(imsJson, SituationType.ROAD_WORK, null, false, now);

        assertNotNull(resultWithGeometry.getGeometry());
        assertNull(resultWithoutGeometry.getGeometry());
    }

    @Test
    public void convertTrafficAnnouncementWithoutAreaGeometryToFeatureJsonObjectShouldContainAlwaysGeometries() throws IOException {

        // Create announcement without area geometry
        final ImsGeoJsonFeature ims = ImsJsonMessageTestFactory
            .createTrafficAnnouncementJsonMessage(
                TRAFFIC_ANNOUNCEMENT,
                false, readerForGeometry);

        final String imsJson = writerForImsGeoJsonFeature.writeValueAsString(ims);
        final Instant now = Instant.now();
        // Convert to feature with includeAreaGeometry -parameter true -> should have the geometry
        final TrafficAnnouncementFeature resultWithGeometry =
            datex2JsonConverterV1.convertToFeatureJsonObject_V1(imsJson, SituationType.TRAFFIC_ANNOUNCEMENT, null, true, now);
        // Convert to feature with includeAreaGeometry -parameter false -> should still have the geometry as it's not an area geometry
        final TrafficAnnouncementFeature resultWithoutGeometry =
            datex2JsonConverterV1.convertToFeatureJsonObject_V1(imsJson, SituationType.TRAFFIC_ANNOUNCEMENT, null, false, now);

        assertNotNull(resultWithGeometry.getGeometry());
        assertNotNull(resultWithoutGeometry.getGeometry());
    }

    private void validateImsSimpleJsonVersionToGeoJsonFeatureObjectV3(final SituationType st, final ImsJsonVersion version,
                                                                      final TrafficAnnouncementFeature feature, final Instant now) {

        final TrafficAnnouncementProperties props = feature.getProperties();
        final TrafficAnnouncement announcement = props.announcements.get(0);

        // Common
        assertContacts(props);
        assertEarlyClosing(announcement, version, st);
        assertType(props, st);
        assertPropertiesTimes(props);
        assertEquals(now, feature.getLastModified());

        switch (st) {
            case TRAFFIC_ANNOUNCEMENT:
                assertEquals("GUID10000001", props.situationId);
                assertTitleContains(announcement, "Liikennetiedote");
                assertGeometry(feature.getGeometry(), Point);
                assertRoadAddressLocation(announcement, Direction.POS);
                assertFeatures(announcement, version,
                               Triple.of("Nopeusrajoitus", 50.0, "km/h"),
                               Triple.of("Huono ajokeli", null, null));
                break;
            case EXEMPTED_TRANSPORT:
                assertEquals("GUID10000002", props.situationId);
                assertTitleContains(announcement, "Erikoiskuljetus");
                assertGeometry(feature.getGeometry(), MultiPolygon);
                assertAreaLocation(announcement, version);
                assertFeatures(announcement, version,
                               Triple.of("Liikenne pysäytetään ajoittain", null, null),
                               Triple.of("Kuljetuksen leveys", 4.5, "m"));
                assertLastActiveItinerarySegment(announcement, version);
                break;
            case WEIGHT_RESTRICTION:
                assertEquals("GUID10000003", props.situationId);
                assertTitleContains(announcement, "Painorajoitus");
                assertGeometry(feature.getGeometry(), Point);
                assertRoadAddressLocation(announcement, Direction.BOTH);
                assertFeatures(announcement, version,
                               Triple.of("Ajoneuvon suurin sallittu massa", 2000.0, "kg"));

                break;
            case ROAD_WORK:
                assertEquals("GUID10000004", props.situationId);
                assertTitleContains(announcement, "Tietyö");
                assertGeometry(feature.getGeometry(), Point);
                assertRoadAddressLocation(announcement, Direction.UNKNOWN);
                assertFeatures(announcement, version,
                               Triple.of("Nopeusrajoitus", 40.0, "km/h"),
                               Triple.of("Silta pois käytöstä", null, null));
                assertRoadWorkPhases(announcement, version);

                break;
            default:
                throw new IllegalArgumentException("Unknown SituationType " + st);
        }
    }

    private void assertPropertiesTimes(final TrafficAnnouncementProperties props) {
        assertNotNull(props.releaseTime);
        assertNotNull(props.versionTime);
    }

    private void assertEarlyClosing(final TrafficAnnouncement announcement,
                                    final ImsJsonVersion version, final SituationType st) {
        if (st == SituationType.ROAD_WORK && version.version >= ImsJsonVersion.V0_2_8.version) {
            assertNotNull(announcement.earlyClosing);
        } else {
            assertNull(announcement.earlyClosing);
        }
    }

    private void assertRoadWorkPhases(final TrafficAnnouncement announcement,
                                      final ImsJsonVersion version) {
        if (version.version < ImsJsonVersion.V0_2_5.version) {
            AssertHelper.assertCollectionSize(0, announcement.roadWorkPhases);
        } else {
            AssertHelper.assertCollectionSize(1, announcement.roadWorkPhases);
            final RoadWorkPhase rwp = announcement.roadWorkPhases.get(0);
            assertNotNull(rwp.location);
            assertNotNull(rwp.locationDetails.roadAddressLocation);
            assertEquals(WeekdayTimePeriod.Weekday.MONDAY, rwp.workingHours.get(0).weekday);
            assertNotNull(rwp.workingHours.get(0).startTime);
            assertNotNull(rwp.workingHours.get(0).endTime);

            if (version.version >= ImsJsonVersion.V0_2_15.version) {
                assertEquals(WorkType.Type.CULVERT_REPLACEMENT, rwp.workTypes.get(1).type);
                assertEquals("Rummun vaihtotyö", rwp.workTypes.get(1).description);
            }

            if (version.version >= ImsJsonVersion.V0_2_14.version) {
                // restriction timeAndDuration && INTERMITTENT_STOPS_AND_CLOSURE_EFFECTIVE added
                final Restriction r3 = rwp.restrictions.get(2);
                assertNotNull(r3.restriction.description);
                assertNotNull(r3.restriction.name);
                assertEquals(Restriction.Type.INTERMITTENT_STOPS_AND_CLOSURE_EFFECTIVE, r3.type);
                assertNotNull(r3.restriction.timeAndDuration);
            }

            if (version.version >= ImsJsonVersion.V0_2_13.version) {
                assertNotNull(rwp.restrictionsLiftable, "restrictionsLiftable should exist");
                assertTrue(rwp.restrictionsLiftable);
                assertNotNull(rwp.restrictions.get(0).restriction.description);
                assertEquals(Restriction.Type.DETOUR_USING_ROADWAYS, rwp.restrictions.get(1).type);
                assertNotNull(rwp.restrictions.get(1).restriction.description);
            }

            if (version.version > ImsJsonVersion.V0_2_10.version) {
                assertEquals(WorkType.Type.LIGHTING, rwp.workTypes.get(0).type);
                assertEquals("Valaistustyö", rwp.workTypes.get(0).description);
                assertEquals(Restriction.Type.SPEED_LIMIT, rwp.restrictions.get(0).type);
                assertEquals("Nopeusrajoitus", rwp.restrictions.get(0).restriction.name);
                assertEquals(40.0, rwp.restrictions.get(0).restriction.quantity, 0.01);
                assertEquals("km/h", rwp.restrictions.get(0).restriction.unit);
            } else {
                assertEquals(WorkType.Type.OTHER, rwp.workTypes.get(0).type);
                assertEquals("Valaistustyö", rwp.workTypes.get(0).description);
            }

            if (version.version >= ImsJsonVersion.V0_2_8.version) {
                assertNotNull(rwp.severity, "Severity should exist");
            }

            if (version.version >= ImsJsonVersion.V0_2_17.version) {
                assertEquals(WeekdayTimePeriod.Weekday.TUESDAY, rwp.slowTrafficTimes.get(0).weekday);
                assertEquals(WeekdayTimePeriod.Weekday.WEDNESDAY, rwp.queuingTrafficTimes.get(0).weekday);
                assertNotNull(rwp.slowTrafficTimes.get(0).startTime);
                assertNotNull(rwp.slowTrafficTimes.get(0).endTime);
                assertNotNull(rwp.queuingTrafficTimes.get(0).startTime);
                assertNotNull(rwp.queuingTrafficTimes.get(0).endTime);
            }
        }
    }

    private void assertLastActiveItinerarySegment(final TrafficAnnouncement announcement,
                                                  final ImsJsonVersion version) {
        if (version.version < ImsJsonVersion.V0_2_6.version) {
            assertNull(announcement.lastActiveItinerarySegment);
        } else {
            assertNotNull(announcement.lastActiveItinerarySegment);
            assertNotNull(announcement.lastActiveItinerarySegment.startTime);
            assertNotNull(announcement.lastActiveItinerarySegment.endTime);
            final ItineraryRoadLeg leg =
                announcement.lastActiveItinerarySegment.legs.get(0).roadLeg;
            assertEquals("Kotikatu 1", announcement.lastActiveItinerarySegment.legs.get(0).roadLeg.roadName);
            assertNotNull(leg.startArea);
            assertNotNull(leg.endArea);
            assertNotNull(leg.roadNumber);
        }
    }

    private void assertType(final TrafficAnnouncementProperties props, final SituationType st) {
        assertEquals(st.name(), props.getSituationType().name());
        if (st.name().equals(SituationType.TRAFFIC_ANNOUNCEMENT.name())) {
            assertNotNull(props.getTrafficAnnouncementType());
        }
    }

    private void assertContacts(final TrafficAnnouncementProperties props) {
        assertNotNull(props.contact.email);
        assertNotNull(props.contact.phone);
    }

    private void assertGeometry(final Geometry<?> geometry,
                                final Geometry.Type type) {
        assertEquals(type, geometry.getType());
    }

    @SafeVarargs
    private void assertFeatures(final TrafficAnnouncement announcement,
                                final ImsJsonVersion version,
                                final Triple<String, Double, String>...features) {
        final double v = version.version;
        for (final Triple<String, Double, String> f : features) {
            announcement.features.stream().filter(value ->
                !Objects.equals(f.getLeft(), value.name) ||
                !Objects.equals(v >= ImsJsonVersion.V0_2_5.version ? f.getMiddle() : null, value.quantity) ||
                !Objects.equals(v >= ImsJsonVersion.V0_2_5.version ? f.getRight() : null, value.unit) ||
                !(v >= 2.13) || value.description != null
            ).findFirst().orElseThrow();
        }
        assertEquals(features.length, announcement.features.size());
    }

    private void assertRoadAddressLocation(final TrafficAnnouncement announcement,
                                           final Direction direction) {
        final fi.livi.digitraffic.tie.dto.trafficmessage.v1.RoadAddressLocation ral = announcement.locationDetails.roadAddressLocation;
        if (direction != null) {
            assertEquals(direction, ral.direction);
            assertNotNull(ral.primaryPoint);
            assertNotNull(ral.secondaryPoint);
        } else {
            assertNull(ral);
        }

    }

    private void assertAreaLocation(final TrafficAnnouncement announcement,
                                    final ImsJsonVersion version) {
        final int size = version.version >= ImsJsonVersion.V0_2_8.version ? 5 : 4;
        assertEquals(size, announcement.locationDetails.areaLocation.areas.size());

        assertContainsLocationType(announcement.locationDetails.areaLocation.areas, AreaType.COUNTRY);
        assertContainsLocationType(announcement.locationDetails.areaLocation.areas, AreaType.MUNICIPALITY);
        assertContainsLocationType(announcement.locationDetails.areaLocation.areas, AreaType.PROVINCE);
        assertContainsLocationType(announcement.locationDetails.areaLocation.areas, AreaType.WEATHER_REGION);
        if (version.version >= ImsJsonVersion.V0_2_8.version) {
            assertContainsLocationType(announcement.locationDetails.areaLocation.areas, AreaType.REGIONAL_STATE_ADMINISTRATIVE_AGENCY);
        }
    }

    private void assertContainsLocationType(final List<Area> areas, final AreaType type) {
        areas.stream().filter(a -> a.type.equals(type)).findFirst().orElseThrow();
    }

    private void assertTitleContains(final TrafficAnnouncement announcement,
                                     final String title) {
        assertTrue(announcement.title.contains(title));
    }
}