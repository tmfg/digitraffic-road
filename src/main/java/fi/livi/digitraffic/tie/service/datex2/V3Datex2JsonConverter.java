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

import org.apache.commons.collections.CollectionUtils;
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

import fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncement;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationType;
import fi.livi.digitraffic.tie.model.v1.datex2.TrafficAnnouncementType;
import fi.livi.digitraffic.tie.service.v3.datex2.V3RegionGeometryDataService;

@ConditionalOnWebApplication
@Component
public class V3Datex2JsonConverter {
    private static final Logger log = LoggerFactory.getLogger(V3Datex2JsonConverter.class);

    protected final ObjectReader featureJsonReaderV2;
    protected final ObjectReader featureJsonReaderV3;

    protected final Validator validator;
    protected final ObjectReader genericJsonReader;
    private V3RegionGeometryDataService v3RegionGeometryDataService;

    protected ObjectMapper objectMapper;

    @Autowired
    public V3Datex2JsonConverter(final ObjectMapper objectMapper,
                                 final V3RegionGeometryDataService v3RegionGeometryDataService) {
        this.objectMapper = objectMapper;

        featureJsonReaderV2 = objectMapper.readerFor(TrafficAnnouncementFeature.class);
        featureJsonReaderV3 = objectMapper.readerFor(fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeature.class);

        genericJsonReader = objectMapper.reader();
        this.v3RegionGeometryDataService = v3RegionGeometryDataService;

        final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
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
            feature.getProperties().announcements.stream().filter(a -> a != null && a.containsAreaLocation()).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(announcementsWithAreas)) {
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

    private List<ConstraintViolation<fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.EstimatedDuration>> getDurationViolationsV3(
        final fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeature feature) {

        return feature.getProperties().announcements.stream().map(this::getDurationViolationsV3).flatMap(Collection::stream).collect(Collectors.toList());
    }

    private Set<ConstraintViolation<fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.EstimatedDuration>> getDurationViolationsV3(
        final fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncement a) {

        if (a.timeAndDuration != null && a.timeAndDuration.estimatedDuration != null) {
            return validator.validate(a.timeAndDuration.estimatedDuration);
        }
        return Collections.emptySet();
    }

    private static void checkIsInvalidAnnouncementGeojsonV3(final fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeature feature) {
        if (feature.getProperties() == null) {
            throw new IllegalStateException("TrafficAnnouncementFeature with null properties " + ToStringHelper.toStringFull(feature));
        }
    }
}
