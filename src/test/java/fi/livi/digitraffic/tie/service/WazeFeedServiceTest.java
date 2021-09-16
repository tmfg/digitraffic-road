package fi.livi.digitraffic.tie.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.converter.WazeDatex2JsonConverter;
import fi.livi.digitraffic.tie.dto.WazeFeedAnnouncementDto;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.RoadAddressLocation;
import fi.livi.digitraffic.tie.metadata.geojson.LineString;
import fi.livi.digitraffic.tie.metadata.geojson.MultiLineString;
import fi.livi.digitraffic.tie.metadata.geojson.MultiPoint;
import fi.livi.digitraffic.tie.metadata.geojson.MultiPolygon;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.Polygon;
import fi.livi.digitraffic.tie.model.v1.datex2.TrafficAnnouncementType;

@Import({ JacksonAutoConfiguration.class })
public class WazeFeedServiceTest extends AbstractRestWebTest {

    @Autowired
    private WazeFeedService wazeFeedService;

    @Autowired
    private WazeFeedServiceTestHelper wazeFeedServiceTestHelper;

    @AfterEach
    public void cleanup() {
        wazeFeedServiceTestHelper.cleanup();
    }

    @Test
    public void getAListOfWazeAnnouncements() {
        wazeFeedServiceTestHelper.insertAccident();

        final List<WazeFeedAnnouncementDto> allActive = wazeFeedService.findActive();

        assertEquals(1, allActive.size());
    }

    @Test
    public void announcementIsProperlyFormatted() {
        final String situationId = "GUID12345";
        final String situationRecordId = "GUID12346";
        final String startTime = "2021-07-28T13:09:47.470Z";
        final String sender = "Fintraffic Tieliikennekeskus Tampere";
        final String street = "24";
        final String email = "tampere.liikennekeskus@fintraffic.fi";
        final String phone = "02002100";
        final String additionalInformation = "Liikenne- ja kelitiedot verkossa: https://liikennetilanne.fintraffic.fi/";
        final String comment = "Autoilijoita suositellaan kiertämään onnettomuuspaikka jo kauempaa vaihtoehtoisia reittejä pitkin.";
        final String descriptionLine = "Tie 24 välillä Lahti - Jämsä, Kuhmoinen.\nTarkempi paikka: Välillä Kuhmoinen - Suoniemi.";
        final String estimatedDurationInformal = "1 - 3 tuntia";
        final List<String> featureList =
            List.of("Onnettomuus", "Onnettomuuspaikan pelastus- ja raivaustyöt käynnissä", "Tie on suljettu liikenteeltä");

        final Map<String, Optional<String>> hm =
            WazeFeedServiceTestHelper.createIncidentMap(additionalInformation, comment, descriptionLine, estimatedDurationInformal, startTime,
                email, phone, sender, situationId, street, "accident_report");

        final String jsonMessage = wazeFeedServiceTestHelper.createJsonMessage(hm, RoadAddressLocation.Direction.BOTH, featureList);

        wazeFeedServiceTestHelper.insertAccident(situationId, situationRecordId, jsonMessage, TrafficAnnouncementType.ACCIDENT_REPORT);

        final String description = featureList.stream().map(x -> x + ".").collect(Collectors.joining(" "));

        final List<WazeFeedAnnouncementDto> allActive = wazeFeedService.findActive();
        assertEquals(1, allActive.size());

        final WazeFeedAnnouncementDto announcement = allActive.get(0);

        assertEquals(situationId, announcement.id);
        assertEquals(String.format("Road %s", street), announcement.street);
        assertEquals(WazeFeedAnnouncementDto.Type.ACCIDENT, announcement.type);
        assertEquals(description.substring(0, 37) + "...", announcement.description);
        assertTrue(announcement.description.length() <= 40);
        assertEquals("FINTRAFFIC", announcement.reference);
    }

    @Test
    public void pointInAnnouncement() {
        final Point point = new Point(25.182835, 61.575153);

        wazeFeedServiceTestHelper.insertAccident("GUID1234", "GUID12345", RoadAddressLocation.Direction.BOTH, "130", "Liikennevirasto", point);

        final List<WazeFeedAnnouncementDto> announcements = wazeFeedService.findActive();
        assertEquals(1, announcements.size());

        final WazeFeedAnnouncementDto announcement = announcements.get(0);
        assertEquals("25.182835 61.575153", announcement.polyline);
        assertNull(announcement.direction);
    }

    @Test
    public void incorrectGeometryType() {
        final List<Double> point = List.of(1.0, 2.2);
        final List<List<Double>> coords = List.of(point);
        final List<List<List<Double>>> poly = List.of(coords);

        assertTrue(WazeDatex2JsonConverter.formatPolyline(new MultiPoint(coords), null).isEmpty());
        assertTrue(WazeDatex2JsonConverter.formatPolyline(new LineString(coords), null).isEmpty());
        assertTrue(WazeDatex2JsonConverter.formatPolyline(new MultiPolygon(poly), null).isEmpty());
        assertTrue(WazeDatex2JsonConverter.formatPolyline(new Polygon(poly), null).isEmpty());
    }

    @Test
    public void onewayDirectionInAccidents() {
        wazeFeedServiceTestHelper.insertAccident("GUID1234", "GUID12345", RoadAddressLocation.Direction.POS, "130", "Liikennevirasto");
        wazeFeedServiceTestHelper.insertAccident("GUID1235", "GUID12346", RoadAddressLocation.Direction.NEG, "129", "Liikennevirasto");
        wazeFeedServiceTestHelper.insertAccident("GUID1236", "GUID12347", RoadAddressLocation.Direction.UNKNOWN, "131", "Liikennevirasto");

        final List<WazeFeedAnnouncementDto> announcements = wazeFeedService.findActive();
        assertEquals(3, announcements.size());

        announcements.forEach(x -> assertEquals(WazeFeedAnnouncementDto.Direction.ONE_DIRECTION, x.direction));
    }

    @Test
    public void unsupportedGeometryTypesAreFilteredFromResults() {
        final List<List<Double>> coords = List.of(List.of(25.180874, 61.569262), List.of(25.180826, 61.569394));
        final MultiLineString geometry = new MultiLineString();
        geometry.addLineString(coords);

        // using supported MultiLineString
        wazeFeedServiceTestHelper.insertAccident("GUID1234", "GUID12345", RoadAddressLocation.Direction.BOTH, "130", "Liikennevirasto", geometry);

        // unsupported MultiPolygon
        wazeFeedServiceTestHelper.insertAccident("GUID1235", "GUID12346", RoadAddressLocation.Direction.BOTH, "130", "Liikennevirasto", new MultiPolygon(List.of(coords)));

        // expect the multipolygon version to be filtered out
        final List<WazeFeedAnnouncementDto> announcements = wazeFeedService.findActive();
        assertEquals(1, announcements.size());
    }

    @Test
    public void bothDirectionsCoordinatesAreReturnedAsProperlyFormattedPolyline() {
        final MultiLineString geometry = new MultiLineString();
        geometry.addLineString(List.of(List.of(25.180874, 61.569262), List.of(25.180826, 61.569394)));

        wazeFeedServiceTestHelper.insertAccident("GUID1234", "GUID12345", RoadAddressLocation.Direction.BOTH, "130", "Liikennevirasto", geometry);
        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive().get(0);

        assertEquals("25.180874 61.569262 25.180826 61.569394 25.180826 61.569394 25.180874 61.569262", announcement.polyline);
        assertEquals(WazeFeedAnnouncementDto.Direction.BOTH_DIRECTIONS, announcement.direction);
    }

    @Test
    public void simpleOneDirectionPolyline() {
        final MultiLineString geometry1 = new MultiLineString();
        geometry1.addLineString(List.of(List.of(25.180874, 61.569262), List.of(25.180826, 61.569394)));

        final MultiLineString geometry2 = new MultiLineString();
        final List<List<Double>> coords = new ArrayList<>();
        coords.add(List.of(25.182835, 61.575153));
        coords.add(List.of(25.183062, 61.575386));
        coords.add(List.of(25.18328, 61.575587));
        coords.add(List.of(25.180874, 61.569262));
        coords.add(List.of(25.180826, 61.569394));
        geometry2.addLineString(coords);

        final Optional<String> maybePolyline1 = WazeDatex2JsonConverter.formatPolyline(geometry1, WazeFeedAnnouncementDto.Direction.ONE_DIRECTION);
        final Optional<String> maybePolyline2 = WazeDatex2JsonConverter.formatPolyline(geometry2, WazeFeedAnnouncementDto.Direction.ONE_DIRECTION);

        assertEquals("25.180874 61.569262 25.180826 61.569394", maybePolyline1.orElse(null));
        assertEquals("25.182835 61.575153 25.183062 61.575386 25.18328 61.575587 25.180874 61.569262 25.180826 61.569394", maybePolyline2.orElse(null));
    }

    @Test
    public void bothDirectionsMultiLineStringToPolyline() {
        final MultiLineString geometry = new MultiLineString();
        final List<List<Double>> coords1 = new ArrayList<>();
        final List<List<Double>> coords2 = new ArrayList<>();

        coords1.add(List.of(25.180874, 61.569262));
        coords1.add(List.of(25.180826, 61.569394));
        coords1.add(List.of(25.180754, 61.569586));
        coords1.add(List.of(25.180681, 61.569794));
        coords1.add(List.of(25.180601, 61.570065));

        coords2.add(List.of(25.212664, 61.586387));
        coords2.add(List.of(25.212664, 61.586387));

        geometry.addLineString(coords1);
        geometry.addLineString(coords2);

        final Optional<String> maybePolyline = WazeDatex2JsonConverter.formatPolyline(geometry, WazeFeedAnnouncementDto.Direction.BOTH_DIRECTIONS);
        assertTrue(maybePolyline.isPresent());

        final String polyline = maybePolyline.get();
        assertEquals("25.180874 61.569262 25.180826 61.569394 25.180754 61.569586 25.180681 61.569794 25.180601 61.570065 25.212664 61.586387 25.212664 61.586387 25.212664 61.586387 25.212664 61.586387 25.180601 61.570065 25.180681 61.569794 25.180754 61.569586 25.180826 61.569394 25.180874 61.569262", polyline);
    }

    @Test
    public void oneDirectionMultiLineStringToPolyline() {
        final MultiLineString geometry = new MultiLineString();
        final List<List<Double>> coords1 = new ArrayList<>();
        final List<List<Double>> coords2 = new ArrayList<>();

        coords1.add(List.of(25.180874, 61.569262));
        coords1.add(List.of(25.180826, 61.569394));
        coords1.add(List.of(25.180754, 61.569586));
        coords1.add(List.of(25.180681, 61.569794));
        coords1.add(List.of(25.180601, 61.570065));

        coords2.add(List.of(25.212664, 61.586387));
        coords2.add(List.of(25.212674, 61.586377));

        geometry.addLineString(coords1);
        geometry.addLineString(coords2);

        final Optional<String> maybePolyline = WazeDatex2JsonConverter.formatPolyline(geometry, WazeFeedAnnouncementDto.Direction.ONE_DIRECTION);
        assertTrue(maybePolyline.isPresent());

        final String polyline = maybePolyline.get();
        assertEquals("25.180874 61.569262 25.180826 61.569394 25.180754 61.569586 25.180681 61.569794 25.180601 61.570065 25.212664 61.586387 25.212674 61.586377", polyline);
    }

    @Test
    public void checkForNullValues() {
        final List<String> featureList = List.of();
        final String startTime = "2021-07-28T13:09:47.470Z";

        // create multiple invalid announcements
        final List<Map<String, Optional<String>>> incidentMaps = List.of(
            WazeFeedServiceTestHelper.createIncidentMap("additional", "comment", "description", "estimation", startTime, "email", "phone", "sender", wazeFeedServiceTestHelper.nextSituationRecord(), null, "general"),
            WazeFeedServiceTestHelper.createIncidentMap(null, null, null, null, null, null, null, null, wazeFeedServiceTestHelper.nextSituationRecord(), "1", null),
            WazeFeedServiceTestHelper.createIncidentMap(null, null, null, null, null, null, null, null, wazeFeedServiceTestHelper.nextSituationRecord(), null, null)
        );

        // check null values in json message
        incidentMaps
            .forEach(incident -> {
                final String situationId = incident.get("situationId").orElseThrow();
                final String json = wazeFeedServiceTestHelper.createJsonMessage(incident, RoadAddressLocation.Direction.BOTH, featureList);

                wazeFeedServiceTestHelper.insertAccident(situationId, situationId, json, TrafficAnnouncementType.GENERAL);
            });

        // check for null geometry
        wazeFeedServiceTestHelper.insertAccident("GUID1234", "GUID12345", RoadAddressLocation.Direction.BOTH, "130", "FINTRAFFIC", null);

        // check for null features list
        final Map<String, Optional<String>> incident = WazeFeedServiceTestHelper.createIncidentMap("additional", "comment", "description", "estimation", startTime, "email", "phone", "sender", wazeFeedServiceTestHelper.nextSituationRecord(), "1", "general");
        final String json = wazeFeedServiceTestHelper.createJsonMessage(incident, RoadAddressLocation.Direction.BOTH, null);
        final String situationId = incident.get("situationId").orElseThrow();
        wazeFeedServiceTestHelper.insertAccident(situationId, situationId, json, TrafficAnnouncementType.GENERAL);

        final List<WazeFeedAnnouncementDto> allActive = wazeFeedService.findActive();
        assertEquals(0, allActive.size());
    }

    @Test
    public void filterPreliminaryAccidentReports() {
        final String situationId = "GUID12345";
        final String situationRecordId = "GUID12346";
        final String startTime = "2021-07-28T13:09:47.470Z";
        final String sender = "Fintraffic Tieliikennekeskus Tampere";
        final String street = "24";
        final String email = "tampere.liikennekeskus@fintraffic.fi";
        final String phone = "02002100";
        final String additionalInformation = "Liikenne- ja kelitiedot verkossa: https://liikennetilanne.fintraffic.fi/";
        final String comment = "Autoilijoita suositellaan kiertämään onnettomuuspaikka jo kauempaa vaihtoehtoisia reittejä pitkin.";
        final String descriptionLine = "Tie 24 välillä Lahti - Jämsä, Kuhmoinen.\nTarkempi paikka: Välillä Kuhmoinen - Suoniemi.";
        final String estimatedDurationInformal = "1 - 3 tuntia";
        final List<String> featureList =
            List.of("Onnettomuus", "Onnettomuuspaikan pelastus- ja raivaustyöt käynnissä", "Tie on suljettu liikenteeltä");

        final Map<String, Optional<String>> hm =
            WazeFeedServiceTestHelper.createIncidentMap(additionalInformation, comment, descriptionLine, estimatedDurationInformal, startTime,
                email, phone, sender, situationId, street, "preliminary_accident_report");

        final String jsonMessage = wazeFeedServiceTestHelper.createJsonMessage(hm, RoadAddressLocation.Direction.BOTH, featureList);

        // datex2 database record having preliminary accident report type
        wazeFeedServiceTestHelper.insertAccident(situationId, situationRecordId, jsonMessage, TrafficAnnouncementType.PRELIMINARY_ACCIDENT_REPORT);

        // database line having incorrect announcement type, but real type still in json format
        wazeFeedServiceTestHelper.insertAccident(situationId, situationRecordId, jsonMessage, TrafficAnnouncementType.GENERAL);

        final List<WazeFeedAnnouncementDto> announcements = wazeFeedService.findActive();
        assertEquals(0, announcements.size());
    }
}