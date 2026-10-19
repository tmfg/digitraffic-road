package fi.livi.digitraffic.tie.controller.trafficmessage;

import fi.livi.digitraffic.tie.dto.trafficmessage.v2.TrafficAnnouncement;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectReader;

import fi.livi.digitraffic.tie.dto.trafficmessage.v2.TrafficAnnouncementFeature;

@Component
public class MessageConverter {
    private final ObjectMapper objectMapper;
    private final ObjectReader featureReader;

    private static final Logger log = LoggerFactory.getLogger(MessageConverter.class);

    public MessageConverter(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.featureReader = this.objectMapper.readerFor(TrafficAnnouncementFeature.class);
    }

    /**
     * The stored message does not carry an update timestamp, so it is taken from the database row
     * the message was read from. Without it the documented and required {@code dataUpdatedTime}
     * would always be null.
     */
    public TrafficAnnouncementFeature convertToFeature(final String message, final boolean includeAreaGeometry,
                                                       final Instant modifiedAt) throws JacksonException {
        final TrafficAnnouncementFeature feature = featureReader.readValue(message);

        if (!includeAreaGeometry && hasAnyAreaGeometries(feature)) {
            feature.setGeometry(null);
        }

        if (feature.getProperties() != null) {
            feature.getProperties().setLastModified(modifiedAt);
        }

        return feature;
    }

    public boolean hasAnyAreaGeometries(final TrafficAnnouncementFeature feature) {
        return feature.getProperties().announcements.stream()
                .anyMatch(TrafficAnnouncement::containsAreaLocation);
    }
}
