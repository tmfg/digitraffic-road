package fi.livi.digitraffic.tie.service.trafficmessage;

import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.Type.MultiPolygon;
import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.Type.Point;
import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.Type.Polygon;
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
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.AbstractRestWebTestWithRegionGeometryGitAndDataServiceMock;
import fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.RoadAddressLocation;
import fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.TrafficAnnouncement;
import fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.TrafficAnnouncementProperties;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.AreaType;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationType;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.ImsJsonVersion;

public class V2Datex2JsonConverterTest extends AbstractRestWebTestWithRegionGeometryGitAndDataServiceMock {
    private static final Logger log = getLogger(V2Datex2JsonConverterTest.class);

    @Autowired
    private V2Datex2JsonConverter v2Datex2JsonConverter;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    public void init() {
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(0));
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(3));
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(7));
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(14));
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(73));
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(408));
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(419));
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(5898));

        when(v3RegionGeometryDataServicMock.getGeoJsonGeometryUnion(any(), any())).thenCallRealMethod();
    }


    @Test
    public void convertImsSimpleJsonVersionToGeoJsonFeatureObjectV2() throws IOException {
        for(ImsJsonVersion jsonVersion : ImsJsonVersion.values()) {
            for (final SituationType st : SituationType.values()) {
                final String json = readStaticImsJmessageResourceContent(jsonVersion, st.name(), ZonedDateTime.now().minusHours(1), ZonedDateTime.now().plusHours(1), false);
                log.info("Try to convert SituationType {} from json version {} to TrafficAnnouncementFeature V2", st, jsonVersion);
                final fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.TrafficAnnouncementFeature ta =
                    v2Datex2JsonConverter.convertToFeatureJsonObjectV2(json, st.getDatex2MessageType());
                validateImsSimpleJsonVersionToGeoJsonFeatureObjectV2(st, jsonVersion, ta);
                log.info("Converted SituationType {} from json version {} to TrafficAnnouncementFeature V2", st, jsonVersion);
            }
        }
    }

    @Test
    public void convertImsSimpleJsonWithNullGeometryAndMultipleAreaAnnouncementsToGeoJsonFeatureObjectV2MergesAreas() throws IOException {
        final ImsJsonVersion jsonVersion = ImsJsonVersion.getLatestVersion();
        final SituationType situationType = SituationType.EXEMPTED_TRANSPORT;
        final String json = readStaticImsJmessageResourceContent(
            "classpath:tloik/ims/versions/" + getJsonVersionString(jsonVersion) + "/" + situationType + "_WITH_MULTIPLE_ANOUNCEMENTS.json",
            ImsJsonVersion.V0_2_12, ZonedDateTime.now().minusHours(1), ZonedDateTime.now().plusHours(1), false);
        log.info("Try to convert SituationType {} from json version {} to TrafficAnnouncementFeature V2", situationType, jsonVersion);
        final TrafficAnnouncementFeature ta =
            v2Datex2JsonConverter.convertToFeatureJsonObjectV2(json, Datex2MessageType.TRAFFIC_INCIDENT);
        // _WITH_MULTIPLE_ANOUNCEMENTS.json contains five areas in 1. anouncement and one area in 2. anouncement.
        // Should be merged to MultiPolygon
        assertGeometry(ta.getGeometry(), MultiPolygon);
        assertEquals(5+1, ta.getGeometry().getCoordinates().size());
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

    private void assertContainsLocationTypeV2(final List<fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.Area> areas, final AreaType type) {
        areas.stream().filter(a -> a.type.equals(type.getFromValue())).findFirst().orElseThrow();
    }

    private void assertTitleContains(final TrafficAnnouncement announcement,
                                     final String title) {
        assertTrue(announcement.title.contains(title));
    }
}