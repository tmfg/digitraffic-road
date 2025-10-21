package fi.livi.digitraffic.tie.controller.trafficmessage;

import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature;

@Component
public class MessageConverter {
    private final ObjectMapper objectMapper;
    private final ObjectReader featureReader;

    private static final Logger log = LoggerFactory.getLogger(MessageConverter.class);

    public MessageConverter(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy();

        // this is needed to handle Instant
        this.objectMapper.registerModule(new JavaTimeModule());
        this.featureReader = this.objectMapper.readerFor(TrafficAnnouncementFeature.class);
    }

    public String removeAreaGeometrySafe(final String message) {
        try {
            return removeAreaGeometry(message);
        } catch (final JsonProcessingException e) {
            log.error("Error removing geometry", e);

            return message;
        }
    }

    public String removeAreaGeometry(final String message) throws JsonProcessingException {
        final TrafficAnnouncementFeature feature = featureReader.readValue(message);

        if(hasAnyAreaGeometries(feature)) {
            feature.setGeometry(null);
        }

        return this.objectMapper.writeValueAsString(feature);
    }

    public boolean hasAnyAreaGeometries(final TrafficAnnouncementFeature feature) {
        return feature.getProperties().announcements.stream()
                .anyMatch(TrafficAnnouncement::containsAreaLocation);
    }
}
