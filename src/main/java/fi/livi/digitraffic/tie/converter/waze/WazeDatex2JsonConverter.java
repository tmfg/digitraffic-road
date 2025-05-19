package fi.livi.digitraffic.tie.converter.waze;

import static fi.livi.digitraffic.tie.converter.waze.WazeAnnouncementDurationConverter.getAnnouncementDuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.dto.trafficmessage.v1.RoadAddressLocation;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncement;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementProperties;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeDatex2FeatureDto;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedIncidentDto;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedLocationDto;
import fi.livi.digitraffic.tie.helper.WazeDatex2MessageConverter;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.metadata.geojson.MultiLineString;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.service.waze.WazeReverseGeocodingService;

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

    public Optional<WazeFeedIncidentDto> convertToWazeFeedAnnouncementDto(final WazeDatex2FeatureDto wazeDatex2FeatureDto) {
        final TrafficAnnouncementFeature feature = wazeDatex2FeatureDto.feature;

        final TrafficAnnouncementProperties properties = feature.getProperties();
        final String situationId = properties.situationId;
        final Optional<WazeFeedIncidentDto.WazeType> maybeType = wazeTypeConverter.convertToWazeType(wazeDatex2FeatureDto);

        final TrafficAnnouncement announcement = properties.announcements.getFirst();
        final Optional<Geometry<?>> maybeGeometry = Optional.ofNullable(feature.getGeometry());

        final String description = wazeDatex2MessageConverter.export(situationId, wazeDatex2FeatureDto.d2LogicalModel);

        final WazeFeedLocationDto.Direction direction = maybeGeometry.flatMap(geometry ->
            convertDirection(announcement.locationDetails.roadAddressLocation.direction, geometry))
            .orElse(null);

        final Optional<String> maybePolyline = maybeGeometry.flatMap(geometry -> formatPolyline(geometry, direction));
        final Optional<String> maybeStreet = maybeGeometry.flatMap(wazeReverseGeocodingService::getStreetName);
        final Pair<String, String> duration = getAnnouncementDuration(announcement, maybeType);

        return maybePolyline.flatMap(polyline ->
            maybeStreet.flatMap(street ->
                maybeType.map(type ->
                    new WazeFeedIncidentDto(situationId, street, description, direction, polyline, type, duration.getLeft(), duration.getRight()))));
    }

    private Optional<WazeFeedLocationDto.Direction> convertDirection(final RoadAddressLocation.Direction direction, final Geometry<?> geometry) {
        if (direction == null || geometry == null || geometry instanceof Point) {
            return Optional.empty();
        }

        switch (direction) {
        case BOTH:
            return Optional.of(WazeFeedLocationDto.Direction.BOTH_DIRECTIONS);
        case UNKNOWN:
        case POS:
        case NEG:
        default:
            return Optional.of(WazeFeedLocationDto.Direction.ONE_DIRECTION);
        }
    }

    public static Optional<String> formatPolyline(final Geometry<?> geometry, final WazeFeedLocationDto.Direction direction) {
        if (geometry instanceof final Point point) {
            return Optional.of(formatPolylineFromPoint(point));
        } else if (geometry instanceof final MultiLineString multiLineString) {
            return Optional.of(formatPolylineFromMultiLineString(multiLineString, direction));
        }

        logger.warn(String.format("method=formatPolyline Unknown geometry type %s", geometry.getClass().getSimpleName()));
        return Optional.empty();
    }

    private static String formatPolylineFromMultiLineString(final MultiLineString multiLineString, final WazeFeedLocationDto.Direction direction) {
        final List<List<Double>> path = multiLineString.getCoordinates().stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        if (direction == WazeFeedLocationDto.Direction.BOTH_DIRECTIONS) {
            final List<List<Double>> copy = new ArrayList<>(path);
            Collections.reverse(copy);
            path.addAll(copy);
        }

        return path.stream()
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
