package fi.livi.digitraffic.tie.converter.waze;

import fi.livi.digitraffic.tie.AbstractSpringJUnitTest;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.*;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeDatex2FeatureDto;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedIncidentDto;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedIncidentDto.WazeType.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WazeTypeConverterTest extends AbstractSpringJUnitTest {
    @Autowired
    private WazeTypeConverter wazeTypeConverter;

    private Optional<WazeFeedIncidentDto.WazeType> testConvert(final SituationType situationType,
                                                               final TrafficAnnouncementType trafficAnnouncementType) {
        return testConvert(situationType, trafficAnnouncementType, (p) -> {});
    }

    private Optional<WazeFeedIncidentDto.WazeType> testConvert(final SituationType situationType,
                                                               final TrafficAnnouncementType trafficAnnouncementType,
                                                               final Consumer<WazeDatex2FeatureDto> consumer) {
        final Geometry g = new Point(1.0, 2.2);
        final TrafficAnnouncementProperties properties = new TrafficAnnouncementProperties("id", 1, situationType,
            trafficAnnouncementType,
            ZonedDateTime.now(), ZonedDateTime.now(), new ArrayList<>(), new Contact());

        final TrafficAnnouncementFeature feature = new TrafficAnnouncementFeature(g, properties);
        final Datex2 d2 = new Datex2();
        final WazeDatex2FeatureDto dto = new WazeDatex2FeatureDto(d2, null, feature);

        d2.setMessage("");

        consumer.accept(dto);

        return wazeTypeConverter.convertToWazeType(dto);
    }

    private void assertType(final WazeFeedIncidentDto.WazeType expected, final Optional<WazeFeedIncidentDto.WazeType> value) {
        if(expected != null) {
            assertTrue(value.isPresent());
            assertEquals(expected, value.get());
        } else {
            assertFalse(value.isPresent());
        }
    }

    private static final Consumer<WazeDatex2FeatureDto> ADD_ROAD_CLOSED = (p) -> {
        p.datex2.setMessage("<roadOrCarriagewayOrLaneManagementType>roadClosed");
    };

    @Test
    public void noTrafficAnnouncementType() {
        final Optional<WazeFeedIncidentDto.WazeType> type = testConvert(SituationType.TRAFFIC_ANNOUNCEMENT, null);

        assertType(null, type);
    }

    @Test
    public void trafficAnnouncement_roadClosed() {
        final Optional<WazeFeedIncidentDto.WazeType> type = testConvert(SituationType.TRAFFIC_ANNOUNCEMENT, TrafficAnnouncementType.GENERAL, ADD_ROAD_CLOSED);

        assertType(ROAD_CLOSED_HAZARD, type);
    }

    @Test
    public void roadwork_roadClosed() {
        final Optional<WazeFeedIncidentDto.WazeType> type = testConvert(SituationType.ROAD_WORK, TrafficAnnouncementType.GENERAL, ADD_ROAD_CLOSED);

        assertType(ROAD_CLOSED_CONSTRUCTION, type);
    }

    @Test
    public void jam_standStillTraffic() {
        final Optional<WazeFeedIncidentDto.WazeType> type = testConvert(SituationType.ROAD_WORK, TrafficAnnouncementType.GENERAL, (dto -> {
           dto.datex2.setMessage("<abnormalTrafficType>stationaryTraffic</abnormalTrafficType>");
        }));

        assertType(JAM_STAND_STILL_TRAFFIC, type);
    }

    @Test
    public void jam_heavyTraffic() {
        final Optional<WazeFeedIncidentDto.WazeType> type = testConvert(SituationType.ROAD_WORK, TrafficAnnouncementType.GENERAL, (dto -> {
            dto.datex2.setMessage("<abnormalTrafficType>heavyTraffic</abnormalTrafficType>");
        }));

        assertType(JAM_HEAVY_TRAFFIC, type);
    }

    @Test
    public void jam_moderateTraffic() {
        final Optional<WazeFeedIncidentDto.WazeType> type = testConvert(SituationType.ROAD_WORK, TrafficAnnouncementType.GENERAL, (dto -> {
            dto.datex2.setMessage("<abnormalTrafficType>queuingTraffic</abnormalTrafficType>");
        }));

        assertType(JAM_MODERATE_TRAFFIC, type);
    }

    @ParameterizedTest
    @MethodSource("convertTrafficAnnouncementProvider")
    public void convertTrafficAnnouncement(final TrafficAnnouncementType taType, final WazeFeedIncidentDto.WazeType expected) {
        final Optional<WazeFeedIncidentDto.WazeType> type = testConvert(SituationType.TRAFFIC_ANNOUNCEMENT, taType);

        assertType(expected, type);
    }

    private static Stream<Arguments> convertTrafficAnnouncementProvider() {
        return Stream.of(
            Arguments.of(TrafficAnnouncementType.ENDED, null),
            Arguments.of(TrafficAnnouncementType.GENERAL, HAZARD_NONE),
            Arguments.of(TrafficAnnouncementType.PRELIMINARY_ACCIDENT_REPORT, ACCIDENT_NONE),
            Arguments.of(TrafficAnnouncementType.ACCIDENT_REPORT, ACCIDENT_NONE)
        );
    }
}
