package fi.livi.digitraffic.tie.converter.waze;

import static fi.livi.digitraffic.tie.converter.waze.WazeAnnouncementDurationConverter.getAnnouncementDuration;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.D2LogicalModel;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.*;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.dto.wazefeed.WazeDatex2FeatureDto;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedIncidentDto;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedLocationDto;
import fi.livi.digitraffic.tie.helper.WazeDatex2MessageConverter;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.metadata.geojson.MultiLineString;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.service.waze.WazeReverseGeocodingService;

import static fi.livi.digitraffic.tie.converter.waze.WazeAnnouncementDurationConverter.createDuration;

@ConditionalOnWebApplication
@Component
public class WazeDatex2JsonConverter {
    private static final Logger logger = LoggerFactory.getLogger(WazeDatex2JsonConverter.class);

    private final WazeDatex2MessageConverter wazeDatex2MessageConverter;

    private final WazeReverseGeocodingService wazeReverseGeocodingService;

    private final WazeTypeConverter wazeTypeConverter;

    @Autowired
    public WazeDatex2JsonConverter(final WazeDatex2MessageConverter wazeDatex2MessageConverter,
                                   final WazeReverseGeocodingService wazeReverseGeocodingService,
                                   final WazeTypeConverter wazeTypeConverter) {
        this.wazeDatex2MessageConverter = wazeDatex2MessageConverter;
        this.wazeReverseGeocodingService = wazeReverseGeocodingService;
        this.wazeTypeConverter = wazeTypeConverter;
    }

    public Stream<WazeFeedIncidentDto> convertToWazeFeedAnnouncementDto(final WazeDatex2FeatureDto wazeDatex2FeatureDto) {
        final TrafficAnnouncementFeature feature = wazeDatex2FeatureDto.feature;
        if(feature.getGeometry() == null) return Stream.empty();

        final Optional<String> maybeStreet = wazeReverseGeocodingService.getStreetName(feature.getGeometry());
        if(maybeStreet.isEmpty()) return Stream.empty();

        final Optional<WazeFeedIncidentDto.WazeType> maybeType = wazeTypeConverter.convertToWazeType(wazeDatex2FeatureDto);
        if(maybeType.isEmpty()) return Stream.empty();

        final TrafficAnnouncement announcement = feature.getProperties().announcements.getFirst();
        final WazeFeedLocationDto.Direction direction = convertDirection(announcement.locationDetails.roadAddressLocation.direction, feature.getGeometry())
                .orElse(null);

        final Optional<String> maybePolyline = formatPolyline(feature.getGeometry(), direction);
        if(maybePolyline.isEmpty()) return Stream.empty();

        final String situationId = feature.getProperties().situationId;

        // special handling for roadworks
        if(feature.getProperties().getSituationType().equals(SituationType.ROAD_WORK)) {
            final var incidentsFromPhases = convertRoadClosed(announcement, wazeDatex2FeatureDto.d2LogicalModel, situationId, maybePolyline.get(), maybeStreet.get(), direction);

            // if no phases that qualify are found, then use the old conversion
            if(!incidentsFromPhases.isEmpty()) {
                return incidentsFromPhases.stream();
            }
        }

        final Pair<String, String> duration = getAnnouncementDuration(announcement, maybeType);
        final String description = wazeDatex2MessageConverter.export(situationId, wazeDatex2FeatureDto.d2LogicalModel);

        return Stream.of(new WazeFeedIncidentDto(situationId, maybeStreet.get(), description, direction, maybePolyline.get(), maybeType.get(), duration.getLeft(), duration.getRight()));
    }

    private Collection<WazeFeedIncidentDto> convertRoadClosed(final TrafficAnnouncement announcement,
                                                              final D2LogicalModel d2logicalModel,
                                                              final String situationId,
                                                              final String polyline,
                                                              final String street,
                                                              final WazeFeedLocationDto.Direction direction) {
        return announcement.roadWorkPhases.stream()
                .filter(WazeTypeConverter::hasRoadClosedRestriction)
                .filter(WazeAnnouncementDurationConverter::isActive)
                .map(p -> {
                    final var duration = createDuration(p.timeAndDuration.startTime, p.timeAndDuration.endTime);
                    final var id = String.format("%s.%s", situationId, p.id);
                    final var description = wazeDatex2MessageConverter.exportPhase(situationId, p.id, d2logicalModel);

                    return new WazeFeedIncidentDto(id, street, description, direction, polyline, WazeFeedIncidentDto.WazeType.ROAD_CLOSED_CONSTRUCTION, duration.getLeft(), duration.getRight());
                })
                .toList();
    }

    private Optional<WazeFeedLocationDto.Direction> convertDirection(final RoadAddressLocation.Direction direction, final Geometry<?> geometry) {
        if (direction == null || geometry == null || geometry instanceof Point) {
            return Optional.empty();
        }

        return switch (direction) {
            case BOTH -> Optional.of(WazeFeedLocationDto.Direction.BOTH_DIRECTIONS);
            default -> Optional.of(WazeFeedLocationDto.Direction.ONE_DIRECTION);
        };
    }

    public static Optional<String> formatPolyline(final Geometry<?> geometry, final WazeFeedLocationDto.Direction direction) {
        if (geometry instanceof final Point point) {
            return Optional.of(formatPolylineFromPoint(point));
        } else if (geometry instanceof final MultiLineString multiLineString) {
            return Optional.of(formatPolylineFromMultiLineString(multiLineString, direction));
        }

        logger.warn("method=formatPolyline Unknown geometry type {}", geometry.getClass().getSimpleName());
        return Optional.empty();
    }

    private static String formatPolylineFromMultiLineString(final MultiLineString multiLineString, final WazeFeedLocationDto.Direction direction) {
        return multiLineString.getCoordinates().stream()
            .flatMap(Collection::stream)
            .map(WazeDatex2JsonConverter::formatPolylineFromPoint)
            .collect(Collectors.joining(" "));
    }

    private static String formatPolylineFromPoint(final Point point) {
        return formatPolylineFromPoint(point.getLongitude(), point.getLatitude());
    }

    private static String formatPolylineFromPoint(final List<Double> point) {
        return formatPolylineFromPoint(point.getFirst(), point.get(1));
    }

    private static String formatPolylineFromPoint(final Double longitude, final Double latitude) {
        return String.format(Locale.US,"%f %f", latitude, longitude);
    }
}
