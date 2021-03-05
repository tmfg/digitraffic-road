package fi.livi.digitraffic.tie.service.datex2;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;

import fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.EstimatedDuration;
import fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncement;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationType;
import fi.livi.digitraffic.tie.model.v1.datex2.TrafficAnnouncementType;

@Service
public class ImsJsonConverterService {
    private static final Logger log = LoggerFactory.getLogger(ImsJsonConverterService.class);

    protected final ObjectReader featureJsonReaderV2;
    protected final ObjectReader featureJsonReaderV3;

    protected final Validator validator;
    protected final ObjectReader genericJsonReader;

    protected ObjectMapper objectMapper;

    @Autowired
    public ImsJsonConverterService(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        featureJsonReaderV2 = objectMapper.readerFor(TrafficAnnouncementFeature.class);
        featureJsonReaderV3 = objectMapper.readerFor(fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeature.class);

        genericJsonReader = objectMapper.reader();

        final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
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

    private void checkDurationViolationsV3(final fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeature feature) {
        final List<ConstraintViolation<fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.EstimatedDuration>> violations =
            getDurationViolationsV3(feature);

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

    private List<ConstraintViolation<fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.EstimatedDuration>> getDurationViolationsV3(
        final fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeature feature) {

        return feature.getProperties().announcements.stream().map(this::getDurationViolationsV3).flatMap(Collection::stream).collect(Collectors.toList());
    }

    private Set<ConstraintViolation<EstimatedDuration>> getDurationViolationsV2(
        final fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.TrafficAnnouncement a) {

        if (a.timeAndDuration != null && a.timeAndDuration.estimatedDuration != null) {
            return validator.validate(a.timeAndDuration.estimatedDuration);
        }
        return Collections.emptySet();
    }

    private Set<ConstraintViolation<fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.EstimatedDuration>> getDurationViolationsV3(
        final TrafficAnnouncement a) {

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

    private static void checkIsInvalidAnnouncementGeojsonV3(final fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeature feature) {
        if (feature.getProperties() == null) {
            throw new IllegalStateException("TrafficAnnouncementFeature with null properties " + ToStringHelper.toStringFull(feature));
        }
    }

    /**
     * If given json is GeoJSON FeatureCollection returns it's features otherwise returns the single feature json.
     * @param imsJson GeoJSON string
     * @return Map of situationId to GeoJSON feature JSON-string, SituationType and TrafficAnnouncementType. Empty if no features is found.
     */
    public Map<String, Triple<String, SituationType, TrafficAnnouncementType>> parseFeatureJsonsFromImsJson(final String imsJson) {

        if (StringUtils.isBlank(imsJson)) {
            return Collections.emptyMap();
        }

        final JsonNode root;
        try {
            root = genericJsonReader.readTree(imsJson);
        } catch (final JsonProcessingException e) {
            log.error(String.format("method=parseFeatureJsonsFromImsJson Failed to read Json tree of imsJson: %s", imsJson), e);
            return Collections.emptyMap();
        }

        if ( isFeatureCollection(root) ) {
            return parseFeatureCollection(root);
        } else if ( isFeature(root) ){
            return parseFeature(root);
        } else {
            log.error("method=parseFeatureJsonsFromImsJson IMS Json doesn't contain valid GeoJson object type [Feature|FeatureCollection]. Json: {}", imsJson);
            return Collections.emptyMap();
        }
    }

    private Map<String, Triple<String, SituationType, TrafficAnnouncementType>> parseFeature(final JsonNode root) {
        final String situationId = getSituationId(root);
        final SituationType situationType = getSituationType(root);
        final TrafficAnnouncementType trafficAnnouncementType = getTrafficAnnouncementType(root, situationType);

        if (StringUtils.isNotBlank(situationId)) {
            return Collections.singletonMap(situationId, Triple.of(root.toPrettyString(), situationType, trafficAnnouncementType));
        }
        return Collections.emptyMap();
    }

    private Map<String, Triple<String, SituationType, TrafficAnnouncementType>> parseFeatureCollection(final JsonNode root) {
        final JsonNode features = root.get("features");
        final Map<String, Triple<String, SituationType, TrafficAnnouncementType>> featureJsons = new HashMap<>();
        for (int i = 0; i < features.size(); i++) {
            final String json = features.get(i).toPrettyString();
            final String situationId = getSituationId(features.get(i));
            final SituationType situationType = getSituationType(features.get(i));
            final TrafficAnnouncementType trafficAnnouncementType = getTrafficAnnouncementType(features.get(i), situationType);
            if (StringUtils.isNotBlank(situationId)) {
                featureJsons.put(situationId, Triple.of(json, situationType, trafficAnnouncementType));
            }
        }
        return featureJsons;
    }

    private String getSituationId(final JsonNode feature) {
        final JsonNode properties = feature.get("properties");
        if (properties == null) {
            log.error("method=getSituationId No properties property for feature json: {}", feature.toPrettyString());
            return null;
        }
        final JsonNode situationId = properties.get("situationId");
        if (situationId == null) {
            log.error("method=getSituationId No situationId property for feature json: {}", feature.toPrettyString());
            return null;
        }
        return situationId.asText();
    }

    private SituationType getSituationType(final JsonNode feature) {
        final JsonNode properties = feature.get("properties");
        if (properties == null) {
            return resolveSituationTypeFromTextWithError(feature);
        }
        final JsonNode situationType = properties.get("situationType");
        if (situationType == null) {
            return resolveSituationTypeFromTextWithError(feature);
        }
        try {
            return SituationType.fromValue(situationType.asText());
        } catch (final Exception e) {
            log.error("method=getSituationType Error while trying to resolve json SituationType", e);
            return resolveSituationTypeFromTextWithError(feature);
        }
    }

    private static SituationType resolveSituationTypeFromTextWithError(final JsonNode featureNode) {
        final SituationType resolvedType = Datex2Helper.resolveSituationTypeFromText(featureNode.toString());
        log.error("method=getSituationType No situationType property for feature json. Resolved type from text {}. Json: {}", resolvedType, featureNode.toPrettyString());
        return resolvedType;
    }

    private TrafficAnnouncementType getTrafficAnnouncementType(final JsonNode feature,
                                                               final SituationType situationType) {
        if (situationType != SituationType.TRAFFIC_ANNOUNCEMENT) {
            return null;
        }
        final JsonNode properties = feature.get("properties");
        if (properties == null) {
            return resolveTrafficAnnouncementTypeTypeFromTextWithError(feature);
        }
        final JsonNode trafficAnnouncementType = properties.get("trafficAnnouncementType");
        if (trafficAnnouncementType == null) {
            return resolveTrafficAnnouncementTypeTypeFromTextWithError(feature);
        }
        try {
            return TrafficAnnouncementType.fromValue(trafficAnnouncementType.asText());
        } catch (Exception e) {
            log.error("method=getTrafficAnnouncementType Error while trying to resolve json TrafficAnnouncementType", e);
            return resolveTrafficAnnouncementTypeTypeFromTextWithError(feature);
        }
    }

    private static TrafficAnnouncementType resolveTrafficAnnouncementTypeTypeFromTextWithError(final JsonNode featureNode) {
        final TrafficAnnouncementType resolvedType = Datex2Helper.resolveTrafficAnnouncementTypeFromText(featureNode.toString());
        log.error("method=getTrafficAnnouncementType No trafficAnnouncementType property for feature json. Resolved type from text {}. Json: {}", resolvedType, featureNode.toPrettyString());
        return resolvedType;
    }

    private boolean isFeatureCollection(final JsonNode root) {
        final JsonNode type = root.get("type");
        return type != null && StringUtils.equals(type.asText(), "FeatureCollection");
    }
    private boolean isFeature(final JsonNode root) {
        final JsonNode type = root.get("type");
        return type != null && StringUtils.equals(type.asText(), "Feature");
    }
}
