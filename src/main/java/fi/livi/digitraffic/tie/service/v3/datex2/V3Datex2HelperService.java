package fi.livi.digitraffic.tie.service.v3.datex2;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.EstimatedDuration;
import fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncement;
import fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2HelperService;

@Service
public class V3Datex2HelperService extends V2Datex2HelperService {
    private static final Logger log = LoggerFactory.getLogger(V3Datex2HelperService.class);

    @Autowired
    public V3Datex2HelperService(final ObjectMapper objectMapper) {
        super(objectMapper);
    }

    public TrafficAnnouncementFeature convertToFeatureJsonObjectV3(final String imsJson) {
        // Ims JSON String can be in 0.2.4 or in 0.2.5 format. Convert 0.2.4 to in 0.2.5 format.
        final String imsJsonV3 = convertImsJsonToV0_2_5(imsJson);

        try {
            final TrafficAnnouncementFeature feature = featureJsonReaderV3.readValue(imsJsonV3);
            if ( isInvalidGeojson(feature) ) {
                log.error("Failed to convert valid GeoJSON Feature from json: {}", imsJson);
                return null;
            }
            final List<ConstraintViolation<EstimatedDuration>> violations = getDurationViolations(feature);

            if (!violations.isEmpty()) {
                violations.forEach(v -> log.error("Invalid EstimatedDuration.{} value {} ", v.getPropertyPath(), v.getInvalidValue()));
                log.error("Failed to convert valid Duration from json: {}", imsJson);
                return null;
            }
            return feature;
        } catch (JsonProcessingException e) {
            log.error("method=convertToFeatureJsonObject error while converting JSON to TrafficAnnouncementFeature jsonValue=\n" + imsJson, e);
            throw new RuntimeException(e);
        }
    }

    private String convertImsJsonToV0_2_5(final String imsJson) {
        try {
            final JsonNode root = genericJsonReader.readTree(imsJson);
            final JsonNode announcements = readAnnouncementsFromTheImsJson(root);
            // if announcements is found json might be V0_2_4 and features must be converted to C0_2_5 format
            if (announcements == null) {
                return imsJson;
            }

            for (JsonNode announcement : announcements) {
                final ArrayNode features = (ArrayNode) announcement.get("features");

                if (features != null && features.size() > 0) {
                    final ArrayNode newFeaturesArrayNode = objectMapper.createArrayNode();
                    for (final JsonNode f : features) {
                        if (!f.isTextual()) {
                            // -> is already V0_2_5
                            return imsJson;
                        }
                        final ObjectNode feature = objectMapper.createObjectNode();
                        feature.put("name", f.textValue());
                        newFeaturesArrayNode.add(feature);
                    }
                    ((ObjectNode) announcement).set("features", newFeaturesArrayNode);
                }
            }
            return objectMapper.writer().writeValueAsString(root);
        } catch (Exception e) {
            return imsJson;
        }
    }

    private List<ConstraintViolation<EstimatedDuration>> getDurationViolations(final TrafficAnnouncementFeature feature) {
        return feature.getProperties().announcements.stream().map(this::getDurationViolations).flatMap(Collection::stream).collect(Collectors.toList());
    }

    private Set<ConstraintViolation<EstimatedDuration>> getDurationViolations(final TrafficAnnouncement a) {
        if (a.timeAndDuration != null && a.timeAndDuration.estimatedDuration != null) {
            return validator.validate(a.timeAndDuration.estimatedDuration);
        }
        return Collections.emptySet();
    }

    private static boolean isInvalidGeojson(final TrafficAnnouncementFeature feature) {
        return feature.getProperties() == null;
    }
}
