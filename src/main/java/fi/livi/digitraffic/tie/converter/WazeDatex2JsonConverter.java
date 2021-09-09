package fi.livi.digitraffic.tie.converter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.Contact;
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

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

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

        final TrafficAnnouncement announcement = properties.announcements.get(0);
        final Geometry<?> geometry = feature.getGeometry();
        final Contact contact = properties.contact;


        final String id = properties.situationId;
        final String street = String.format("Road %s", announcement.locationDetails.roadAddressLocation.primaryPoint.roadAddress.road.toString());

        final String description = createDescription(announcement, contact);

        final Optional<WazeFeedAnnouncementDto.Direction> direction = convertDirection(announcement.locationDetails.roadAddressLocation.direction, geometry);

        final Optional<String> maybePolyline = formatPolyline(geometry, direction);

        return maybePolyline.map(x -> new WazeFeedAnnouncementDto(id, street, description, direction, x));
    }

    private Optional<WazeFeedAnnouncementDto.Direction> convertDirection(final RoadAddressLocation.Direction direction, Geometry<?> geometry) {
        final WazeFeedAnnouncementDto.Direction result;

        if (geometry instanceof Point) {
            return Optional.empty();
        }

        switch (direction) {
        case BOTH:
            result = WazeFeedAnnouncementDto.Direction.BOTH_DIRECTIONS;
            break;
        case UNKNOWN:
        case POS:
        case NEG:
        default:
            result = WazeFeedAnnouncementDto.Direction.ONE_DIRECTION;
            break;
        }

        return Optional.of(result);
    }

    private String createDescription(final TrafficAnnouncement announcement, final Contact contact) {
        final String detailedDescription = announcement.features.stream()
            .map(x -> String.format("%s.", x.name))
            .collect(Collectors.joining("\n"));

        final ZonedDateTime dateTime = announcement.timeAndDuration.startTime;

        return String.format("%s\n\n%s\n\nLisätieto: %s\n\nAjankohta: %s klo %s. Arvioitu kesto: %s.\n\n%s\n\n%s\nPuh: %s\nSähköposti: %s",
            announcement.location.description,
            detailedDescription,
            announcement.comment,
            dateTime.format(dateFormatter),
            dateTime.format(timeFormatter),
            announcement.timeAndDuration.estimatedDuration.informal,
            announcement.additionalInformation,
            announcement.sender,
            contact.phone,
            contact.email
        ).substring(0, 40);
    }

    public static Optional<String> formatPolyline(final Geometry<?> geometry, final Optional<WazeFeedAnnouncementDto.Direction> direction) {
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

    private static String formatPolylineFromPoint(final Point point) {
        return String.format("%f %f", point.getLongitude(), point.getLatitude());
    }

    private static String formatPolylineFromMultiLineString(final MultiLineString multiLineString, final Optional<WazeFeedAnnouncementDto.Direction> direction) {
        final List<List<Double>> path = multiLineString.getCoordinates().stream().flatMap(Collection::stream).collect(Collectors.toList());

        final boolean bothWays = direction.map(x -> x == WazeFeedAnnouncementDto.Direction.BOTH_DIRECTIONS).orElse(false);
        if (bothWays) {
            final List<List<Double>> copy = new ArrayList<>(path);
            Collections.reverse(copy);
            path.addAll(copy);
        }

        return path.stream()
            .flatMap(Collection::stream)
            .map(Object::toString)
            .collect(Collectors.joining(" "));
    }

}