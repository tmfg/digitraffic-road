package fi.livi.digitraffic.tie.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static fi.livi.digitraffic.tie.service.WazeFeedServiceTestHelper.readDatex2MessageFromFile;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.converter.WazeDatex2JsonConverter;
import fi.livi.digitraffic.tie.dto.wazefeed.ReverseGeocode;
import fi.livi.digitraffic.tie.dto.wazefeed.ReverseGeocodeResult;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedAnnouncementDto;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedIncidentDto;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.RoadAddressLocation;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedLocationDto;
import fi.livi.digitraffic.tie.helper.WazeDatex2MessageConverter;
import fi.livi.digitraffic.tie.helper.WazeReverseGeocodingApi;
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

    @Autowired
    private WazeDatex2MessageConverter wazeDatex2MessageConverter;

    @Autowired
    private WazeReverseGeocodingService wazeReverseGeocodingService;

    @AfterEach
    public void cleanup() {
        wazeFeedServiceTestHelper.cleanup();
    }

    @Test
    public void getAListOfWazeAnnouncements() {
        wazeFeedServiceTestHelper.insertSituation();

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentDto> incidents = announcement.incidents;

        assertEquals(1, incidents.size());
    }

    @Test
    public void announcementIsProperlyFormatted() {
        final String situationId = "GUID12345";
        final ZonedDateTime startTime = ZonedDateTime.parse("2021-07-28T13:09:47.470Z");
        final Integer streetNumber = 24;
        final String municipality = "Espoo";
        final String roadName = "Puolarmetsänkatu";

        final WazeFeedServiceTestHelper.AnnouncementAddress announcementAddress = new WazeFeedServiceTestHelper.AnnouncementAddress(municipality, roadName, streetNumber);
        final WazeFeedServiceTestHelper.SituationParams params =
            new WazeFeedServiceTestHelper.SituationParams(situationId, announcementAddress, startTime,
                TrafficAnnouncementType.ACCIDENT_REPORT, RoadAddressLocation.Direction.BOTH);

        wazeFeedServiceTestHelper.insertSituation(params, "");

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentDto> incidents = announcement.incidents;
        assertEquals(1, incidents.size());

        final WazeFeedIncidentDto incident = incidents.get(0);

        assertEquals(situationId, incident.id);
        assertEquals(String.format("%s - %s, %s", streetNumber, roadName, municipality), incident.location.street);
        assertEquals(WazeFeedIncidentDto.Type.ACCIDENT, incident.type);
        assertEquals("", incident.description);
        assertTrue(incident.description.length() <= 40);
        assertEquals("FINTRAFFIC", incident.reference);
    }

    @Test
    public void pointInAnnouncement() {
        final Point point = new Point(25.182835, 61.575153);

        wazeFeedServiceTestHelper.insertSituation("GUID1234", RoadAddressLocation.Direction.BOTH, 130, point);

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentDto> incidents = announcement.incidents;
        assertEquals(1, incidents.size());

        final WazeFeedIncidentDto incident = incidents.get(0);
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
        wazeFeedServiceTestHelper.insertSituation("GUID1234", RoadAddressLocation.Direction.POS, 130);
        wazeFeedServiceTestHelper.insertSituation("GUID1235", RoadAddressLocation.Direction.NEG, 129);
        wazeFeedServiceTestHelper.insertSituation("GUID1236", RoadAddressLocation.Direction.UNKNOWN, 131);

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentDto> incidents = announcement.incidents;
        assertEquals(3, incidents.size());

        incidents.forEach(x -> assertEquals(WazeFeedLocationDto.Direction.ONE_DIRECTION, x.location.direction));
    }

    @Test
    public void unsupportedGeometryTypesAreFilteredFromResults() {
        final List<List<Double>> coords = List.of(List.of(25.180874, 61.569262), List.of(25.180826, 61.569394));
        final MultiLineString geometry = new MultiLineString();
        geometry.addLineString(coords);

        // using supported MultiLineString
        wazeFeedServiceTestHelper.insertSituation("GUID1234", RoadAddressLocation.Direction.BOTH, 130, geometry);

        // unsupported MultiPolygon
        wazeFeedServiceTestHelper.insertSituation("GUID1235", RoadAddressLocation.Direction.BOTH, 130, new MultiPolygon(List.of(coords)));

        // expect the multipolygon version to be filtered out
        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentDto> incidents = announcement.incidents;
        assertEquals(1, incidents.size());
    }

    @Test
    public void bothDirectionsCoordinatesAreReturnedAsProperlyFormattedPolyline() {
        final MultiLineString geometry = new MultiLineString();
        geometry.addLineString(List.of(List.of(25.180874, 61.569262), List.of(25.180826, 61.569394)));

        wazeFeedServiceTestHelper.insertSituation("GUID1234", RoadAddressLocation.Direction.BOTH, 130, geometry);

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentDto> incidents = announcement.incidents;
        assertEquals(incidents.size(), 1);

        final WazeFeedIncidentDto incident = incidents.get(0);

        assertEquals("25.180874 61.569262 25.180826 61.569394 25.180826 61.569394 25.180874 61.569262", incident.location.polyline);
        assertEquals(WazeFeedLocationDto.Direction.BOTH_DIRECTIONS, incident.location.direction);
    }

    @Test
    public void noIncidents() {
        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentDto> incidents = announcement.incidents;

        assertEquals(incidents.size(), 0);
    }

    @Test
    public void datex2MessageConversion() throws IOException {
        final String situationId = "GUID10004";
        final String datex2MessageA = readDatex2MessageFromFile("TrafficSituationRoadOrCarriagewayOrLaneManagementAndSpeedManagement.xml");
        final String datex2MessageB = readDatex2MessageFromFile("TrafficSituationEquipmentOrSystemFault.xml");
        final String datex2MessageC = readDatex2MessageFromFile("TrafficSituationVehicleObstructionAndRoadOrCarriagewayOrLaneManagement.xml");
        final String datex2MessageD = readDatex2MessageFromFile("TrafficSituationRoadOrCarriagewayOrLaneManagementWithReroutingManagement.xml");
        final String datex2MessageF = readDatex2MessageFromFile("TrafficSituationIceRoadClosed.xml");
        final String datex2MessageG = readDatex2MessageFromFile("TrafficSituationRoadOrCarriagewayOrLaneManagementAndWeatherCondition.xml");

        // TODO add missing types
        final String datex2MessageH = readDatex2MessageFromFile("TrafficSituationAccidentAndRoadOrCarriagewayOrLaneManagement.xml");

        assertEquals("Lanes deviated. Temporary speed limit of 50 km/h.", wazeDatex2MessageConverter.export(situationId, datex2MessageA));
        assertEquals("Traffic light sets out of service.", wazeDatex2MessageConverter.export(situationId, datex2MessageB));
        assertEquals("Vehicle obstruction: vehicle on fire. Lane closures.", wazeDatex2MessageConverter.export(situationId, datex2MessageC));
        assertEquals("Road closed. Follow diversion signs.", wazeDatex2MessageConverter.export(situationId, datex2MessageD));
        assertEquals("Ice road closed.", wazeDatex2MessageConverter.export(situationId, datex2MessageF));
        assertEquals("Surface water. Lane closures.", wazeDatex2MessageConverter.export(situationId, datex2MessageG));
        assertEquals("Accident involving multiple vehicles. Lane closures. Traffic building up. Unprotected accident area.", wazeDatex2MessageConverter.export(situationId, datex2MessageH));
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
        final ZonedDateTime startTime = ZonedDateTime.parse("2021-07-28T13:09:47.470Z");

        // Create multiple invalid announcements
        final List<WazeFeedServiceTestHelper.SituationParams> situationParams = List.of(
            // Filtered out
            new WazeFeedServiceTestHelper.SituationParams(
                wazeFeedServiceTestHelper.nextSituationRecord(),
                new WazeFeedServiceTestHelper.AnnouncementAddress("municipality", "roadName", 1),
                startTime,
                TrafficAnnouncementType.GENERAL,
                RoadAddressLocation.Direction.BOTH,
                null
            ),
            // Filtered out
            new WazeFeedServiceTestHelper.SituationParams(
                wazeFeedServiceTestHelper.nextSituationRecord(),
                new WazeFeedServiceTestHelper.AnnouncementAddress("municipality", "roadName", 1),
                startTime,
                null,
                RoadAddressLocation.Direction.BOTH
            ),
            // Filtered out
            new WazeFeedServiceTestHelper.SituationParams(
                wazeFeedServiceTestHelper.nextSituationRecord(),
                new WazeFeedServiceTestHelper.AnnouncementAddress("municipality", "roadName", 1),
                startTime,
                TrafficAnnouncementType.GENERAL,
                RoadAddressLocation.Direction.BOTH,
                null
            ),

            // These are in the end result even if they may have some missing values
            new WazeFeedServiceTestHelper.SituationParams(
                wazeFeedServiceTestHelper.nextSituationRecord(),
                new WazeFeedServiceTestHelper.AnnouncementAddress("municipality", "roadName", 1),
                startTime,
                TrafficAnnouncementType.GENERAL,
                null
            ),
            new WazeFeedServiceTestHelper.SituationParams(
                wazeFeedServiceTestHelper.nextSituationRecord(),
                new WazeFeedServiceTestHelper.AnnouncementAddress(null, "roadName", 1),
                startTime,
                TrafficAnnouncementType.GENERAL,
                RoadAddressLocation.Direction.BOTH
            ),
            new WazeFeedServiceTestHelper.SituationParams(
                wazeFeedServiceTestHelper.nextSituationRecord(),
                new WazeFeedServiceTestHelper.AnnouncementAddress("municipality", null, 1),
                startTime,
                TrafficAnnouncementType.GENERAL,
                RoadAddressLocation.Direction.BOTH
            ),
            new WazeFeedServiceTestHelper.SituationParams(
                wazeFeedServiceTestHelper.nextSituationRecord(),
                new WazeFeedServiceTestHelper.AnnouncementAddress("municipality", "roadName", null),
                startTime,
                TrafficAnnouncementType.GENERAL,
                RoadAddressLocation.Direction.BOTH
            ),
            new WazeFeedServiceTestHelper.SituationParams(
                wazeFeedServiceTestHelper.nextSituationRecord(),
                new WazeFeedServiceTestHelper.AnnouncementAddress("municipality", "roadName", 1),
                null,
                TrafficAnnouncementType.GENERAL,
                RoadAddressLocation.Direction.BOTH
            )
        );

        situationParams.forEach(wazeFeedServiceTestHelper::insertSituation);

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentDto> incidents = announcement.incidents;
        assertEquals(5, incidents.size());
    }

    @Test
    public void filterPreliminaryAccidentReports() {
        final WazeFeedServiceTestHelper.SituationParams params = new WazeFeedServiceTestHelper.SituationParams();
        params.situationId = wazeFeedServiceTestHelper.nextSituationRecord();
        params.trafficAnnouncementType = TrafficAnnouncementType.PRELIMINARY_ACCIDENT_REPORT;

        // datex2 database record having preliminary accident report type
        wazeFeedServiceTestHelper.insertSituation(params);

        // datex2 announcement type column having incorrect announcement type, but real preliminary accident report type still in json format
        params.situationId = wazeFeedServiceTestHelper.nextSituationRecord();
        wazeFeedServiceTestHelper.insertSituation(params.situationId, params.situationId, "", params, TrafficAnnouncementType.GENERAL);

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentDto> incidents = announcement.incidents;
        assertEquals(0, incidents.size());
    }

    @Test
    public void shouldConvertReverseGeocodingResponseToJavaObject() {
        final int RADIUS = 50;
        final double DISTANCE = 2.242342;
        final String NAME = "Lautta";

        final String responseTemplate = "{\"lat\":60.542603,\"lon\":21.336319,\"radius\":%s,\"result\":[{\"distance\":%s,\"names\":[\"%s\"]},{\"distance\":20.24164959825527,\"names\":[\"192 - Kivimaantie\"]}]}";
        final String response = String.format(responseTemplate, RADIUS, DISTANCE, NAME);

        final Optional<ReverseGeocode> maybeReverseGeocode = wazeReverseGeocodingService.parseReverseGeocodeJson(response);

        assertNotNull(maybeReverseGeocode);
        assertTrue(maybeReverseGeocode.isPresent());
        final ReverseGeocode reverseGeocode = maybeReverseGeocode.get();

        assertTrue(reverseGeocode.results.size() >= 1, "Results list size is less than one.");
        final ReverseGeocodeResult reverseGeocodeResult = reverseGeocode.results.get(0);

        assertEquals(RADIUS, reverseGeocode.radius);
        assertEquals(DISTANCE, reverseGeocodeResult.distance);

        assertTrue(reverseGeocodeResult.names.size() >= 1, "Result names has less than one item.");
        final String resultName = reverseGeocodeResult.names.get(0);
        assertEquals(NAME, resultName);
    }

    @Test
    public void shouldCacheReverseGeocodingResponses() {
        final WazeReverseGeocodingApi wazeReverseGeocodingApi = mock(WazeReverseGeocodingApi.class);
        final double latitude = 60.1;
        final double longitude = 21.3;
        final String responseTemplate = "{\"lat\":%s,\"lon\":%s,\"radius\":50,\"result\":[{\"distance\":3.1415,\"names\":[\"Lautta\"]},{\"distance\":20.24164959825527,\"names\":[\"192 - Kivimaantie\"]}]}";
        final String response = String.format(responseTemplate, latitude, longitude);
        when(wazeReverseGeocodingApi.fetch(anyDouble(), anyDouble())).thenReturn(Optional.of(response));

        final WazeReverseGeocodingService wazeReverseGeocodingService = wazeFeedServiceTestHelper.constructWazeReverseGeocodingServiceWithMockApi(wazeReverseGeocodingApi);
        final Optional<ReverseGeocode> maybeReverseGeocode1 = wazeReverseGeocodingService.fetch(latitude, longitude);
        final Optional<ReverseGeocode> maybeReverseGeocode2 = wazeReverseGeocodingService.fetch(latitude, longitude);

        assertNotNull(maybeReverseGeocode1);
        assertTrue(maybeReverseGeocode1.isPresent());
        assertNotNull(maybeReverseGeocode2);
        assertTrue(maybeReverseGeocode2.isPresent());

        final ReverseGeocode reverseGeocode1 = maybeReverseGeocode1.get();
        final ReverseGeocode reverseGeocode2 = maybeReverseGeocode2.get();
        assertEquals(reverseGeocode1.latitude, reverseGeocode2.latitude);
        assertEquals(reverseGeocode1.longitude, reverseGeocode2.longitude);

        verify(wazeReverseGeocodingApi, times(1)).fetch(latitude, longitude);
    }

    @Test
    public void shouldFetchReverseGeocoding() {
        final WazeReverseGeocodingApi wazeReverseGeocodingApi = mock(WazeReverseGeocodingApi.class);
        final double latitude = 60.1;
        final double longitude = 21.3;
        final String responseTemplate = "{\"lat\":%s,\"lon\":%s,\"radius\":50,\"result\":[{\"distance\":3.1415,\"names\":[\"Lautta\"]},{\"distance\":20.24164959825527,\"names\":[\"192 - Kivimaantie\"]}]}";
        final String response = String.format(responseTemplate, latitude, longitude);
        when(wazeReverseGeocodingApi.fetch(anyDouble(), anyDouble())).thenReturn(Optional.of(response));

        final WazeReverseGeocodingService wazeReverseGeocodingService = wazeFeedServiceTestHelper.constructWazeReverseGeocodingServiceWithMockApi(wazeReverseGeocodingApi);
        final var maybeReverseGeocode = wazeReverseGeocodingService.fetch(latitude, longitude);

        assertNotNull(maybeReverseGeocode);
        assertTrue(maybeReverseGeocode.isPresent());

        final var reverseGeocode = maybeReverseGeocode.get();
        assertEquals(latitude, reverseGeocode.latitude);
        assertEquals(longitude, reverseGeocode.longitude);
    }

    @Test
    public void shouldHandleIncorrectReverseGeocodingResults() {
        final WazeReverseGeocodingApi wazeReverseGeocodingApi = mock(WazeReverseGeocodingApi.class);
        when(wazeReverseGeocodingApi.fetch(anyDouble(), anyDouble())).thenReturn(Optional.of(""));

        final WazeReverseGeocodingService wazeReverseGeocodingService = wazeFeedServiceTestHelper.constructWazeReverseGeocodingServiceWithMockApi(wazeReverseGeocodingApi);
        final var reverseGeocode = wazeReverseGeocodingService.fetch(60.542603, 21.336319);

        assertNotNull(reverseGeocode);
        assertTrue(reverseGeocode.isEmpty());
    }

}