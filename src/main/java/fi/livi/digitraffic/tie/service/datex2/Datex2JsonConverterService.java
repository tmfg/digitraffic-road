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
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

@Service
public class Datex2JsonConverterService {
    private static final Logger log = LoggerFactory.getLogger(Datex2JsonConverterService.class);

    protected final ObjectReader featureJsonReaderV2;
    protected final ObjectReader featureJsonReaderV3;

    protected final Validator validator;

    protected final ObjectReader imsJsonReaderV0_2_4;
    protected final ObjectReader imsJsonReaderV0_2_5;

    protected final ObjectWriter imsJsonWriterV0_2_5;
    protected final ObjectWriter imsJsonWriterV0_2_4;
    protected final ObjectReader genericJsonReader;

    protected ObjectMapper objectMapper;

    @Autowired
    public Datex2JsonConverterService(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        imsJsonWriterV0_2_4 = objectMapper.writerFor(fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.ImsGeoJsonFeature.class);
        imsJsonWriterV0_2_5 = objectMapper.writerFor(fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.ImsGeoJsonFeature.class);

        imsJsonReaderV0_2_4 = objectMapper.readerFor(fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.ImsGeoJsonFeature.class);
        imsJsonReaderV0_2_5 = objectMapper.readerFor(fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.ImsGeoJsonFeature.class);

        featureJsonReaderV2 = objectMapper.readerFor(fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeature.class);
        featureJsonReaderV3 = objectMapper.readerFor(fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature.class);

        genericJsonReader = objectMapper.reader();

        final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    public fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeature convertToFeatureJsonObjectV2(final String imsJson)
        throws JsonProcessingException {
        // Ims JSON String can be in 0.2.4 or in 0.2.5 format. Convert 0.2.5 to in 0.2.4 format.
        final String imsJsonV0_2_4 = convertImsJsonToV0_2_4(imsJson);

        final fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeature feature =
            featureJsonReaderV2.readValue(imsJsonV0_2_4);

        checkIsInvalidAnnouncementGeojsonV2(feature);
        checkDurationViolationsV2(feature);

        return feature;
    }

    public fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature convertToFeatureJsonObjectV3(final String imsJson)
        throws JsonProcessingException {
        // Ims JSON String can be in 0.2.4 or in 0.2.5 format. Convert 0.2.4 to in 0.2.5 format.
        final String imsJsonV3 = convertImsJsonToV0_2_5(imsJson);

        final fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature feature =
            featureJsonReaderV3.readValue(imsJsonV3);

        checkIsInvalidAnnouncementGeojsonV3(feature);
        checkDurationViolationsV3(feature);

        return feature;
    }

    private String convertImsJsonToV0_2_4(final String imsJson) throws JsonProcessingException {
        final JsonNode root = genericJsonReader.readTree(imsJson);
        final JsonNode announcements = readAnnouncementsFromTheImsJson(root);
        // if announcements is found json might be V0_2_5 and features must be converted to C0_2_4 format
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

            // V0_2_4 doesn't have roadWorkPhases
            final ArrayNode roadWorkPhases = (ArrayNode) announcement.get("roadWorkPhases");
            if (roadWorkPhases != null) {
                ((ObjectNode)announcement).remove("roadWorkPhases");
            }

        }
        return objectMapper.writer().writeValueAsString(root);
    }

    private String convertImsJsonToV0_2_5(final String imsJson) throws JsonProcessingException {
        final JsonNode root = genericJsonReader.readTree(imsJson);
        final JsonNode announcements = readAnnouncementsFromTheImsJson(root);
        // if announcements is found json might be V0_2_4 and features must be converted to C0_2_5 format
        if ( announcements == null || announcements.isEmpty() ) {
            return imsJson;
        }

        for (final JsonNode announcement : announcements) {
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
    }

    protected JsonNode readAnnouncementsFromTheImsJson(final JsonNode root) {
        final JsonNode properties = root.get("properties");
        if (properties == null) {
            return null;
        }
        return properties.get("announcements");
    }

    private void checkDurationViolationsV2(final fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeature feature) {
        final List<ConstraintViolation<fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.EstimatedDuration>> violations =
            getDurationViolationsV2(feature);

        if (!violations.isEmpty()) {
            final String joinedViolations = violations.stream()
                .map(v -> String.format("Invalid EstimatedDuration.%s value %s", v.getPropertyPath(), v.getInvalidValue()))
                .collect(Collectors.joining(","));
            throw new IllegalArgumentException(joinedViolations + " " + ToStringHelper.toStringFull(feature));
        }
    }

    private void checkDurationViolationsV3(final fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature feature) {
        final List<ConstraintViolation<fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.EstimatedDuration>> violations =
            getDurationViolationsV3(feature);

        if (!violations.isEmpty()) {
            final String joinedViolations = violations.stream()
                .map(v -> String.format("Invalid EstimatedDuration.%s value %s", v.getPropertyPath(), v.getInvalidValue()))
                .collect(Collectors.joining(","));
            throw new IllegalArgumentException(joinedViolations + " " + ToStringHelper.toStringFull(feature));
        }
    }

    private List<ConstraintViolation<fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.EstimatedDuration>> getDurationViolationsV2(
        final fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeature feature) {

        return feature.getProperties().announcements.stream().map(this::getDurationViolationsV2).flatMap(Collection::stream).collect(Collectors.toList());
    }

    private List<ConstraintViolation<fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.EstimatedDuration>> getDurationViolationsV3(
        final fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature feature) {

        return feature.getProperties().announcements.stream().map(this::getDurationViolationsV3).flatMap(Collection::stream).collect(Collectors.toList());
    }

    private Set<ConstraintViolation<fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.EstimatedDuration>> getDurationViolationsV2(
        final fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncement a) {

        if (a.timeAndDuration != null && a.timeAndDuration.estimatedDuration != null) {
            return validator.validate(a.timeAndDuration.estimatedDuration);
        }
        return Collections.emptySet();
    }

    private Set<ConstraintViolation<fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.EstimatedDuration>> getDurationViolationsV3(
        final fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncement a) {

        if (a.timeAndDuration != null && a.timeAndDuration.estimatedDuration != null) {
            return validator.validate(a.timeAndDuration.estimatedDuration);
        }
        return Collections.emptySet();
    }

    private static void checkIsInvalidAnnouncementGeojsonV2(final fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeature feature) {
        if (feature.getProperties() == null) {
            throw new IllegalStateException("TrafficAnnouncementFeature with null properties " + ToStringHelper.toStringFull(feature));
        }
    }

    private static void checkIsInvalidAnnouncementGeojsonV3(final fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature feature) {
        if (feature.getProperties() == null) {
            throw new IllegalStateException("TrafficAnnouncementFeature with null properties " + ToStringHelper.toStringFull(feature));
        }
    }
}
