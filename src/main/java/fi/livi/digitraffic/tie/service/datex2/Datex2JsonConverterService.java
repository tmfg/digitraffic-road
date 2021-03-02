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
import com.fasterxml.jackson.databind.node.ObjectNode;

import fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.EstimatedDuration;
import fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncement;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationType;
import fi.livi.digitraffic.tie.model.v1.datex2.TrafficAnnouncementType;
import fi.livi.digitraffic.tie.service.v3.datex2.V3RegionGeometryDataService;

@Service
public class Datex2JsonConverterService {
    private static final Logger log = LoggerFactory.getLogger(Datex2JsonConverterService.class);

    protected final ObjectReader featureJsonReaderV2;
    protected final ObjectReader featureJsonReaderV3;

    protected final Validator validator;
    protected final ObjectReader genericJsonReader;
    private V3RegionGeometryDataService v3RegionGeometryDataService;

    protected ObjectMapper objectMapper;

    @Autowired
    public Datex2JsonConverterService(final ObjectMapper objectMapper,
                                      final V3RegionGeometryDataService v3RegionGeometryDataService) {
        this.objectMapper = objectMapper;

        featureJsonReaderV2 = objectMapper.readerFor(TrafficAnnouncementFeature.class);
        featureJsonReaderV3 = objectMapper.readerFor(fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeature.class);

        genericJsonReader = objectMapper.reader();
        this.v3RegionGeometryDataService = v3RegionGeometryDataService;

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

    public fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeature convertToFeatureJsonObjectV3(final String imsJson,
                                                                                                                                final SituationType situationType,
                                                                                                                                final TrafficAnnouncementType trafficAnnouncementType,
                                                                                                                                boolean includeAreaGeometry)
        throws JsonProcessingException {
        // Ims JSON String can be in 0.2.4, 0.2.6 or 0.2.8 format. Convert all to 0.2.10 format.
        final String imsJsonV3 = convertImsJsonToV3Compatible(imsJson);

        final fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeature feature =
            featureJsonReaderV3.readValue(imsJsonV3);

        // Older
        if (feature.getProperties().getSituationType() == null) {
            feature.getProperties().setSituationType(situationType);
            feature.getProperties().setTrafficAnnouncementType(trafficAnnouncementType);
        } else if (!feature.getProperties().getSituationType().equals(situationType)) {
            log.error("Datex2 import-time SituationType: {} not equal to type in JSON: {}, sourceJson: {}", situationType, feature.getProperties().getSituationType(), imsJson);
        }
        checkIsInvalidAnnouncementGeojsonV3(feature);
        checkDurationViolationsV3(feature);

        // Fetch or clear area geometries
        final List<TrafficAnnouncement> announcementsWithAreas =
            feature.getProperties().announcements.stream().filter(Datex2JsonConverterService::containsAreaLocation).collect(Collectors.toList());

        if (!announcementsWithAreas.isEmpty()) {
            if (includeAreaGeometry) {
                feature.setGeometry(v3RegionGeometryDataService.getGeoJsonGeometryUnion(feature.getProperties().releaseTime.toInstant(),
                    announcementsWithAreas.stream()
                        .map(withArea ->
                            withArea.locationDetails.areaLocation.areas.stream()
                                .map(a -> a.locationCode).collect(Collectors.toList()))
                        .flatMap(Collection::stream)
                        .toArray(Integer[]::new)));
            } else {
                feature.setGeometry(null);
            }
        }

        return feature;
    }

    private static boolean containsAreaLocation(final TrafficAnnouncement announcement) {
        return
            announcement != null &&
            announcement.locationDetails != null &&
            announcement.locationDetails.areaLocation != null &&
            announcement.locationDetails.areaLocation.areas != null &&
            !announcement.locationDetails.areaLocation.areas.isEmpty();
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

    private String convertImsJsonToV3Compatible(final String imsJson) throws JsonProcessingException {
        final JsonNode root = genericJsonReader.readTree(imsJson);
        final JsonNode announcements = readAnnouncementsFromTheImsJson(root);
        // if announcements is found json might be V0_2_4 and features must be converted to V0_2_6 and V0_2_8 format
        if ( announcements == null || announcements.isEmpty() ) {
            return imsJson;
        }

        for (final JsonNode announcement : announcements) {

            final ArrayNode roadWorkPhases = (ArrayNode) announcement.get("roadWorkPhases");
            if (roadWorkPhases != null && roadWorkPhases.size() > 0) {
                for (final JsonNode roadWorkPhase : roadWorkPhases) {
                    final ArrayNode features = (ArrayNode) roadWorkPhase.get("features");
                /*
                "features" : [ {
                    "name" : "Valaistustyö"
                } ],
                ->
                "worktypes": [
                    {
                        "type": "lighting",
                        "description": "Valaistustyö"
                    }
                ],
                 */
                    if (features != null && features.size() > 0) {
                        final ArrayNode worktypes = objectMapper.createArrayNode();

                        for (final JsonNode f : features) {
                            final ObjectNode worktype = objectMapper.createObjectNode();
                            worktype.put("type", "other");
                            worktype.set("description", f.get("name"));
                            worktypes.add(worktype);
                        }
                        ((ObjectNode) roadWorkPhase).set("worktypes", worktypes);
                    }
                }
            }
            final ArrayNode features = (ArrayNode) announcement.get("features");
            if (features != null && features.size() > 0) {
                // Replace features with new version
                final ArrayNode featuresWithProperties = objectMapper.createArrayNode();
                for (final JsonNode f : features) {
                    // If it's not textual, then it is already right format from V0.2.5 on
                    if (f.isTextual()) {
                        final ObjectNode feature = objectMapper.createObjectNode();
                        feature.put("name", f.textValue());
                        featuresWithProperties.add(feature);
                    }
                }
                if (featuresWithProperties.size() > 0) {
                    ((ObjectNode) announcement).set("features", featuresWithProperties);
                }
            }
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
        final fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncement a) {

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
