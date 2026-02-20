package fi.livi.digitraffic.tie.controller.trafficmessage;

import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectReader;

import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature;

@Component
public class MessageConverter {
    private final ObjectMapper objectMapper;
    private final ObjectReader featureReader;

    private static final Logger log = LoggerFactory.getLogger(MessageConverter.class);

    public MessageConverter(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.featureReader = this.objectMapper.readerFor(TrafficAnnouncementFeature.class);
    }

    public String removeAreaGeometrySafe(final String message) {
        try {
            return removeAreaGeometry(message);
        } catch (final JacksonException e) {
            log.error("method=removeAreaGeometrySafe Error removing geometry", e);

            return message;
        }
    }

    public String removeAreaGeometry(final String message) throws JacksonException {
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
