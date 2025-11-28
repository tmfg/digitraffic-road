package fi.livi.digitraffic.tie.service;

import static fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementType.ACCIDENT_REPORT;
import static fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementType.GENERAL;
import static fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementType.PRELIMINARY_ACCIDENT_REPORT;
import static fi.livi.digitraffic.tie.service.WazeFeedServiceTestHelper.readDatex2MessageFromFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.converter.waze.WazeDatex2JsonConverter;
import fi.livi.digitraffic.tie.dao.trafficmessage.datex2.Datex2Repository;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.RoadAddressLocation;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedAnnouncementDto;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedIncidentDto;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedLocationDto;
import fi.livi.digitraffic.tie.helper.WazeDatex2MessageConverter;
import fi.livi.digitraffic.tie.helper.WazeReverseGeocodingApi;
import fi.livi.digitraffic.tie.metadata.geojson.LineString;
import fi.livi.digitraffic.tie.metadata.geojson.MultiLineString;
import fi.livi.digitraffic.tie.metadata.geojson.MultiPoint;
import fi.livi.digitraffic.tie.metadata.geojson.MultiPolygon;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.Polygon;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.SituationType;
import fi.livi.digitraffic.tie.service.waze.WazeFeedService;

@Import({ JacksonAutoConfiguration.class })
public class WazeFeedServiceTest extends AbstractRestWebTest {

    @Autowired
    private WazeFeedService wazeFeedService;

    @Autowired
    private WazeFeedServiceTestHelper wazeFeedServiceTestHelper;

    @Autowired
    private WazeDatex2MessageConverter wazeDatex2MessageConverter;

    @MockitoBean
    private WazeReverseGeocodingApi wazeReverseGeocodingApi;

    @MockitoSpyBean
    private Datex2Repository datex2Repository;

    public static final String EXAMPLE_WAZE_REVERSE_GEOCODING_RESPONSE = "{\"lat\":60.1,\"lon\":21.3,\"radius\":50,\"result\":[{\"distance\":3.1415,\"names\":[\"Lautta\"]},{\"distance\":20.24164959825527,\"names\":[\"192 - Kivimaantie\"]}]}";

    @BeforeEach
    public void setupMock() {
        when(this.wazeReverseGeocodingApi.fetch(anyDouble(), anyDouble())).thenReturn(Optional.of(EXAMPLE_WAZE_REVERSE_GEOCODING_RESPONSE));
    }

    @AfterEach
    public void cleanup() {
        wazeFeedServiceTestHelper.cleanup();
    }


    private void assertWazeType(final WazeFeedIncidentDto incident, final WazeFeedIncidentDto.WazeType expected) {
        assertEquals(expected.type.name(), incident.type);
        assertEquals(expected.getSubtype(), incident.subtype);


    }
    @Test
    public void getAListOfWazeAnnouncements() throws IOException {
        wazeFeedServiceTestHelper.insertSituation();

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentDto> incidents = announcement.incidents;

        assertEquals(1, incidents.size());
        assertWazeType(incidents.getFirst(), WazeFeedIncidentDto.WazeType.ACCIDENT_NONE);
    }

    private void assertIncident(final WazeFeedIncidentDto incident, final WazeFeedIncidentDto.WazeType expectedType, final String expectedStartTime, final String expectedEndTime) {
        assertWazeType(incident, expectedType);

        assertEquals(expectedStartTime, incident.starttime);
        assertEquals(expectedEndTime, incident.endtime);
    }

    @Test
    public void flood() throws IOException {
        setupDatex2("Flood");

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        assertEquals(1, announcement.incidents.size());

        assertWazeType(announcement.incidents.getFirst(), WazeFeedIncidentDto.WazeType.ROAD_CLOSED_HAZARD);
    }

    private void setupDatex2(final String filename) throws IOException {
        final Datex2 d2 = new Datex2(SituationType.ROAD_WORK, null);
        d2.setMessage(readDatex2MessageFromFile(filename + ".xml"));
        d2.setJsonMessage(readDatex2MessageFromFile(filename + ".json"));

        when(datex2Repository.findAllActiveBySituationTypeWithJson(anyInt(), any(String[].class)))
                .thenReturn(List.of(d2));

    }

    @Test
    public void laneManagement() throws IOException {
        setupDatex2("Roadwork_lane_management");

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        assertEquals(1, announcement.incidents.size());

        assertIncident(announcement.incidents.getFirst(), WazeFeedIncidentDto.WazeType.HAZARD_ON_ROAD_LANE_CLOSED, "2022-10-09T21:00:00+00:00", "2025-12-18T21:59:59+00:00");
    }

    @Test
    public void roadwork() throws IOException {
        setupDatex2("Roadwork");

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        assertEquals(2, announcement.incidents.size());

        // check that times are from the roadworkphase, not from the announcement!
        final WazeFeedIncidentDto incident = announcement.incidents.getFirst();
        assertWazeType(incident, WazeFeedIncidentDto.WazeType.ROAD_CLOSED_CONSTRUCTION);

        assertEquals("2024-09-04T18:00:00+00:00", incident.starttime);
        assertEquals("2026-09-05T03:00:00+00:00", incident.endtime);

        final WazeFeedIncidentDto secondIncident = announcement.incidents.getLast();
        assertWazeType(secondIncident, WazeFeedIncidentDto.WazeType.ROAD_CLOSED_CONSTRUCTION);

        assertEquals("2024-08-18T18:00:00+00:00", secondIncident.starttime);
        assertEquals("2026-08-19T03:00:00+00:00", secondIncident.endtime);
    }

    @Test
    public void roadwork2() throws IOException {
        setupDatex2("Roadwork2");

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        assertEquals(1, announcement.incidents.size());

        // check that times are from the roadworkphase, not from the announcement!
        final WazeFeedIncidentDto incident = announcement.incidents.getFirst();
        assertWazeType(incident, WazeFeedIncidentDto.WazeType.ROAD_CLOSED_CONSTRUCTION);

        assertEquals("2025-05-14T21:00:00+00:00", incident.starttime);
        assertEquals("2025-12-31T21:59:59+00:00", incident.endtime);
    }

    @Test
    public void roadworkLaneClosed() throws IOException {
        setupDatex2("Roadwork_lane_closed");

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        assertEquals(1, announcement.incidents.size());

        final WazeFeedIncidentDto incident = announcement.incidents.getFirst();
        assertWazeType(incident, WazeFeedIncidentDto.WazeType.HAZARD_ON_ROAD_LANE_CLOSED);
    }

    @Test
    public void roadworkLaneClosed2() throws IOException {
        setupDatex2("Roadwork_lane_closed_2");

        // currently we do not make an incident from lane closed, this might change in the future
        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        assertEquals(0, announcement.incidents.size());
    }

    @Test
    public void announcementIsProperlyFormatted() throws IOException {
        final String situationId = "GUID12345";
        final Instant startTime = Instant.parse("2021-07-28T13:09:47.470Z");

        final WazeFeedServiceTestHelper.SituationParams params =
            new WazeFeedServiceTestHelper.SituationParams(situationId, startTime,
                    ACCIDENT_REPORT, RoadAddressLocation.Direction.BOTH);

        wazeFeedServiceTestHelper.insertSituation(params, readDatex2MessageFromFile("TrafficSituationAbnormalTraffic.xml"));

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentDto> incidents = announcement.incidents;
        assertEquals(1, incidents.size());

        final WazeFeedIncidentDto incident = incidents.getFirst();

        assertEquals(situationId, incident.id);
        assertWazeType(incident, WazeFeedIncidentDto.WazeType.HAZARD_ON_ROAD_LANE_CLOSED);
        assertEquals("Accident. Lane closures. Queuing traffic.", incident.description);
        assertEquals("FINTRAFFIC", WazeFeedIncidentDto.reference);
    }

    @Test
    public void pointInAnnouncement() throws IOException {
        final Point point = new Point(25.182835, 61.575153);

        wazeFeedServiceTestHelper.insertSituation("GUID1234", RoadAddressLocation.Direction.BOTH, point);

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentDto> incidents = announcement.incidents;
        assertEquals(1, incidents.size());

        final WazeFeedIncidentDto incident = incidents.getFirst();
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
    public void onewayDirectionInAccidents() throws IOException {
        wazeFeedServiceTestHelper.insertSituation("GUID1234", RoadAddressLocation.Direction.POS);
        wazeFeedServiceTestHelper.insertSituation("GUID1235", RoadAddressLocation.Direction.NEG);
        wazeFeedServiceTestHelper.insertSituation("GUID1236", RoadAddressLocation.Direction.UNKNOWN);

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentDto> incidents = announcement.incidents;
        assertEquals(3, incidents.size());

        incidents.forEach(x -> assertEquals(WazeFeedLocationDto.Direction.ONE_DIRECTION, x.location.direction));
    }

    @Test
    public void unsupportedGeometryTypesAreFilteredFromResults() throws IOException {
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
    public void noIncidents() {
        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentDto> incidents = announcement.incidents;

        assertEquals(0, incidents.size());
    }

    @Test
    public void datex2MessageConversion() throws IOException {
        final String situationId = "GUID10004";

        final long hour = 60 * 60;

        // Valid time range
        final Instant overallStartTime = Instant.now().minusSeconds(hour);
        final Instant overallEndTime = Instant.now().plusSeconds(hour);

        final String datex2Message_01 = readDatex2MessageFromFile("TrafficSituationRoadOrCarriagewayOrLaneManagementAndSpeedManagement.xml");
        final String datex2Message_02 = readDatex2MessageFromFile("TrafficSituationEquipmentOrSystemFault.xml");
        final String datex2Message_03 = readDatex2MessageFromFile("TrafficSituationVehicleObstructionAndRoadOrCarriagewayOrLaneManagement.xml");
        final String datex2Message_04 = readDatex2MessageFromFile("TrafficSituationRoadOrCarriagewayOrLaneManagementWithReroutingManagement.xml")
                .replaceAll("OVERALL_START_TIME", overallStartTime.toString())
                .replaceAll("OVERALL_END_TIME", overallEndTime.toString());
        final String datex2Message_05 = readDatex2MessageFromFile("TrafficSituationIceRoadClosed.xml");
        final String datex2Message_06 = readDatex2MessageFromFile("TrafficSituationRoadOrCarriagewayOrLaneManagementAndWeatherCondition.xml");
        final String datex2Message_07 = readDatex2MessageFromFile("TrafficSituationAccidentAndRoadOrCarriagewayOrLaneManagement.xml")
                .replaceAll("OVERALL_START_TIME", overallStartTime.toString())
                .replaceAll("OVERALL_END_TIME", overallEndTime.toString());
        final String datex2Message_08 = readDatex2MessageFromFile("TrafficSituationTransitInformation.xml");
        final String datex2Message_09 = readDatex2MessageFromFile("TrafficSituationGeneralAccidentOtherAndUnprotectedAccidentArea.xml")
                .replaceAll("OVERALL_START_TIME", overallStartTime.toString())
                .replaceAll("OVERALL_END_TIME", overallEndTime.toString());
        final String datex2Message_10 = readDatex2MessageFromFile("TrafficSituationGeneralNetworkManagement.xml")
                .replaceAll("OVERALL_START_TIME", overallStartTime.toString())
                .replaceAll("OVERALL_END_TIME", overallEndTime.toString());
        final String datex2Message_11 = readDatex2MessageFromFile("TrafficSituationPublicEvent.xml");
        final String datex2Message_12 = readDatex2MessageFromFile("TrafficSituationAbnormalTraffic.xml");
        final String datex2Message_13 = readDatex2MessageFromFile("TrafficSituationInfrastructureDamageObstruction.xml");
        final String datex2Message_14 = readDatex2MessageFromFile("TrafficSituationAbnormalTrafficExtension.xml");
        final String datex2Message_15 = readDatex2MessageFromFile("TrafficSituationNonWeatherRelatedRoadcondition.xml");
        final String datex2Message_16 = readDatex2MessageFromFile("TrafficSituationPoorEnvironmentalConditions.xml");
        final String datex2Message_17 = readDatex2MessageFromFile("TrafficSituationDisturbanceActivity.xml");

        assertEquals("Lanes deviated. Temporary speed limit of 50 km/h.", wazeDatex2MessageConverter.export(situationId, datex2Message_01));
        assertEquals("Traffic light sets out of service.", wazeDatex2MessageConverter.export(situationId, datex2Message_02));
        assertEquals("Vehicle on fire. Lane closures.", wazeDatex2MessageConverter.export(situationId, datex2Message_03));
        assertEquals("Road closed. Follow diversion signs.", wazeDatex2MessageConverter.export(situationId, datex2Message_04));
        assertEquals("Ice road closed.", wazeDatex2MessageConverter.export(situationId, datex2Message_05));
        assertEquals("Surface water. Lane closures.", wazeDatex2MessageConverter.export(situationId, datex2Message_06));
        assertEquals("Accident involving multiple vehicles. Lane closures. Traffic building up. Accident. Unprotected accident area.", wazeDatex2MessageConverter.export(situationId, datex2Message_07));
        assertEquals("Underground metro: load capacity changed.", wazeDatex2MessageConverter.export(situationId, datex2Message_08));
        assertEquals("Accident. Unprotected accident area.", wazeDatex2MessageConverter.export(situationId, datex2Message_09));
        assertEquals("Accident. Road closed. Traffic being manually directed. Unprotected accident area.", wazeDatex2MessageConverter.export(situationId, datex2Message_10));
        assertEquals("Public event: fair. Traffic building up.", wazeDatex2MessageConverter.export(situationId, datex2Message_11));
        assertEquals("Accident. Lane closures. Queuing traffic.", wazeDatex2MessageConverter.export(situationId, datex2Message_12));
        assertEquals("Fallen power cables. Traffic building up.", wazeDatex2MessageConverter.export(situationId, datex2Message_13));
        assertEquals("Accident. Lane closures. Traffic may build up.", wazeDatex2MessageConverter.export(situationId, datex2Message_14));
        assertEquals("Oil on road. Lane closures.", wazeDatex2MessageConverter.export(situationId, datex2Message_15));
        assertEquals("Grassfire. Smoke hazard.", wazeDatex2MessageConverter.export(situationId, datex2Message_16));
        assertEquals("Road closed. Demonstration.", wazeDatex2MessageConverter.export(situationId, datex2Message_17));
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
    public void filterPreliminaryAccidentReports() throws IOException {
        final WazeFeedServiceTestHelper.SituationParams params = new WazeFeedServiceTestHelper.SituationParams();
        params.situationId = wazeFeedServiceTestHelper.nextSituationRecord();
        params.trafficAnnouncementType = PRELIMINARY_ACCIDENT_REPORT;

        // datex2 database record having preliminary accident report type
        wazeFeedServiceTestHelper.insertSituation(params, readDatex2MessageFromFile("TrafficSituationEquipmentOrSystemFault.xml"));

        // datex2 announcement type column having incorrect announcement type, but real preliminary accident report type still in json format
        params.situationId = wazeFeedServiceTestHelper.nextSituationRecord();
        wazeFeedServiceTestHelper.insertSituation(params.situationId, params.situationId, "", params, GENERAL);

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        final List<WazeFeedIncidentDto> incidents = announcement.incidents;
        assertEquals(0, incidents.size());
    }
}
