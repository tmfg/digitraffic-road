package fi.livi.digitraffic.tie.service.trafficmessage;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.SituationType;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.TrafficAnnouncementType;
import fi.livi.digitraffic.tie.service.trafficmessage.v1.RegionGeometryDataServiceV1;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * In this java class V2 and V3 refers to old controller and api path hierarchy
 * and _V1 refers to new api paths versions.
 */
@Component
public class TrafficMessageImsJsonConverterV1 {
    private static final Logger log = LoggerFactory.getLogger(TrafficMessageImsJsonConverterV1.class);

    protected final ObjectReader featureJsonReader_V1;

    protected final Validator validator;
    protected final ObjectReader genericJsonReader;
    private final RegionGeometryDataServiceV1 regionGeometryDataServiceV1;

    protected ObjectMapper objectMapper;

    public TrafficMessageImsJsonConverterV1(final ObjectMapper objectMapper,
                                            final RegionGeometryDataServiceV1 regionGeometryDataServiceV1) {
        this.objectMapper = objectMapper;
        this.featureJsonReader_V1 = objectMapper.readerFor(fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature.class);

        this.genericJsonReader = objectMapper.reader();
        this.regionGeometryDataServiceV1 = regionGeometryDataServiceV1;

        try (final ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        }
    }

    public fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature convertToFeatureJsonObject_V1(
        final String imsJson,
        final SituationType situationType,
        final TrafficAnnouncementType trafficAnnouncementType,
        final boolean includeAreaGeometry,
        final Instant lastModified) throws JsonProcessingException {

        // Ims JSON String can be in 0.2.4, 0.2.6 or 0.2.8 format. Convert all to 0.2.10 format.
        final String imsJsonV3 = convertImsJsonTo_V1Compatible(imsJson);

        final fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature feature =
            featureJsonReader_V1.readValue(imsJsonV3);
        feature.getProperties().setLastModified(lastModified);

        // Older
        if (feature.getProperties().getSituationType() == null) {
            feature.getProperties().setSituationType(fi.livi.digitraffic.tie.dto.trafficmessage.v1.SituationType.fromValue(situationType.name()));
            if (trafficAnnouncementType != null) {
                feature.getProperties().setTrafficAnnouncementType(fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementType.fromValue(trafficAnnouncementType.name()));
            }
        } else if (!feature.getProperties().getSituationType().name().equals(situationType.name())) {
            log.error("method=convertToFeatureJsonObjectV3 Datex2 situationId={} SituationType: {} not equal to type in JSON: {}, sourceJson: {}",
                      feature.getProperties().situationId, situationType.name(), feature.getProperties().getSituationType().name(), imsJson);
        }
        checkIsInvalidAnnouncementGeojson_V1(feature);
        checkDurationViolations_V1(feature);

        // Fetch or clear area geometries
        final List<fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncement> announcementsWithAreas =
            feature.getProperties().announcements.stream().filter(a -> a != null && a.containsAreaLocation()).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(announcementsWithAreas)) {
            if (includeAreaGeometry) {
                final Instant effectiveDate = feature.getProperties().releaseTime;
                final Integer[] ids = announcementsWithAreas.stream()
                        .map(withArea ->
                                withArea.locationDetails.areaLocation.areas.stream()
                                        .map(a -> a.locationCode).collect(Collectors.toList()))
                        .flatMap(Collection::stream)
                        .toArray(Integer[]::new);
                try {
                    feature.setGeometry(
                        regionGeometryDataServiceV1.getGeoJsonGeometryUnion(effectiveDate, ids)
                    );
                } catch (final Exception e) {
                    // Fallback as null geometry to not fail the whole query that might contain multiple situations
                    feature.setGeometry(null);
                    log.error("method=getGeoJsonGeometryUnion for situationId={} failed with parameters effectiveDate: {}, ids: {}. Returning null geometry.", feature.getProperties().situationId, effectiveDate, ids, e);
                }
            } else {
                feature.setGeometry(null);
            }
        }

        return feature;
    }

    private String convertImsJsonTo_V1Compatible(final String imsJson) throws JsonProcessingException {
        return convertImsJsonToV3And_V1Compatible(imsJson);
    }

    private String convertImsJsonToV3And_V1Compatible(final String imsJson) throws JsonProcessingException {
        final JsonNode root = genericJsonReader.readTree(imsJson);

        // append versionTime if missing
        final JsonNode properties = readPropertiesFromTheImsJson(root);
        fixVersionTime(properties);

        final JsonNode announcements = readAnnouncementsFromTheImsJsonProperties(properties);
        // if announcements is found json might be V0_2_4 and features must be converted to V0_2_6 and V0_2_8 format
        fixAnnouncements(announcements);

        // Return fixed json
        return objectMapper.writer().writeValueAsString(root);
    }

    private void fixVersionTime(final JsonNode properties) {
        if (properties != null && !properties.has("versionTime")) {
            ((ObjectNode)properties).set("versionTime", properties.get("releaseTime"));
        }
    }

    private void fixAnnouncements(final JsonNode announcements) {
        if (announcements == null || announcements.isEmpty()) {
            return;
        }
        for (final JsonNode announcement : announcements) {

            final ArrayNode roadWorkPhases = (ArrayNode) announcement.get("roadWorkPhases");
            if (roadWorkPhases != null && !roadWorkPhases.isEmpty()) {
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
                    if (features != null && !features.isEmpty()) {
                        final ArrayNode worktypes = objectMapper.createArrayNode();

                        for (final JsonNode f : features) {
                            final ObjectNode worktype = objectMapper.createObjectNode();
                            worktype.put("type", "other");
                            worktype.set("description", f.get("name"));
                            worktypes.add(worktype);
                        }
                        ((ObjectNode) roadWorkPhase).set("worktypes", worktypes);
                    }

                    fixWorktypesToCamelCase(roadWorkPhase);
                }
            }
            final ArrayNode features = (ArrayNode) announcement.get("features");
            if (features != null && !features.isEmpty()) {
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
                if (!featuresWithProperties.isEmpty()) {
                    ((ObjectNode) announcement).set("features", featuresWithProperties);
                }
            }
        }
    }

    private void fixWorktypesToCamelCase(final JsonNode roadWorkPhase) {
        final ArrayNode workTypes = (ArrayNode) roadWorkPhase.get("worktypes");
        if (workTypes != null) {
            if (!workTypes.isEmpty()) {
                ((ObjectNode) roadWorkPhase).set("workTypes", workTypes);
            }
            ((ObjectNode) roadWorkPhase).remove("worktypes");
        }
    }

    protected ArrayNode readAnnouncementsFromTheImsJsonProperties(final JsonNode properties) {
        if (properties == null) {
            return null;
        }
        return (ArrayNode)properties.get("announcements");
    }

    protected JsonNode readPropertiesFromTheImsJson(final JsonNode root) {
        return root.get("properties");
    }

    private void checkDurationViolations_V1(final fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature feature) {
        final List<ConstraintViolation<fi.livi.digitraffic.tie.dto.trafficmessage.v1.EstimatedDuration>> violations =
            getDurationViolations_V1(feature);

        if (!violations.isEmpty()) {
            final String joinedViolations = violations.stream()
                .map(v -> String.format("Invalid EstimatedDuration.%s value %s", v.getPropertyPath(), v.getInvalidValue()))
                .collect(Collectors.joining(","));
            throw new IllegalArgumentException(joinedViolations + " " + ToStringHelper.toStringFull(feature));
        }
    }

    private List<ConstraintViolation<fi.livi.digitraffic.tie.dto.trafficmessage.v1.EstimatedDuration>> getDurationViolations_V1(
        final fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature feature) {

        return feature.getProperties().announcements.stream().map(this::getDurationViolations_V1).flatMap(Collection::stream).collect(Collectors.toList());
    }

    private Set<ConstraintViolation<fi.livi.digitraffic.tie.dto.trafficmessage.v1.EstimatedDuration>> getDurationViolations_V1(
        final fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncement a) {

        if (a.timeAndDuration != null && a.timeAndDuration.estimatedDuration != null) {
            return validator.validate(a.timeAndDuration.estimatedDuration);
        }
        return Collections.emptySet();
    }

    private static void checkIsInvalidAnnouncementGeojson_V1(final fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature feature) {
        if (feature.getProperties() == null) {
            throw new IllegalStateException("TrafficAnnouncementFeature with null properties " + ToStringHelper.toStringFull(feature));
        }
    }
}
