package fi.livi.digitraffic.tie.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.converter.WazeDatex2JsonConverter;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedAnnouncementDto;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedIncidentsDto;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.RoadAddressLocation;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedLocationDto;
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

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentsDto> incidents = announcement.incidents;

        assertEquals(1, incidents.size());
    }

    @Test
    public void announcementIsProperlyFormatted() {
        final String situationId = "GUID12345";
        final ZonedDateTime startTime = ZonedDateTime.parse("2021-07-28T13:09:47.470Z");
        final Integer street = 24;
        final String municipality = "Espoo";
        final String roadName = "Puolarmetsänkatu";
        final List<String> featureList =
            List.of("Onnettomuus", "Onnettomuuspaikan pelastus- ja raivaustyöt käynnissä", "Tie on suljettu liikenteeltä");

        final WazeFeedServiceTestHelper.AnnouncementAddress announcementAddress = new WazeFeedServiceTestHelper.AnnouncementAddress(municipality, roadName, street);
        final WazeFeedServiceTestHelper.AnnouncementParams params =
            new WazeFeedServiceTestHelper.AnnouncementParams(situationId, announcementAddress, startTime,
                TrafficAnnouncementType.ACCIDENT_REPORT, RoadAddressLocation.Direction.BOTH, featureList);

        wazeFeedServiceTestHelper.insertAccident(params);

        final String description = featureList.stream().map(x -> x + ".").collect(Collectors.joining(" "));

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentsDto> incidents = announcement.incidents;
        assertEquals(1, incidents.size());

        final WazeFeedIncidentsDto incident = incidents.get(0);

        assertEquals(situationId, incident.id);
        assertEquals(String.format("%s - %s, %s", street, roadName, municipality), incident.location.street);
        assertEquals(WazeFeedIncidentsDto.Type.ACCIDENT, incident.type);
        assertEquals(description.substring(0, 37) + "...", incident.description);
        assertTrue(incident.description.length() <= 40);
        assertEquals("FINTRAFFIC", incident.reference);
    }

    @Test
    public void pointInAnnouncement() {
        final Point point = new Point(25.182835, 61.575153);

        wazeFeedServiceTestHelper.insertAccident("GUID1234", RoadAddressLocation.Direction.BOTH, 130, point);

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentsDto> incidents = announcement.incidents;
        assertEquals(1, incidents.size());

        final WazeFeedIncidentsDto incident = incidents.get(0);
        assertEquals("25.182835 61.575153", incident.location.polyline);
        assertNull(incident.location.direction);
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
        wazeFeedServiceTestHelper.insertAccident("GUID1234", RoadAddressLocation.Direction.POS, 130);
        wazeFeedServiceTestHelper.insertAccident("GUID1235", RoadAddressLocation.Direction.NEG, 129);
        wazeFeedServiceTestHelper.insertAccident("GUID1236", RoadAddressLocation.Direction.UNKNOWN, 131);

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentsDto> incidents = announcement.incidents;
        assertEquals(3, incidents.size());

        incidents.forEach(x -> assertEquals(WazeFeedLocationDto.Direction.ONE_DIRECTION, x.location.direction));
    }

    @Test
    public void unsupportedGeometryTypesAreFilteredFromResults() {
        final List<List<Double>> coords = List.of(List.of(25.180874, 61.569262), List.of(25.180826, 61.569394));
        final MultiLineString geometry = new MultiLineString();
        geometry.addLineString(coords);

        // using supported MultiLineString
        wazeFeedServiceTestHelper.insertAccident("GUID1234", RoadAddressLocation.Direction.BOTH, 130, geometry);

        // unsupported MultiPolygon
        wazeFeedServiceTestHelper.insertAccident("GUID1235", RoadAddressLocation.Direction.BOTH, 130, new MultiPolygon(List.of(coords)));

        // expect the multipolygon version to be filtered out
        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentsDto> incidents = announcement.incidents;
        assertEquals(1, incidents.size());
    }

    @Test
    public void bothDirectionsCoordinatesAreReturnedAsProperlyFormattedPolyline() {
        final MultiLineString geometry = new MultiLineString();
        geometry.addLineString(List.of(List.of(25.180874, 61.569262), List.of(25.180826, 61.569394)));

        wazeFeedServiceTestHelper.insertAccident("GUID1234", RoadAddressLocation.Direction.BOTH, 130, geometry);

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentsDto> incidents = announcement.incidents;
        assertEquals(incidents.size(), 1);

        final WazeFeedIncidentsDto incident = incidents.get(0);

        assertEquals("25.180874 61.569262 25.180826 61.569394 25.180826 61.569394 25.180874 61.569262", incident.location.polyline);
        assertEquals(WazeFeedLocationDto.Direction.BOTH_DIRECTIONS, incident.location.direction);
    }

    @Test
    public void noIncidents() {
        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentsDto> incidents = announcement.incidents;

        assertEquals(incidents.size(), 0);
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

        final Optional<String> maybePolyline1 = WazeDatex2JsonConverter.formatPolyline(geometry1, WazeFeedLocationDto.Direction.ONE_DIRECTION);
        final Optional<String> maybePolyline2 = WazeDatex2JsonConverter.formatPolyline(geometry2, WazeFeedLocationDto.Direction.ONE_DIRECTION);

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

        final Optional<String> maybePolyline = WazeDatex2JsonConverter.formatPolyline(geometry, WazeFeedLocationDto.Direction.BOTH_DIRECTIONS);
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

        final Optional<String> maybePolyline = WazeDatex2JsonConverter.formatPolyline(geometry, WazeFeedLocationDto.Direction.ONE_DIRECTION);
        assertTrue(maybePolyline.isPresent());

        final String polyline = maybePolyline.get();
        assertEquals("25.180874 61.569262 25.180826 61.569394 25.180754 61.569586 25.180681 61.569794 25.180601 61.570065 25.212664 61.586387 25.212674 61.586377", polyline);
    }

    @Test
    public void checkForNullValues() {
        final List<String> featureList = List.of("Onnettomuus");
        final ZonedDateTime startTime = ZonedDateTime.parse("2021-07-28T13:09:47.470Z");

        // Create multiple invalid announcements
        final List<WazeFeedServiceTestHelper.AnnouncementParams> announcementParams = List.of(
            // Filtered out
            new WazeFeedServiceTestHelper.AnnouncementParams(
                wazeFeedServiceTestHelper.nextSituationRecord(),
                new WazeFeedServiceTestHelper.AnnouncementAddress("municipality", "roadName", 1),
                startTime,
                TrafficAnnouncementType.GENERAL,
                RoadAddressLocation.Direction.BOTH,
                null
            ),
            // Filtered out
            new WazeFeedServiceTestHelper.AnnouncementParams(
                wazeFeedServiceTestHelper.nextSituationRecord(),
                new WazeFeedServiceTestHelper.AnnouncementAddress("municipality", "roadName", 1),
                startTime,
                null,
                RoadAddressLocation.Direction.BOTH,
                featureList
            ),
            // Filtered out
            new WazeFeedServiceTestHelper.AnnouncementParams(
                wazeFeedServiceTestHelper.nextSituationRecord(),
                new WazeFeedServiceTestHelper.AnnouncementAddress("municipality", "roadName", 1),
                startTime,
                TrafficAnnouncementType.GENERAL,
                RoadAddressLocation.Direction.BOTH,
                featureList,
                null
            ),

            // These are in the end result even if they may have some missing values
            new WazeFeedServiceTestHelper.AnnouncementParams(
                wazeFeedServiceTestHelper.nextSituationRecord(),
                new WazeFeedServiceTestHelper.AnnouncementAddress("municipality", "roadName", 1),
                startTime,
                TrafficAnnouncementType.GENERAL,
                null,
                featureList
            ),
            new WazeFeedServiceTestHelper.AnnouncementParams(
                wazeFeedServiceTestHelper.nextSituationRecord(),
                new WazeFeedServiceTestHelper.AnnouncementAddress(null, "roadName", 1),
                startTime,
                TrafficAnnouncementType.GENERAL,
                RoadAddressLocation.Direction.BOTH,
                featureList
            ),
            new WazeFeedServiceTestHelper.AnnouncementParams(
                wazeFeedServiceTestHelper.nextSituationRecord(),
                new WazeFeedServiceTestHelper.AnnouncementAddress("municipality", null, 1),
                startTime,
                TrafficAnnouncementType.GENERAL,
                RoadAddressLocation.Direction.BOTH,
                featureList
            ),
            new WazeFeedServiceTestHelper.AnnouncementParams(
                wazeFeedServiceTestHelper.nextSituationRecord(),
                new WazeFeedServiceTestHelper.AnnouncementAddress("municipality", "roadName", null),
                startTime,
                TrafficAnnouncementType.GENERAL,
                RoadAddressLocation.Direction.BOTH,
                featureList
            ),
            new WazeFeedServiceTestHelper.AnnouncementParams(
                wazeFeedServiceTestHelper.nextSituationRecord(),
                new WazeFeedServiceTestHelper.AnnouncementAddress("municipality", "roadName", 1),
                null,
                TrafficAnnouncementType.GENERAL,
                RoadAddressLocation.Direction.BOTH,
                featureList
            )
        );

        announcementParams.forEach(wazeFeedServiceTestHelper::insertAccident);

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentsDto> incidents = announcement.incidents;
        assertEquals(5, incidents.size());
    }

    @Test
    public void filterPreliminaryAccidentReports() {
        final WazeFeedServiceTestHelper.AnnouncementParams params = new WazeFeedServiceTestHelper.AnnouncementParams();
        params.situationId = wazeFeedServiceTestHelper.nextSituationRecord();
        params.trafficAnnouncementType = TrafficAnnouncementType.PRELIMINARY_ACCIDENT_REPORT;

        // datex2 database record having preliminary accident report type
        wazeFeedServiceTestHelper.insertAccident(params);

        // datex2 announcement type column having incorrect announcement type, but real preliminary accident report type still in json format
        params.situationId = wazeFeedServiceTestHelper.nextSituationRecord();
        wazeFeedServiceTestHelper.insertAccident(params.situationId, params.situationId, params, TrafficAnnouncementType.GENERAL);

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentsDto> incidents = announcement.incidents;
        assertEquals(0, incidents.size());
    }
}