package fi.livi.digitraffic.tie.service.v2.datex2;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.Situation;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.datex2.SituationRecord;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.ImsGeoJsonFeature;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.EstimatedDuration;
import fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncement;
import fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeature;

@Service
@Primary
public class V2Datex2HelperService {
    private static final Logger log = LoggerFactory.getLogger(V2Datex2HelperService.class);

    protected final ObjectReader featureJsonReaderV2;
    protected final ObjectReader featureJsonReaderV3;

    protected final Validator validator;

    protected final List<ObjectWriter> jsonWriters = new ArrayList<>();

    protected final List<ObjectReader> imsJsonReaders = new ArrayList<>();
    protected final ObjectReader imsJsonReaderV0_2_4;
    protected final ObjectReader imsJsonReaderV0_2_5;

    protected final ObjectWriter imsJsonWriterV0_2_5;
    protected final ObjectWriter imsJsonWriterV0_2_4;
    protected final ObjectReader genericJsonReader;

    protected ObjectMapper objectMapper;

    @Autowired
    public V2Datex2HelperService(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        imsJsonWriterV0_2_4 = objectMapper.writerFor(ImsGeoJsonFeature.class);
        imsJsonWriterV0_2_5 = objectMapper.writerFor(fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.ImsGeoJsonFeature.class);

        jsonWriters.add(imsJsonWriterV0_2_4);
        jsonWriters.add(imsJsonWriterV0_2_5);

        imsJsonReaderV0_2_4 = objectMapper.readerFor(ImsGeoJsonFeature.class);
        imsJsonReaderV0_2_5 = objectMapper.readerFor(fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.ImsGeoJsonFeature.class);

        imsJsonReaders.add(imsJsonReaderV0_2_4);
        imsJsonReaders.add(imsJsonReaderV0_2_5);

        featureJsonReaderV2 = objectMapper.readerFor(TrafficAnnouncementFeature.class);
        featureJsonReaderV3 = objectMapper.readerFor(fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature.class);

        genericJsonReader = objectMapper.reader();

        final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    public static boolean isNewOrUpdatedSituation(final ZonedDateTime latestVersionTime, final Situation situation) {
        return isNewOrUpdatedSituation(DateHelper.toInstant(latestVersionTime), situation);
    }

    public static boolean isNewOrUpdatedSituation(final Instant latestVersionTime, final Situation situation) {
        // does any record have new version time?
        return latestVersionTime == null || situation.getSituationRecords().stream().anyMatch(r -> isUpdatedRecord(latestVersionTime, r));
    }

    public static boolean isUpdatedRecord(final Instant latestVersionTime, final SituationRecord record) {
        // different resolution, so remove fractions of second
        final Instant vTime = DateHelper.withoutMillis(record.getSituationRecordVersionTime());
        return vTime.isAfter(DateHelper.withoutMillis(latestVersionTime) );
    }

    public static SituationPublication getSituationPublication(final D2LogicalModel model) {
        if (model.getPayloadPublication() instanceof SituationPublication) {
            return (SituationPublication) model.getPayloadPublication();
        } else {
            final String err = "Not SituationPublication available for " + model.getPayloadPublication().getClass();
            log.error(err);
            throw new IllegalArgumentException(err);
        }
    }

    public static void checkD2HasOnlyOneSituation(D2LogicalModel d2) {
        final int situations = getSituationPublication(d2).getSituations().size();
        if ( situations > 1 ) {
            log.error("method=checkOnyOneSituation D2LogicalModel had {) situations. Only 1 is allowed in this service.");
            throw new java.lang.IllegalArgumentException("D2LogicalModel passed to Datex2UpdateService can only have one situation per message, " +
                "there was " + situations);
        }
    }

    public TrafficAnnouncementFeature convertToFeatureJsonObjectV2(final String imsJson) {
        // Ims JSON String can be in 0.2.4 or in 0.2.5 format. Convert 0.2.5 to in 0.2.4 format.
        final String imsJsonV0_2_4 = convertImsJsonToV0_2_4(imsJson);

        try {
            final TrafficAnnouncementFeature feature = featureJsonReaderV2.readValue(imsJsonV0_2_4);
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

    private String convertImsJsonToV0_2_4(final String imsJson) {
        try {
            final JsonNode root = genericJsonReader.readTree(imsJson);
            final JsonNode announcements = readAnnouncementsFromTheImsJson(root);
            // if announcements is found json might be V0_2_5 and features must be converted to C0_2_4 format
            if (announcements == null) {
                return imsJson;
            }

            for (JsonNode announcement : announcements) {
                final ArrayNode features = (ArrayNode) announcement.get("features");

                if (features != null && features.size() > 0) {
                    final ArrayNode newFeaturesArrayNode = objectMapper.createArrayNode();
                    for (JsonNode f : features) {
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
            }
            return objectMapper.writer().writeValueAsString(root);
        } catch (Exception e) {
            return imsJson;
        }
    }

    protected JsonNode readAnnouncementsFromTheImsJson(final JsonNode root) {
        final JsonNode properties = root.get("properties");
        if (properties == null) {
            return null;
        }
        return properties.get("announcements");
    }

    private TrafficAnnouncementFeature tryFeatureV2(String imsJson) {
        try {
            return featureJsonReaderV2.readValue(imsJson);
        } catch (final JsonProcessingException e) {
            return null;
        }
    }

    private fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature tryFeatureV3(String imsJson) {
        try {
            return featureJsonReaderV3.readValue(imsJson);
        } catch (final JsonProcessingException e) {
            return null;
        }
    }

    private List<ConstraintViolation<EstimatedDuration>> getDurationViolations(final TrafficAnnouncementFeature feature) {
        return feature.getProperties().announcements.stream().map(this::getDurationViolations).flatMap(Collection::stream).collect(Collectors.toList());
    }

    private Set<ConstraintViolation<EstimatedDuration>> getDurationViolations(TrafficAnnouncement a) {
        if (a.timeAndDuration != null && a.timeAndDuration.estimatedDuration != null) {
            return validator.validate(a.timeAndDuration.estimatedDuration);
        }
        return Collections.emptySet();
    }

    private static boolean isInvalidGeojson(final TrafficAnnouncementFeature feature) {
        return feature.getProperties() == null;
    }
}
