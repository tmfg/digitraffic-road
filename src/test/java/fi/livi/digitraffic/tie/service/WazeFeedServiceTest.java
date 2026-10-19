package fi.livi.digitraffic.tie.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.converter.waze.WazeDatex2Converter;
import fi.livi.digitraffic.tie.converter.waze.WazeDatex2JsonConverter;
import fi.livi.digitraffic.tie.dao.data.DataDatex2SituationRepository;
import fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagement;
import fi.livi.digitraffic.tie.datex2.v3_7.Situation;
import fi.livi.digitraffic.tie.datex2.v3_7.SituationPublication;
import fi.livi.digitraffic.tie.datex2.v3_7.Validity;
import fi.livi.digitraffic.tie.datex2.v3_7.ValidityStatusEnum;
import fi.livi.digitraffic.tie.datex2.v3_7._ValidityStatusEnum;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.Contact;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.SituationType;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementProperties;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeDatex2FeatureDto;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedAnnouncementDto;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedIncidentDto;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedLocationDto;
import fi.livi.digitraffic.tie.metadata.geojson.LineString;
import fi.livi.digitraffic.tie.metadata.geojson.MultiLineString;
import fi.livi.digitraffic.tie.metadata.geojson.MultiPoint;
import fi.livi.digitraffic.tie.metadata.geojson.MultiPolygon;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.Polygon;
import fi.livi.digitraffic.tie.model.data.SituationMessage;
import fi.livi.digitraffic.tie.service.waze.WazeFeedService;

@Import({ JacksonAutoConfiguration.class })
public class WazeFeedServiceTest extends AbstractRestWebTest {

    @Autowired
    private WazeFeedService wazeFeedService;

    @MockitoBean
    private DataDatex2SituationRepository dataDatex2SituationRepository;

    @MockitoBean
    private WazeDatex2Converter wazeDatex2Converter;

    @MockitoBean
    private WazeDatex2JsonConverter wazeDatex2JsonConverter;

    @Test
    public void findActiveReturnsConvertedIncidentsForPairedMessages() {
        final List<SituationMessage> messages = List.of(
            situationMessage("GUID-1", "TRAFFIC_ANNOUNCEMENT", "SIMPPELI", "0.2.18", "{}"),
            situationMessage("GUID-1", "TRAFFIC_ANNOUNCEMENT", "DATEX2", "3.7", "<xml />")
        );
        final WazeDatex2FeatureDto featureDto = new WazeDatex2FeatureDto(
            "GUID-1",
            SituationType.TRAFFIC_ANNOUNCEMENT,
            "<xml />",
            activeSituationPublication(),
            new TrafficAnnouncementFeature(new Point(25.0, 60.0), new TrafficAnnouncementProperties(
                "GUID-1", 1, SituationType.TRAFFIC_ANNOUNCEMENT, null, null, null, new ArrayList<>(), new Contact())));
        final WazeFeedIncidentDto incident = new WazeFeedIncidentDto(
            "GUID-1",
            "Street",
            "Description",
            WazeFeedLocationDto.Direction.ONE_DIRECTION,
            "60.0 25.0",
            WazeFeedIncidentDto.WazeType.HAZARD_NONE,
            null,
            null);

        when(dataDatex2SituationRepository.findActiveSituationMessages(anyList(), any(), any(), any())).thenReturn(messages);
        when(wazeDatex2Converter.convertToWazeDatex2FeatureDto(any())).thenReturn(Optional.of(featureDto));
        when(wazeDatex2JsonConverter.convertToWazeFeedAnnouncementDto(featureDto)).thenReturn(Stream.of(incident));

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();

        assertEquals(1, announcement.incidents.size());
        assertEquals("GUID-1", announcement.incidents.getFirst().id);
    }

    @Test
    public void findActiveSkipsSituationsMissingPairedMessages() {
        when(dataDatex2SituationRepository.findActiveSituationMessages(anyList(), any(), any(), any())).thenReturn(List.of(
            situationMessage("GUID-1", "TRAFFIC_ANNOUNCEMENT", "SIMPPELI", "0.2.18", "{}")
        ));

        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();

        assertTrue(announcement.incidents.isEmpty());
        verify(wazeDatex2Converter, never()).convertToWazeDatex2FeatureDto(any());
    }

    @Test
    public void noIncidents() {
        when(dataDatex2SituationRepository.findActiveSituationMessages(anyList(), any(), any(), any())).thenReturn(List.of());
        final WazeFeedAnnouncementDto announcement = wazeFeedService.findActive();
        assertEquals(0, announcement.incidents.size());
    }

    @Test
    public void incorrectGeometryType() {
        final List<Double> point = List.of(1.0, 2.2);
        final List<List<Double>> coords = List.of(point);
        final List<List<List<Double>>> poly = List.of(coords);

        assertTrue(WazeDatex2JsonConverter.formatPolyline(new MultiPoint(coords)).isEmpty());
        assertTrue(WazeDatex2JsonConverter.formatPolyline(new LineString(coords)).isEmpty());
        assertTrue(WazeDatex2JsonConverter.formatPolyline(MultiPolygon.ofSinglePolygon(poly)).isEmpty());
        assertTrue(WazeDatex2JsonConverter.formatPolyline(new Polygon(poly)).isEmpty());
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

        final Optional<String> maybePolyline1 = WazeDatex2JsonConverter.formatPolyline(geometry1);
        final Optional<String> maybePolyline2 = WazeDatex2JsonConverter.formatPolyline(geometry2);

        assertEquals("61.569262 25.180874 61.569394 25.180826", maybePolyline1.orElse(null));
        assertEquals("61.575153 25.182835 61.575386 25.183062 61.575587 25.183280 61.569262 25.180874 61.569394 25.180826", maybePolyline2.orElse(null));
    }

    private SituationPublication activeSituationPublication() {
        final Validity validity = new Validity();
        validity.setValidityStatus(new _ValidityStatusEnum(ValidityStatusEnum.ACTIVE, null));

        final RoadOrCarriagewayOrLaneManagement record = new RoadOrCarriagewayOrLaneManagement();
        record.setValidity(validity);

        final Situation situation = new Situation();
        situation.getSituationRecords().add(record);

        final SituationPublication publication = new SituationPublication();
        publication.getSituations().add(situation);
        return publication;
    }

    private SituationMessage situationMessage(final String situationId,
                                              final String situationType,
                                              final String messageType,
                                              final String messageVersion,
                                              final String message) {
        return new SituationMessage() {
            @Override
            public String getSituationId() {
                return situationId;
            }

            @Override
            public String getSituationType() {
                return situationType;
            }

            @Override
            public String getMessageType() {
                return messageType;
            }

            @Override
            public String getMessageVersion() {
                return messageVersion;
            }

            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public java.time.Instant getModifiedAt() {
                return java.time.Instant.now();
            }
        };
    }
}
