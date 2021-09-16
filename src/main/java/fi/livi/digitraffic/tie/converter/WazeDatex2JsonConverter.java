package fi.livi.digitraffic.tie.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import fi.livi.digitraffic.tie.dto.WazeFeedAnnouncementDto;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.RoadAddressLocation;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncement;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementProperties;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.metadata.geojson.MultiLineString;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationType;
import fi.livi.digitraffic.tie.model.v1.datex2.TrafficAnnouncementType;
import fi.livi.digitraffic.tie.service.datex2.V3Datex2JsonConverter;

@ConditionalOnWebApplication
@Component
public class WazeDatex2JsonConverter {
    private static final Logger logger = LoggerFactory.getLogger(WazeDatex2JsonConverter.class);

    private final V3Datex2JsonConverter v3Datex2JsonConverter;

    @Autowired
    public WazeDatex2JsonConverter(final V3Datex2JsonConverter v3Datex2JsonConverter) {
        this.v3Datex2JsonConverter = v3Datex2JsonConverter;
    }

    public Optional<WazeFeedAnnouncementDto> convertToWazeFeedAnnouncementDto(final Datex2 datex2) {
        final TrafficAnnouncementFeature feature;
        final String jsonMessage = datex2.getJsonMessage();

        try {
            feature = v3Datex2JsonConverter.convertToFeatureJsonObjectV3(
                jsonMessage,
                SituationType.TRAFFIC_ANNOUNCEMENT,
                TrafficAnnouncementType.ACCIDENT_REPORT,
                false
            );
        } catch (final JsonProcessingException e) {
            logger.error("method=convertToWazeFeedAnnouncementDto json string conversion to feature object failed", e);
            logger.info(String.format("DEBUG method=convertToWazeFeedAnnouncementDto json string conversion error in string: %s", jsonMessage));
            return Optional.empty();
        }

        final TrafficAnnouncementProperties properties = feature.getProperties();
        final Optional<WazeFeedAnnouncementDto.Type> maybeType = Optional.ofNullable(properties.getTrafficAnnouncementType()).flatMap(this::convertToWazeType);

        final TrafficAnnouncement announcement = properties.announcements.get(0);
        final Optional<Geometry<?>> maybeGeometry = Optional.ofNullable(feature.getGeometry());

        final String id = properties.situationId;
        final Optional<String> maybeStreet = getRoadAddress(announcement);

        if (maybeStreet.isEmpty()) {
            logger.info("method=getRoadAddress TrafficAnnouncement missing road address in situation {}", properties.situationId);
        }

        final Optional<String> maybeDescription = createDescription(announcement);

        final WazeFeedAnnouncementDto.Direction direction = maybeGeometry.flatMap(geometry ->
            convertDirection(announcement.locationDetails.roadAddressLocation.direction, geometry))
            .orElse(null);

        final Optional<String> maybePolyline = maybeGeometry.flatMap(geometry -> formatPolyline(geometry, direction));

        return maybePolyline.flatMap(polyline ->
            maybeStreet.flatMap(street ->
                maybeDescription.flatMap(description ->
                    maybeType.map(type ->
                        new WazeFeedAnnouncementDto(id, street, description, direction, polyline, type)))));
    }

    private Optional<WazeFeedAnnouncementDto.Type> convertToWazeType(final TrafficAnnouncementType trafficAnnouncementType) {
        if (trafficAnnouncementType == null) {
            return Optional.empty();
        }

        switch (trafficAnnouncementType) {
        case ACCIDENT_REPORT:
            return Optional.of(WazeFeedAnnouncementDto.Type.ACCIDENT);
        case GENERAL:
            return Optional.of(WazeFeedAnnouncementDto.Type.HAZARD);
        default:
            return Optional.empty();
        }
    }

    private Optional<String> getRoadAddress(final TrafficAnnouncement announcement) {
        Optional<String> address;

        try {
            address = Optional.of(announcement.locationDetails.roadAddressLocation.primaryPoint.roadAddress.road.toString());
        } catch (NullPointerException e) {
            address = Optional.empty();
        }

        return address.map(s -> String.format("Road %s", s));
    }

    private Optional<WazeFeedAnnouncementDto.Direction> convertDirection(final RoadAddressLocation.Direction direction, Geometry<?> geometry) {
        if (geometry == null || geometry instanceof Point) {
            return Optional.empty();
        }

        switch (direction) {
        case BOTH:
            return Optional.of(WazeFeedAnnouncementDto.Direction.BOTH_DIRECTIONS);
        case UNKNOWN:
        case POS:
        case NEG:
        default:
            return Optional.of(WazeFeedAnnouncementDto.Direction.ONE_DIRECTION);
        }
    }

    private Optional<String> createDescription(final TrafficAnnouncement announcement) {
        final String description = announcement.features.stream()
            .map(x -> String.format("%s.", x.name))
            .collect(Collectors.joining(" "));

        return Optional.of(description)
            .filter(s -> !s.isEmpty())
            .map(s -> s.length() > 40 ? s.substring(0, 37) + "..." : s);
    }

    public static Optional<String> formatPolyline(final Geometry<?> geometry, final WazeFeedAnnouncementDto.Direction direction) {
        if (geometry instanceof Point) {
            final Point point = (Point) geometry;
            return Optional.of(formatPolylineFromPoint(point));
        } else if (geometry instanceof MultiLineString) {
            final MultiLineString multiLineString = (MultiLineString) geometry;
            return Optional.of(formatPolylineFromMultiLineString(multiLineString, direction));
        }

        logger.warn(String.format("method=formatPolyline Unknown geometry type %s", geometry.getClass().getSimpleName()));
        return Optional.empty();
    }

    private static String formatPolylineFromMultiLineString(final MultiLineString multiLineString, final WazeFeedAnnouncementDto.Direction direction) {
        final List<List<Double>> path = multiLineString.getCoordinates().stream().flatMap(Collection::stream).collect(Collectors.toList());

        if (direction == WazeFeedAnnouncementDto.Direction.BOTH_DIRECTIONS) {
            final List<List<Double>> copy = new ArrayList<>(path);
            Collections.reverse(copy);
            path.addAll(copy);
        }

        return path.stream()
            .flatMap(Collection::stream)
            .map(Object::toString)
            .collect(Collectors.joining(" "));
    }

    private static String formatPolylineFromPoint(final Point point) {
        return String.format("%f %f", point.getLongitude(), point.getLatitude());
    }

}