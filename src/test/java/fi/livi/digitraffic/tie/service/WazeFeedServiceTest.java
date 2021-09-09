package fi.livi.digitraffic.tie.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

    private static final Optional<WazeFeedAnnouncementDto.Direction> BOTH_DIRECTIONS = Optional.of(WazeFeedAnnouncementDto.Direction.BOTH_DIRECTIONS);
    private static final Optional<WazeFeedAnnouncementDto.Direction> ONE_DIRECTION = Optional.of(WazeFeedAnnouncementDto.Direction.ONE_DIRECTION);

    @Test
    public void getAListOfWazeAnnouncements() {
        wazeFeedServiceTestHelper.insertAccident();

        final List<WazeFeedAnnouncementDto> allActive = wazeFeedService.findActive();

        assertEquals(1, allActive.size());
    }

    @Test
    public void accidentAnnouncementIsProperlyFormatted() {
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

        final Map<String, String> hm =
            WazeFeedServiceTestHelper.createIncidentMap(additionalInformation, comment, descriptionLine, estimatedDurationInformal, startTime,
                email, phone, sender, situationId, street);

        final String jsonMessage = wazeFeedServiceTestHelper.createJsonMessage(hm, RoadAddressLocation.Direction.BOTH, featureList);

        wazeFeedServiceTestHelper.insertAccident(situationId, situationRecordId, jsonMessage);

        final ZonedDateTime datetime = ZonedDateTime.parse(startTime);
        final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        final String dateTimeString = datetime.format(dateFormatter) + " klo " + datetime.format(timeFormatter);

        final String description =
            descriptionLine + "\n" +
            "\n" +
            featureList.stream().map(x -> x + ".").collect(Collectors.joining("\n")) +
            "\n\n" +
            "Lisätieto: " + comment + "\n" +
            "\n" +
            "Ajankohta: " + dateTimeString + ". Arvioitu kesto: " + estimatedDurationInformal + ".\n" +
            "\n" +
            additionalInformation + "\n" +
            "\n" +
            sender + "\n" +
            "Puh: " + phone + "\n" +
            "Sähköposti: " + email;

        final List<WazeFeedAnnouncementDto> allActive = wazeFeedService.findActive();
        assertEquals(1, allActive.size());

        final WazeFeedAnnouncementDto announcement = allActive.get(0);

        assertEquals(situationId, announcement.id);
        assertEquals(String.format("Road %s", street), announcement.street);
        assertEquals(WazeFeedAnnouncementDto.Type.ACCIDENT, announcement.type);
        assertEquals(description.substring(0, 40), announcement.description);
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
        assertEquals(announcement.direction, Optional.empty());
    }

    @Test
    public void incorrectGeometryType() {
        final List<Double> point = List.of(1.0, 2.2);
        final List<List<Double>> coords = List.of(point);
        final List<List<List<Double>>> poly = List.of(coords);

        assertTrue(WazeDatex2JsonConverter.formatPolyline(new MultiPoint(coords), Optional.empty()).isEmpty());
        assertTrue(WazeDatex2JsonConverter.formatPolyline(new LineString(coords), Optional.empty()).isEmpty());
        assertTrue(WazeDatex2JsonConverter.formatPolyline(new MultiPolygon(poly), Optional.empty()).isEmpty());
        assertTrue(WazeDatex2JsonConverter.formatPolyline(new Polygon(poly), Optional.empty()).isEmpty());
    }

    @Test
    public void onewayDirectionInAccidents() {
        wazeFeedServiceTestHelper.insertAccident("GUID1234", "GUID12345", RoadAddressLocation.Direction.POS, "130", "Liikennevirasto");
        wazeFeedServiceTestHelper.insertAccident("GUID1235", "GUID12346", RoadAddressLocation.Direction.NEG, "129", "Liikennevirasto");
        wazeFeedServiceTestHelper.insertAccident("GUID1236", "GUID12347", RoadAddressLocation.Direction.UNKNOWN, "131", "Liikennevirasto");

        final List<WazeFeedAnnouncementDto> announcements = wazeFeedService.findActive();
        assertEquals(3, announcements.size());

        announcements.forEach((x) -> assertEquals(WazeFeedAnnouncementDto.Direction.ONE_DIRECTION, x.direction.orElseThrow()));
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
        assertEquals(WazeFeedAnnouncementDto.Direction.BOTH_DIRECTIONS, announcement.direction.orElseThrow());
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

        final Optional<String> maybePolyline1 = WazeDatex2JsonConverter.formatPolyline(geometry1, ONE_DIRECTION);
        final Optional<String> maybePolyline2 = WazeDatex2JsonConverter.formatPolyline(geometry2, ONE_DIRECTION);

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

        final Optional<String> maybePolyline = WazeDatex2JsonConverter.formatPolyline(geometry, BOTH_DIRECTIONS);
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

        final Optional<String> maybePolyline = WazeDatex2JsonConverter.formatPolyline(geometry, ONE_DIRECTION);
        assertTrue(maybePolyline.isPresent());

        final String polyline = maybePolyline.get();
        assertEquals("25.180874 61.569262 25.180826 61.569394 25.180754 61.569586 25.180681 61.569794 25.180601 61.570065 25.212664 61.586387 25.212674 61.586377", polyline);
    }
}