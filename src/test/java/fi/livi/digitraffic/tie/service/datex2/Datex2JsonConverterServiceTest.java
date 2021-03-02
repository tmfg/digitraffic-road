package fi.livi.digitraffic.tie.service.datex2;

import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.Type.MultiPolygon;
import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.Type.Point;
import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.Type.Polygon;
import static fi.livi.digitraffic.tie.model.v1.datex2.TrafficAnnouncementType.GENERAL;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.getJsonVersionString;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.readStaticImsJmessageResourceContent;
import static fi.livi.digitraffic.tie.service.v2.datex2.RegionGeometryTestHelper.createNewRegionGeometry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.RoadAddressLocation;
import fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.TrafficAnnouncement;
import fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.TrafficAnnouncementProperties;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.AreaType;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.ItineraryRoadLeg;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.Restriction;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.RoadWorkPhase;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.WorkingHour;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.Worktype;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.ImsGeoJsonFeature;
import fi.livi.digitraffic.tie.helper.AssertHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationType;
import fi.livi.digitraffic.tie.model.v1.datex2.TrafficAnnouncementType;
import fi.livi.digitraffic.tie.service.AbstractDatex2DataServiceTest;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.ImsJsonVersion;
import fi.livi.digitraffic.tie.service.v3.datex2.V3RegionGeometryDataService;

public class Datex2JsonConverterServiceTest extends AbstractDatex2DataServiceTest {
    private static final Logger log = getLogger(Datex2JsonConverterServiceTest.class);

    public static final String MAX_DURATION = "PT8H";
    public static final String MIN_DURATION = "PT6H";
    public static final String WORK_PHASE_ID = "WP1";

    @Autowired
    private Datex2JsonConverterService datex2JsonConverterService;

    @Autowired
    protected ObjectMapper objectMapper;
    private ObjectWriter writerForImsGeoJsonFeature;
    private ObjectReader readerForGeometry;

    @SpyBean
    private V3RegionGeometryDataService v3RegionGeometryDataService;

    @Before
    public void init() {
        writerForImsGeoJsonFeature = objectMapper.writerFor(ImsGeoJsonFeature.class);
        readerForGeometry = objectMapper.readerFor(Geometry.class);
        when(v3RegionGeometryDataService.getAreaLocationRegionEffectiveOn(eq(0), any())).thenReturn(createNewRegionGeometry(0));
        when(v3RegionGeometryDataService.getAreaLocationRegionEffectiveOn(eq(3), any())).thenReturn(createNewRegionGeometry(3));
        when(v3RegionGeometryDataService.getAreaLocationRegionEffectiveOn(eq(7), any())).thenReturn(createNewRegionGeometry(7));
        when(v3RegionGeometryDataService.getAreaLocationRegionEffectiveOn(eq(14), any())).thenReturn(createNewRegionGeometry(14));
        when(v3RegionGeometryDataService.getAreaLocationRegionEffectiveOn(eq(73), any())).thenReturn(createNewRegionGeometry(73));
        when(v3RegionGeometryDataService.getAreaLocationRegionEffectiveOn(eq(408), any())).thenReturn(createNewRegionGeometry(408));
        when(v3RegionGeometryDataService.getAreaLocationRegionEffectiveOn(eq(419), any())).thenReturn(createNewRegionGeometry(419));
        when(v3RegionGeometryDataService.getAreaLocationRegionEffectiveOn(eq(5898), any())).thenReturn(createNewRegionGeometry(5898));
    }

    @Test
    public void convertImsSimpleJsonVersionToGeoJsonFeatureObjectV2() throws IOException {
        for(ImsJsonVersion jsonVersion : ImsJsonVersion.values()) {
            for (final SituationType st : SituationType.values()) {
                final String json = readStaticImsJmessageResourceContent(jsonVersion, st, ZonedDateTime.now().minusHours(1), ZonedDateTime.now().plusHours(1));
                log.info("Try to convert SituationType {} from json version {} to TrafficAnnouncementFeature V2", st, jsonVersion);
                final fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.TrafficAnnouncementFeature ta =
                    datex2JsonConverterService.convertToFeatureJsonObjectV2(json, st.getDatex2MessageType());
                validateImsSimpleJsonVersionToGeoJsonFeatureObjectV2(st, jsonVersion, ta);
                log.info("Converted SituationType {} from json version {} to TrafficAnnouncementFeature V2", st, jsonVersion);
            }
        }
    }

    @Test
    public void convertImsSimpleJsonVersionToGeoJsonFeatureObjectV3() throws IOException {
        for(ImsJsonVersion jsonVersion : ImsJsonVersion.values()) {
            for (final SituationType st : SituationType.values()) {
                final String json = readStaticImsJmessageResourceContent(jsonVersion, st, ZonedDateTime.now().minusHours(1), ZonedDateTime.now().plusHours(1));
                log.info("Try to convert SituationType {} from json version {} to TrafficAnnouncementFeature V2", st, jsonVersion);
                final fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeature ta =
                    datex2JsonConverterService.convertToFeatureJsonObjectV3(json, st, GENERAL, true);
                validateImsSimpleJsonVersionToGeoJsonFeatureObjectV3(st, jsonVersion, ta);
                log.info("Converted SituationType {} from json version {} to TrafficAnnouncementFeature V2", st, jsonVersion);
            }
        }
    }

    @Test
    public void convertImsSimpleJsonWithMultipleAreaAnnouncementsToGeoJsonFeatureObjectV3MergesAreas() throws IOException {
        final ImsJsonVersion jsonVersion = ImsJsonVersion.getLatestVersion();
        final SituationType situationType = SituationType.EXEMPTED_TRANSPORT;
        final String json = readStaticImsJmessageResourceContent(
            "classpath:tloik/ims/versions/" + getJsonVersionString(jsonVersion) + "/" + situationType + "_WITH_MULTIPLE_ANOUNCEMENTS.json",
            ImsJsonVersion.V0_2_12, ZonedDateTime.now().minusHours(1), ZonedDateTime.now().plusHours(1));
        log.info("Try to convert SituationType {} from json version {} to TrafficAnnouncementFeature V2", situationType, jsonVersion);
        final fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeature ta =
            datex2JsonConverterService.convertToFeatureJsonObjectV3(json, situationType, GENERAL, true);
        // _WITH_MULTIPLE_ANOUNCEMENTS.json contains five areas in 1. anouncement and one area in 2. anouncement.
        // Should be merged to MultiPolygon
        assertGeometry(ta.getGeometry(), MultiPolygon);
        assertEquals(5+1, ta.getGeometry().getCoordinates().size());
    }

    private static final String SITUATION_ID1 = "GUID00000001";
    private static final String SITUATION_ID2 = "GUID00000002";
    private static final String FEATURE =
        "{\n" +
            "  \"type\" : \"Feature\",\n" +
            "  \"geometry\" : {\n" +
            "    \"type\" : \"Point\",\n" +
            "    \"coordinates\" : [ 23.77474, 61.50221 ]\n" +
            "  },\n" +
            "  \"properties\" : {\n" +
            "    \"situationId\" : \"GUID00000001\",\n" +
            "    \"version\" : 1,\n" +
            "    \"releaseTime\" : \"2019-12-13T14:43:18.388+02:00\",\n" +
            "    \"locationToDisplay\" : {\n" +
            "      \"e\" : 331529.0,\n" +
            "      \"n\" : 6805963.0\n" +
            "    },\n" +
            "    \"announcements\" : [ ],\n" +
            "    \"contact\" : {\n" +
            "      \"phone\" : \"12341234\",\n" +
            "      \"fax\" : \"43214321\",\n" +
            "      \"email\" : \"paivystys@liikenne.fi\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private static final String FEATURE_COLLECTION =
        "{\n" +
            "  \"type\": \"FeatureCollection\",\n" +
            "  \"features\": [\n" +
            "    FEATURES \n" +
            "  ]\n" +
            "}";

    @Test
    public void parseFeatureJsonsFromImsJson_Feature() throws JsonProcessingException {
        final Map<String, Triple<String, SituationType, TrafficAnnouncementType>> jsons = datex2JsonConverterService.parseFeatureJsonsFromImsJson(FEATURE);
        assertEquals(1, jsons.size());
        final ObjectReader reader = objectMapper.reader();
        final JsonNode original = reader.readTree(FEATURE);
        final JsonNode parsed = reader.readTree(jsons.get(SITUATION_ID1).getLeft());
        assertEquals(original, parsed);
    }

    @Test
    public void parseFeatureJsonsFromImsJson_FeatureCollection() throws JsonProcessingException {
        // Create feature collection with two features (just situationId differs)
        final String feature2 = changeSituationIdInFeature(FEATURE, SITUATION_ID1, SITUATION_ID2);
        final String featureCollection = createFeatureCollectionWithSituations(FEATURE, feature2);

        // parse features from collection and test src == result
        final Map<String, Triple<String, SituationType, TrafficAnnouncementType>> jsons = datex2JsonConverterService.parseFeatureJsonsFromImsJson(featureCollection);
        assertEquals(2, jsons.size());

        final ObjectReader reader = objectMapper.reader();
        final JsonNode original1 = reader.readTree(FEATURE);
        final JsonNode original2 = reader.readTree(feature2);
        assertNotEquals("Originals should differ with situationId", original1, original2);

        final JsonNode parsed1 = reader.readTree(jsons.get(SITUATION_ID1).getLeft());
        final JsonNode parsed2 = reader.readTree(jsons.get(SITUATION_ID2).getLeft());
        assertEquals(original1, parsed1);
        assertEquals(original2, parsed2);
        assertNotEquals(parsed1, parsed2);
    }

    @Test
    public void parseFeatureJsonsFromImsJson_FeatureEmptySituationId() {
        final Map<String, Triple<String, SituationType, TrafficAnnouncementType>> jsons =
            datex2JsonConverterService.parseFeatureJsonsFromImsJson(FEATURE.replace(SITUATION_ID1, ""));
        assertEquals(0, jsons.size());
    }

    @Test
    public void parseFeatureJsonsFromImsJson_FeatureNoSituationId() {
        final Map<String, Triple<String, SituationType, TrafficAnnouncementType>> jsons =
            datex2JsonConverterService.parseFeatureJsonsFromImsJson(FEATURE.replace("situationId", "situationI"));
        assertEquals(0, jsons.size());
    }

    @Test
    public void parseFeatureJsonsFromImsJson_InvalidJson() {
        final Map<String, Triple<String, SituationType, TrafficAnnouncementType>> jsons =
            datex2JsonConverterService.parseFeatureJsonsFromImsJson(FEATURE_COLLECTION);
        assertEquals(0, jsons.size());
    }

    @Test
    public void parseFeatureJsonsFromImsJson_FeatureCollectionEmptySituationId() {
        final String featureCollection = FEATURE_COLLECTION.replace("FEATURES", FEATURE.replace(SITUATION_ID1, ""));
        final Map<String, Triple<String, SituationType, TrafficAnnouncementType>> jsons =
            datex2JsonConverterService.parseFeatureJsonsFromImsJson(featureCollection);
        assertEquals(0, jsons.size());
    }

    @Test
    public void parseFeatureJsonsFromImsJson_FeatureCollectionNoSituationId() {
        final String featureCollection = FEATURE_COLLECTION.replace("FEATURES", FEATURE.replace("situationId", "situationsId"));
        final Map<String, Triple<String, SituationType, TrafficAnnouncementType>> jsons =
            datex2JsonConverterService.parseFeatureJsonsFromImsJson(featureCollection);
        assertEquals(0, jsons.size());
    }

    @Test
    public void convertRoadWorkToFeatureJsonObjectWithAndWithoutGeometry() throws IOException {

        // Create announcement with area geometry
        final ImsGeoJsonFeature ims = ImsJsonMessageFactory
            .createTrafficAnnouncementJsonMessage(
                fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.TrafficAnnouncementProperties.SituationType.TRAFFIC_ANNOUNCEMENT,
                true, readerForGeometry);

        final String imsJson = writerForImsGeoJsonFeature.writeValueAsString(ims);
        // Convert to feature with includeAreaGeometry -parameter true -> should have the geometry
        final TrafficAnnouncementFeature resultWithGeometry =
            datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJson, SituationType.ROAD_WORK, null, true);
        // Convert to feature with includeAreaGeometry -parameter false -> should not have the area geometry
        final TrafficAnnouncementFeature resultWithoutGeometry =
            datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJson, SituationType.ROAD_WORK, null, false);

        assertNotNull(resultWithGeometry.getGeometry());
        assertNull(resultWithoutGeometry.getGeometry());
    }

    @Test
    public void convertTrafficAnnouncementWithoutAreaGeometryToFeatureJsonObjectShouldContainAlwaysGeometries() throws IOException {

        // Create announcement without area geometry
        final ImsGeoJsonFeature ims = ImsJsonMessageFactory
            .createTrafficAnnouncementJsonMessage(
                fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.TrafficAnnouncementProperties.SituationType.TRAFFIC_ANNOUNCEMENT,
                false, readerForGeometry);

        final String imsJson = writerForImsGeoJsonFeature.writeValueAsString(ims);
        // Convert to feature with includeAreaGeometry -parameter true -> should have the geometry
        final TrafficAnnouncementFeature resultWithGeometry =
            datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJson, SituationType.TRAFFIC_ANNOUNCEMENT, null, true);
        // Convert to feature with includeAreaGeometry -parameter false -> should still have the geometry as it's not an area geometry
        final TrafficAnnouncementFeature resultWithoutGeometry =
            datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJson, SituationType.TRAFFIC_ANNOUNCEMENT, null, false);

        assertNotNull(resultWithGeometry.getGeometry());
        assertNotNull(resultWithoutGeometry.getGeometry());
    }

    private void validateImsSimpleJsonVersionToGeoJsonFeatureObjectV2(final SituationType st, final ImsJsonVersion version,
                                                                      final fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.TrafficAnnouncementFeature feature) {

        final TrafficAnnouncementProperties props = feature.getProperties();
        final TrafficAnnouncement announcement = props.announcements.get(0);

        // Common
        assertEquals(st.getDatex2MessageType(), props.getMessageType());
        assertContacts(props, version);
        assertLocationToDisplay(props, version);

        switch (st) {
        case TRAFFIC_ANNOUNCEMENT:
            assertEquals("GUID10000001", props.situationId);
            assertTitleContains(announcement, "Liikennetiedote");
            assertGeometry(feature.getGeometry(), Point);
            assertRoadAddressLocationV2(announcement, RoadAddressLocation.Direction.POS);
            assertFeatures(announcement, "Nopeusrajoitus", "Huono ajokeli");
            break;
        case EXEMPTED_TRANSPORT:
            assertEquals("GUID10000002", props.situationId);
            assertTitleContains(announcement, "Erikoiskuljetus");
            assertGeometry(feature.getGeometry(), Polygon);
            assertAreaLocation(announcement, version);
            assertFeatures(announcement, "Liikenne pysäytetään ajoittain", "Kuljetuksen leveys");
            break;
        case WEIGHT_RESTRICTION:
            assertEquals("GUID10000003", props.situationId);
            assertTitleContains(announcement, "Painorajoitus");
            assertGeometry(feature.getGeometry(), Point);
            assertRoadAddressLocationV2(announcement, RoadAddressLocation.Direction.BOTH);
            assertFeatures(announcement, "Ajoneuvon suurin sallittu massa");
            break;
        case ROAD_WORK:
            assertEquals("GUID10000004", props.situationId);
            assertTitleContains(announcement, "Tietyö");
            assertGeometry(feature.getGeometry(), Point);
            assertRoadAddressLocationV2(announcement, RoadAddressLocation.Direction.UNKNOWN);
            assertFeatures(announcement, "Nopeusrajoitus", "Silta pois käytöstä");
            break;
        default:
            throw new IllegalArgumentException("Unknown SituationType " + st);
        }

    }

    private void validateImsSimpleJsonVersionToGeoJsonFeatureObjectV3(final SituationType st, final ImsJsonVersion version,
                                                                      final fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeature feature) {

        final fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementProperties props = feature.getProperties();
        final fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncement announcement = props.announcements.get(0);

        // Common
        assertContacts(props, version);
        assertEarlyClosing(announcement, version, st);
        assertType(props, st);

        switch (st) {
            case TRAFFIC_ANNOUNCEMENT:
                assertEquals("GUID10000001", props.situationId);
                assertTitleContains(announcement, "Liikennetiedote");
                assertGeometry(feature.getGeometry(), Point);
                assertRoadAddressLocation(announcement, fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.RoadAddressLocation.Direction.POS);
                assertFeatures(announcement, version, Triple.of("Nopeusrajoitus", 50.0, "km/h"),
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
                assertRoadAddressLocation(announcement, fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.RoadAddressLocation.Direction.BOTH);
                assertFeatures(announcement, version, Triple.of("Ajoneuvon suurin sallittu massa", 2000.0, "kg"));

                break;
            case ROAD_WORK:
                assertEquals("GUID10000004", props.situationId);
                assertTitleContains(announcement, "Tietyö");
                assertGeometry(feature.getGeometry(), Point);
                assertRoadAddressLocation(announcement, fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.RoadAddressLocation.Direction.UNKNOWN);
                assertFeatures(announcement, version,
                                             Triple.of("Nopeusrajoitus", 40.0, "km/h"),
                                             Triple.of("Silta pois käytöstä", null, null));
                assertRoadWorkPhases(announcement, version);

                break;
            default:
                throw new IllegalArgumentException("Unknown SituationType " + st);
        }
    }

    private void assertEarlyClosing(fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncement announcement,
                                    ImsJsonVersion version, SituationType st) {
        if (st == SituationType.ROAD_WORK && version.version >= 2.08) {
            assertNotNull(announcement.earlyClosing);
        } else {
            assertNull(announcement.earlyClosing);
        }
    }

    private void assertRoadWorkPhases(final fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncement announcement,
                                      final ImsJsonVersion version) {
        if (version.version < 2.05) {
            AssertHelper.assertCollectionSize(0, announcement.roadWorkPhases);
        } else {
            AssertHelper.assertCollectionSize(1, announcement.roadWorkPhases);
            final RoadWorkPhase rwp = announcement.roadWorkPhases.get(0);
            assertNotNull(rwp.location);
            assertNotNull(rwp.locationDetails.roadAddressLocation);
            assertEquals(WorkingHour.Weekday.MONDAY, rwp.workingHours.get(0).weekday);
            assertNotNull(rwp.workingHours.get(0).startTime);
            assertNotNull(rwp.workingHours.get(0).endTime);

            if (version.version > 2.10) {
                assertEquals(Worktype.Type.LIGHTING, rwp.worktypes.get(0).type);
                assertEquals("Valaistustyö", rwp.worktypes.get(0).description);
                assertEquals(Restriction.Type.SPEED_LIMIT, rwp.restrictions.get(0).type);
                assertEquals("Nopeusrajoitus", rwp.restrictions.get(0).restriction.name);
                assertEquals(40.0, rwp.restrictions.get(0).restriction.quantity, 0.01);
                assertEquals("km/h", rwp.restrictions.get(0).restriction.unit);
            } else {
                assertEquals(Worktype.Type.OTHER, rwp.worktypes.get(0).type);
                assertEquals("Valaistustyö", rwp.worktypes.get(0).description);
            }

            if (version.version >= 2.08) {
                Assert.notNull(rwp.severity, "Severity should exist");
            }
        }
    }

    private void assertLastActiveItinerarySegment(final fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncement announcement,
                                                  final ImsJsonVersion version) {
        if (version.version < 2.06) {
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

    private void assertType(final fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementProperties props, SituationType st) {
        assertEquals(st, props.getSituationType());
        if (st == SituationType.TRAFFIC_ANNOUNCEMENT) {
            assertNotNull(props.getTrafficAnnouncementType());
        }
    }



    private void assertLocationToDisplay(final TrafficAnnouncementProperties props, final ImsJsonVersion version) {
        if (version.version < 2.08) {
            assertNotNull(props.locationToDisplay);
        } else {
            assertNull(props.locationToDisplay);
        }
    }

    private void assertContacts(final TrafficAnnouncementProperties props,
                                final ImsJsonVersion version) {
        assertNotNull(props.contact.email);
        assertNotNull(props.contact.phone);
        if (version.version < 2.09) {
            assertNotNull(props.contact.fax);
        } else {
            assertNull(props.contact.fax);
        }
    }

    private void assertContacts(final fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementProperties props,
                                  final ImsJsonVersion version) {
        assertNotNull(props.contact.email);
        assertNotNull(props.contact.phone);
    }

    private void assertGeometry(final Geometry<?> geometry,
                                final Geometry.Type type) {
        assertEquals(type, geometry.getType());
    }

    private void assertFeatures(final TrafficAnnouncement announcement,
                                final String...features) {
        for (final String f : features) {
            announcement.features.stream().filter(value -> value.contains(f)).findFirst().orElseThrow();
        }
        assertEquals(features.length, announcement.features.size());
    }

    private void assertFeatures(final fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncement announcement,
                                final ImsJsonVersion version,
                                final Triple<String, Double, String>...features) {
        final double v = version.version;
        for (final Triple<String, Double, String> f : features) {
            announcement.features.stream().filter(value -> Objects.equals(f.getLeft(), value.name) &&
                                                           Objects.equals(v >= 2.05 ? f.getMiddle() : null, value.quantity) &&
                                                           Objects.equals(v >= 2.05 ? f.getRight() : null, value.unit)).findFirst().orElseThrow();
        }
        assertEquals(features.length, announcement.features.size());
    }

    private void assertRoadAddressLocationV2(final TrafficAnnouncement announcement,
                                             final RoadAddressLocation.Direction direction) {
        final RoadAddressLocation ral = announcement.locationDetails.roadAddressLocation;
        if (direction != null) {
            assertEquals(direction, ral.direction);
            assertNotNull(ral.primaryPoint);
            assertNotNull(ral.secondaryPoint);
        } else {
            assertNull(ral);
        }

    }

    private void assertRoadAddressLocation(final fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncement announcement,
                                           final fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.RoadAddressLocation.Direction direction) {
        final fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.RoadAddressLocation ral = announcement.locationDetails.roadAddressLocation;
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
        final int size = version.version >= 2.08 ? 5 : 4;
        assertEquals(size, announcement.locationDetails.areaLocation.areas.size());

        assertContainsLocationTypeV2(announcement.locationDetails.areaLocation.areas, AreaType.COUNTRY);
        assertContainsLocationTypeV2(announcement.locationDetails.areaLocation.areas, AreaType.MUNICIPALITY);
        assertContainsLocationTypeV2(announcement.locationDetails.areaLocation.areas, AreaType.PROVINCE);
        assertContainsLocationTypeV2(announcement.locationDetails.areaLocation.areas, AreaType.WEATHER_REGION);
        if (version.version >= 2.08) {
            assertContainsLocationTypeV2(announcement.locationDetails.areaLocation.areas, AreaType.REGIONAL_STATE_ADMINISTRATIVE_AGENCY);
        }

    }

    private void assertAreaLocation(final fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncement announcement,
                                    final ImsJsonVersion version) {
        final int size = version.version >= 2.08 ? 5 : 4;
        assertEquals(size, announcement.locationDetails.areaLocation.areas.size());

        assertContainsLocationType(announcement.locationDetails.areaLocation.areas, AreaType.COUNTRY);
        assertContainsLocationType(announcement.locationDetails.areaLocation.areas, AreaType.MUNICIPALITY);
        assertContainsLocationType(announcement.locationDetails.areaLocation.areas, AreaType.PROVINCE);
        assertContainsLocationType(announcement.locationDetails.areaLocation.areas, AreaType.WEATHER_REGION);
        if (version.version >= 2.08) {
            assertContainsLocationType(announcement.locationDetails.areaLocation.areas, AreaType.REGIONAL_STATE_ADMINISTRATIVE_AGENCY);
        }
    }

    private void assertContainsLocationTypeV2(final List<fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.Area> areas, final AreaType type) {
        areas.stream().filter(a -> a.type.equals(type.getFromValue())).findFirst().orElseThrow();
    }

    private void assertContainsLocationType(final List<fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.Area> areas, final AreaType type) {
        areas.stream().filter(a -> a.type.equals(type)).findFirst().orElseThrow();
    }

    private void assertTitleContains(final TrafficAnnouncement announcement,
                                     final String title) {
        assertTrue(announcement.title.contains(title));
    }

    private void assertTitleContains(final fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncement announcement,
                                     final String title) {
        assertTrue(announcement.title.contains(title));
    }

    private String changeSituationIdInFeature(final String featureToEdit, final String situationIdToReplace, final String replacementSituationId) {
        return StringUtils.replace(featureToEdit, situationIdToReplace, replacementSituationId);
    }

    private String createFeatureCollectionWithSituations(final String... feature) {
        final String features = StringUtils.joinWith(", ", feature);
        return StringUtils.replace(FEATURE_COLLECTION, "FEATURES", features);
    }
}