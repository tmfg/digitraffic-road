package fi.livi.digitraffic.tie.controller.trafficmessage;

import fi.livi.digitraffic.tie.dto.trafficmessage.v2.TrafficAnnouncement;

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

    public TrafficAnnouncementFeature convertToFeature(final String message, final boolean includeAreaGeometry) throws JacksonException {
        final TrafficAnnouncementFeature feature = featureReader.readValue(message);

        if (!includeAreaGeometry && hasAnyAreaGeometries(feature)) {
            feature.setGeometry(null);
        }

        return feature;
    }

    public boolean hasAnyAreaGeometries(final TrafficAnnouncementFeature feature) {
        return feature.getProperties().announcements.stream()
                .anyMatch(TrafficAnnouncement::containsAreaLocation);
    }
}
