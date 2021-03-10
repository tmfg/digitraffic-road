package fi.livi.digitraffic.tie.service.datex2;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.EstimatedDuration;
import fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;

@ConditionalOnWebApplication
@Component
public class V2Datex2JsonConverterService {
    private static final Logger log = LoggerFactory.getLogger(V2Datex2JsonConverterService.class);

    protected final ObjectReader featureJsonReaderV2;
    protected final ObjectReader featureJsonReaderV3;

    protected final Validator validator;
    protected final ObjectReader genericJsonReader;

    protected ObjectMapper objectMapper;

    @Autowired
    public V2Datex2JsonConverterService(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        featureJsonReaderV2 = objectMapper.readerFor(TrafficAnnouncementFeature.class);
        featureJsonReaderV3 = objectMapper.readerFor(fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeature.class);

        genericJsonReader = objectMapper.reader();

        final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    public TrafficAnnouncementFeature convertToFeatureJsonObjectV2(final String imsJson,
                                                                   final Datex2MessageType messageType)
        throws JsonProcessingException {
        final String imsJsonV0_2_4 = convertImsJsonToV2Compatible(imsJson);

        final TrafficAnnouncementFeature feature =
            featureJsonReaderV2.readValue(imsJsonV0_2_4);

        checkIsInvalidAnnouncementGeojsonV2(feature);
        checkDurationViolationsV2(feature);

        feature.getProperties().setMessageType(messageType);

        return feature;
    }

    private String convertImsJsonToV2Compatible(final String imsJson) throws JsonProcessingException {
        final JsonNode root = genericJsonReader.readTree(imsJson);
        final ArrayNode announcements = readAnnouncementsFromTheImsJson(root);
        // if announcements is found json might be V0_2_6 or V0_2_8 and features must be converted to V0_2_4 format
        if ( announcements == null || announcements.isEmpty() ) {
            return imsJson;
        }

        for (final JsonNode announcement : announcements) {
            final ArrayNode features = (ArrayNode) announcement.get("features");

            if (features != null && features.size() > 0) {
                final ArrayNode newFeaturesArrayNode = objectMapper.createArrayNode();
                for (final JsonNode f : features) {
                    if (f.isTextual()) {
                        // -> is already V0_2_4 -> return original
                        return imsJson;
                    } else {
                        final JsonNode name = f.get("name");
                        newFeaturesArrayNode.add(name);
                    }
                }
                // replace features with V0_2_4 json
                ((ObjectNode) announcement).set("features", newFeaturesArrayNode);
            }
            // No need to remove extra nodes in V0_2_6 or V0_2_8 schema as they will be skipped
        }
        return objectMapper.writer().writeValueAsString(root);
    }

    protected ArrayNode readAnnouncementsFromTheImsJson(final JsonNode root) {
        final JsonNode properties = root.get("properties");
        if (properties == null) {
            return null;
        }
        return (ArrayNode)properties.get("announcements");
    }

    private void checkDurationViolationsV2(final TrafficAnnouncementFeature feature) {
        final List<ConstraintViolation<EstimatedDuration>> violations =
            getDurationViolationsV2(feature);

        if (!violations.isEmpty()) {
            final String joinedViolations = violations.stream()
                .map(v -> String.format("Invalid EstimatedDuration.%s value %s", v.getPropertyPath(), v.getInvalidValue()))
                .collect(Collectors.joining(","));
            throw new IllegalArgumentException(joinedViolations + " " + ToStringHelper.toStringFull(feature));
        }
    }

    private List<ConstraintViolation<EstimatedDuration>> getDurationViolationsV2(
        final TrafficAnnouncementFeature feature) {

        return feature.getProperties().announcements.stream().map(this::getDurationViolationsV2).flatMap(Collection::stream).collect(Collectors.toList());
    }

    private Set<ConstraintViolation<EstimatedDuration>> getDurationViolationsV2(
        final fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.TrafficAnnouncement a) {

        if (a.timeAndDuration != null && a.timeAndDuration.estimatedDuration != null) {
            return validator.validate(a.timeAndDuration.estimatedDuration);
        }
        return Collections.emptySet();
    }

    private static void checkIsInvalidAnnouncementGeojsonV2(final TrafficAnnouncementFeature feature) {
        if (feature.getProperties() == null) {
            throw new IllegalStateException("TrafficAnnouncementFeature with null properties " + ToStringHelper.toStringFull(feature));
        }
    }
}
