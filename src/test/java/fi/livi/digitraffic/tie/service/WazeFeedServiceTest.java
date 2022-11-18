package fi.livi.digitraffic.tie.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static fi.livi.digitraffic.tie.service.WazeFeedServiceTestHelper.readDatex2MessageFromFile;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.converter.WazeDatex2JsonConverter;
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

    @MockBean
    private WazeReverseGeocodingApi wazeReverseGeocodingApi;

    public static final String EXAMPLE_WAZE_REVERSE_GEOCODING_RESPONSE = "{\"lat\":60.1,\"lon\":21.3,\"radius\":50,\"result\":[{\"distance\":3.1415,\"names\":[\"Lautta\"]},{\"distance\":20.24164959825527,\"names\":[\"192 - Kivimaantie\"]}]}";

    @BeforeEach
    public void setupMock() {
        when(this.wazeReverseGeocodingApi.fetch(anyDouble(), anyDouble())).thenReturn(Optional.of(EXAMPLE_WAZE_REVERSE_GEOCODING_RESPONSE));
    }

    @AfterEach
    public void cleanup() {
        wazeFeedServiceTestHelper.cleanup();
    }

    @Test
    @Disabled
    public void getAListOfWazeAnnouncements() {
        wazeFeedServiceTestHelper.insertSituation();

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentDto> incidents = announcement.incidents;

        assertEquals(1, incidents.size());
    }

    @Test
    @Disabled
    public void announcementIsProperlyFormatted() {
        final String situationId = "GUID12345";
        final ZonedDateTime startTime = ZonedDateTime.parse("2021-07-28T13:09:47.470Z");

        final WazeFeedServiceTestHelper.SituationParams params =
            new WazeFeedServiceTestHelper.SituationParams(situationId, startTime,
                TrafficAnnouncementType.ACCIDENT_REPORT, RoadAddressLocation.Direction.BOTH);

        wazeFeedServiceTestHelper.insertSituation(params, "");

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentDto> incidents = announcement.incidents;
        assertEquals(1, incidents.size());

        final WazeFeedIncidentDto incident = incidents.get(0);

        assertEquals(situationId, incident.id);
        assertEquals(WazeFeedIncidentDto.Type.ACCIDENT, incident.type);
        assertEquals("", incident.description);
        assertTrue(incident.description.length() <= 40);
        assertEquals("FINTRAFFIC", incident.reference);
    }

    @Test
    @Disabled
    public void pointInAnnouncement() {
        final Point point = new Point(25.182835, 61.575153);

        wazeFeedServiceTestHelper.insertSituation("GUID1234", RoadAddressLocation.Direction.BOTH, point);

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentDto> incidents = announcement.incidents;
        assertEquals(1, incidents.size());

        final WazeFeedIncidentDto incident = incidents.get(0);
        assertEquals("61.575153 25.182835", incident.location.polyline);
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
    @Disabled
    public void onewayDirectionInAccidents() {
        wazeFeedServiceTestHelper.insertSituation("GUID1234", RoadAddressLocation.Direction.POS);
        wazeFeedServiceTestHelper.insertSituation("GUID1235", RoadAddressLocation.Direction.NEG);
        wazeFeedServiceTestHelper.insertSituation("GUID1236", RoadAddressLocation.Direction.UNKNOWN);

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentDto> incidents = announcement.incidents;
        assertEquals(3, incidents.size());

        incidents.forEach(x -> assertEquals(WazeFeedLocationDto.Direction.ONE_DIRECTION, x.location.direction));
    }

    @Test
    @Disabled
    public void unsupportedGeometryTypesAreFilteredFromResults() {
        final List<List<Double>> coords = List.of(List.of(25.180874, 61.569262), List.of(25.180826, 61.569394));
        final MultiLineString geometry = new MultiLineString();
        geometry.addLineString(coords);

        // using supported MultiLineString
        wazeFeedServiceTestHelper.insertSituation("GUID1234", RoadAddressLocation.Direction.BOTH, geometry);

        // unsupported MultiPolygon
        wazeFeedServiceTestHelper.insertSituation("GUID1235", RoadAddressLocation.Direction.BOTH, new MultiPolygon(List.of(coords)));

        // expect the multipolygon version to be filtered out
        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentDto> incidents = announcement.incidents;
        assertEquals(1, incidents.size());
    }

    @Test
    @Disabled
    public void bothDirectionsCoordinatesAreReturnedAsProperlyFormattedPolyline() {
        final MultiLineString geometry = new MultiLineString();
        geometry.addLineString(List.of(List.of(25.180874, 61.569262), List.of(25.180826, 61.569394)));

        wazeFeedServiceTestHelper.insertSituation("GUID1234", RoadAddressLocation.Direction.BOTH, geometry);

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentDto> incidents = announcement.incidents;
        assertEquals(incidents.size(), 1);

        final WazeFeedIncidentDto incident = incidents.get(0);

        assertEquals("61.569262 25.180874 61.569394 25.180826 61.569394 25.180826 61.569262 25.180874", incident.location.polyline);
        assertEquals(WazeFeedLocationDto.Direction.BOTH_DIRECTIONS, incident.location.direction);
    }

    @Test
    @Disabled
    public void noIncidents() {
        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentDto> incidents = announcement.incidents;

        assertEquals(incidents.size(), 0);
    }

    @Test
    public void datex2MessageConversion() throws IOException {
        final String situationId = "GUID10004";
        final String datex2Message_01 = readDatex2MessageFromFile("TrafficSituationRoadOrCarriagewayOrLaneManagementAndSpeedManagement.xml");
        final String datex2Message_02 = readDatex2MessageFromFile("TrafficSituationEquipmentOrSystemFault.xml");
        final String datex2Message_03 = readDatex2MessageFromFile("TrafficSituationVehicleObstructionAndRoadOrCarriagewayOrLaneManagement.xml");
        final String datex2Message_04 = readDatex2MessageFromFile("TrafficSituationRoadOrCarriagewayOrLaneManagementWithReroutingManagement.xml");
        final String datex2Message_05 = readDatex2MessageFromFile("TrafficSituationIceRoadClosed.xml");
        final String datex2Message_06 = readDatex2MessageFromFile("TrafficSituationRoadOrCarriagewayOrLaneManagementAndWeatherCondition.xml");
        final String datex2Message_07 = readDatex2MessageFromFile("TrafficSituationAccidentAndRoadOrCarriagewayOrLaneManagement.xml");
        final String datex2Message_08 = readDatex2MessageFromFile("TrafficSituationTransitInformation.xml");
        final String datex2Message_09 = readDatex2MessageFromFile("TrafficSituationGeneralAccidentOtherAndUnprotectedAccidentArea.xml");
        final String datex2Message_10 = readDatex2MessageFromFile("TrafficSituationGeneralNetworkManagement.xml");
        final String datex2Message_11 = readDatex2MessageFromFile("TrafficSituationPublicEvent.xml");

        assertEquals("Lanes deviated. Temporary speed limit of 50 km/h.", wazeDatex2MessageConverter.export(situationId, datex2Message_01));
        assertEquals("Traffic light sets out of service.", wazeDatex2MessageConverter.export(situationId, datex2Message_02));
        assertEquals("Vehicle obstruction: vehicle on fire. Lane closures.", wazeDatex2MessageConverter.export(situationId, datex2Message_03));
        assertEquals("Road closed. Follow diversion signs.", wazeDatex2MessageConverter.export(situationId, datex2Message_04));
        assertEquals("Ice road closed.", wazeDatex2MessageConverter.export(situationId, datex2Message_05));
        assertEquals("Surface water. Lane closures.", wazeDatex2MessageConverter.export(situationId, datex2Message_06));
        assertEquals("Accident involving multiple vehicles. Lane closures. Traffic building up. Accident. Unprotected accident area.", wazeDatex2MessageConverter.export(situationId, datex2Message_07));
        assertEquals("Underground metro: load capacity changed.", wazeDatex2MessageConverter.export(situationId, datex2Message_08));
        assertEquals("Accident. Unprotected accident area.", wazeDatex2MessageConverter.export(situationId, datex2Message_09));
        assertEquals("Accident. Road closed. General network management: traffic being manually directed. Unprotected accident area.", wazeDatex2MessageConverter.export(situationId, datex2Message_10));
        assertEquals("Public event: fair. Traffic building up.", wazeDatex2MessageConverter.export(situationId, datex2Message_11));
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

        assertEquals("61.569262 25.180874 61.569394 25.180826", maybePolyline1.orElse(null));
        assertEquals("61.575153 25.182835 61.575386 25.183062 61.575587 25.183280 61.569262 25.180874 61.569394 25.180826", maybePolyline2.orElse(null));
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
        assertEquals("61.569262 25.180874 61.569394 25.180826 61.569586 25.180754 61.569794 25.180681 61.570065 25.180601 61.586387 25.212664 61.586387 25.212664 61.586387 25.212664 61.586387 25.212664 61.570065 25.180601 61.569794 25.180681 61.569586 25.180754 61.569394 25.180826 61.569262 25.180874", polyline);
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
        assertEquals("61.569262 25.180874 61.569394 25.180826 61.569586 25.180754 61.569794 25.180681 61.570065 25.180601 61.586387 25.212664 61.586377 25.212674", polyline);
    }

    @Test
    @Disabled
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
}