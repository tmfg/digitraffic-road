package fi.livi.digitraffic.tie.converter.waze;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import tools.jackson.core.JacksonException;

import fi.livi.digitraffic.tie.datex2.v3_7.Comment;
import fi.livi.digitraffic.tie.datex2.v3_7.ConstructionWorks;
import fi.livi.digitraffic.tie.datex2.v3_7.EquipmentOrSystemFault;
import fi.livi.digitraffic.tie.datex2.v3_7.MultilingualString;
import fi.livi.digitraffic.tie.datex2.v3_7.MultilingualStringValue;
import fi.livi.digitraffic.tie.datex2.v3_7.OverallPeriod;
import fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagement;
import fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7.Situation;
import fi.livi.digitraffic.tie.datex2.v3_7.SituationPublication;
import fi.livi.digitraffic.tie.datex2.v3_7.SituationRecord;
import fi.livi.digitraffic.tie.datex2.v3_7.TransitInformation;
import fi.livi.digitraffic.tie.datex2.v3_7._RoadOrCarriagewayOrLaneManagementTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7._ValidityStatusEnum;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.SituationType;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeDatex2FeatureDto;
import fi.livi.digitraffic.tie.service.trafficmessage.DatexII37XmlMarshaller;
import fi.livi.digitraffic.tie.service.trafficmessage.TrafficMessageImsJsonConverterV1;

@ConditionalOnWebApplication
@Component
public class WazeDatex2Converter {
    private static final Logger logger = LoggerFactory.getLogger(WazeDatex2Converter.class);

    /**
     * Matches the Finnish phrase TLOIK uses for an ice road that is open to traffic. A closed ice road
     * reads "Jäätie suljettu" and must not match.
     */
    private static final Pattern ICE_ROAD_OPEN = Pattern.compile("jäätie\\s+auki", Pattern.CASE_INSENSITIVE);

    /** Matches the Finnish phrase TLOIK uses for an ice road that is closed to traffic. */
    private static final Pattern ICE_ROAD_CLOSED = Pattern.compile("jäätie\\s+suljettu", Pattern.CASE_INSENSITIVE);

    /**
     * Matches the Finnish sentence TLOIK uses to tell that a faulty equipment has been repaired.
     * <p>
     * Anchored to the start of a line on purpose: TLOIK writes it as a sentence of its own
     * ("Liikennevalot.\nKorjattu."). Without the anchor the word would also match inside a sentence
     * such as "Lisätieto: Sortuma korjattu", which is about road damage and not about equipment.
     */
    private static final Pattern REPAIRED =
        Pattern.compile("^\\s*korjattu\\b", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    private final TrafficMessageImsJsonConverterV1 datex2JsonConverterV1;
    private final DatexII37XmlMarshaller datex2XmlStringToObjectMarshaller;

    public record SituationMessagePair(
        String situationId,
        fi.livi.digitraffic.tie.model.trafficmessage.datex2.SituationType situationType,
        String simppeliMessage,
        String datex2Message,
        Instant modifiedAt) {
    }

    public WazeDatex2Converter(final TrafficMessageImsJsonConverterV1 datex2JsonConverterV1,
                               final DatexII37XmlMarshaller datex2XmlStringToObjectMarshaller) {
        this.datex2JsonConverterV1 = datex2JsonConverterV1;
        this.datex2XmlStringToObjectMarshaller = datex2XmlStringToObjectMarshaller;
    }

    public Optional<WazeDatex2FeatureDto> convertToWazeDatex2FeatureDto(final SituationMessagePair messages) {
        final TrafficAnnouncementFeature feature;

        try {
            feature = datex2JsonConverterV1.convertToFeatureJsonObject_V1(
                messages.simppeliMessage(),
                messages.situationType(),
                null,
                false,
                messages.modifiedAt());
        } catch (final JacksonException e) {
            logger.error("method=convertToWazeDatex2FeatureDto json string conversion to feature object failed", e);
            logger.info("DEBUG method=convertToWazeFeedAnnouncementDto json string conversion error in string: {}", messages.simppeliMessage());
            return Optional.empty();
        }

        final SituationPublication situationPublication;
        try {
            situationPublication = datex2XmlStringToObjectMarshaller.convertToObject(null, messages.datex2Message());
        } catch (final RuntimeException e) {
            logger.error("method=export situation {} did not have a proper datex2 message, error: {}", messages.situationId(), e.getMessage());
            return Optional.empty();
        }

        return Optional.of(new WazeDatex2FeatureDto(
            messages.situationId(),
            SituationType.fromValue(messages.situationType().name()),
            messages.datex2Message(),
            situationPublication,
            feature));
    }

    public static boolean hasGeometry(final WazeDatex2FeatureDto wazeDatex2FeatureDto) {
        return wazeDatex2FeatureDto.feature.getGeometry() != null;
    }

    public static boolean hasActiveSituationRecords(final WazeDatex2FeatureDto wazeDatex2FeatureDto) {
        final String situationId = wazeDatex2FeatureDto.situationId;

        final boolean result = getSituationRecords(situationId, wazeDatex2FeatureDto.situationPublication)
            .stream()
            .anyMatch(WazeDatex2Converter::isActiveSituationRecord);

        if (!result) {
            logger.info("method=hasActiveSituationRecords situation {} did not have any valid situation records", situationId);
        }

        return result;
    }

    public static boolean isActiveSituationRecord(final SituationRecord situationRecord) {
        final _ValidityStatusEnum validityStatus = situationRecord.getValidity().getValidityStatus();

        if (validityStatus == null || validityStatus.getValue() == null) {
            return false;
        }

        switch (validityStatus.getValue()) {
            case ACTIVE:
                return true;
            case DEFINED_BY_VALIDITY_TIME_SPEC:
                final OverallPeriod validityTimeSpec = situationRecord.getValidity().getValidityTimeSpecification();
                final Instant overallEndTime = validityTimeSpec.getOverallEndTime();
                // Relevant until it has ended; a missing end time means it is valid indefinitely.
                // The start time is deliberately ignored: the Waze CIFS feed publishes planned
                // closures in advance and Waze schedules them itself from the starttime we send,
                // so a record whose validity has not started yet must still be included.
                // "You can use the Waze Partner feed to provide planned and real time road closures.
                //  The planned closures you send to Waze [...] will appear on the Waze app during
                //  the actual time of the closure."
                // https://developers.google.com/waze/data-feed/road-closure-information
                return overallEndTime == null || Instant.now().isBefore(overallEndTime);
            default:
                return false;
        }
    }

    public static List<SituationRecord> getSituationRecords(final String situationId, final SituationPublication situationPublication) {
        if (situationPublication == null) {
            logger.info("method=getSituationRecords situation {} did not have a situation publication payload", situationId);
            return List.of();
        }

        final List<Situation> situations = situationPublication.getSituations();
        if (situations.isEmpty()) {
            logger.info("method=getSituationRecords situation {} did not have any situation records", situationId);
            return List.of();
        }

        return situations.stream()
            .map(Situation::getSituationRecords)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    /**
     * Detects an ice road that is open to traffic, which is not an incident and must be kept out of
     * the Waze feed.
     *
     * <p>Datex II 2.2.3 had a Finnish extension carrying {@code iceRoadOpen}. Datex II 3.7 dropped it
     * and has no replacement: the value is missing both from
     * {@link RoadOrCarriagewayOrLaneManagementTypeEnum} and from the restricted {@code _extendedValue}
     * set, so TLOIK sends {@code other} and states the ice road only in the Finnish free text. That
     * text is therefore the only signal left.</p>
     */
    public static boolean hasIceRoadOpenRecord(final WazeDatex2FeatureDto wazeDatex2FeatureDto) {
        return getSituationRecords(wazeDatex2FeatureDto.situationId, wazeDatex2FeatureDto.situationPublication)
            .stream()
            .anyMatch(WazeDatex2Converter::isIceRoadOpenRecord);
    }

    public static boolean isIceRoadOpenRecord(final SituationRecord situationRecord) {
        return matchesIceRoadPhrase(situationRecord, ICE_ROAD_OPEN);
    }

    /**
     * Detects an ice road that is closed to traffic. Read from the Finnish free text for the same
     * reason as {@link #isIceRoadOpenRecord}: Datex II 3.7 has no coded value for it.
     */
    public static boolean isIceRoadClosedRecord(final SituationRecord situationRecord) {
        return matchesIceRoadPhrase(situationRecord, ICE_ROAD_CLOSED);
    }

    /**
     * Tells whether a faulty equipment has been repaired.
     * <p>
     * Datex II 2.2.3 had a Finnish extension value {@code repaired} that took precedence over the
     * base fault type. Datex II 3.7 dropped the extension and its fault types only describe a fault
     * ({@code notWorking}, {@code outOfService}, {@code workingIncorrectly},
     * {@code workingIntermittently}), so the base type still names the original fault after it has
     * been fixed. Read from the Finnish free text for the same reason as {@link #isIceRoadOpenRecord}.
     * <p>
     * Without this the description turns into its opposite: a repaired traffic light set would be
     * announced as "Traffic light sets not working".
     */
    public static boolean isRepairedRecord(final SituationRecord situationRecord) {
        return situationRecord instanceof EquipmentOrSystemFault
            && generalPublicComments(situationRecord).anyMatch(comment -> REPAIRED.matcher(comment).find());
    }

    private static boolean matchesIceRoadPhrase(final SituationRecord situationRecord, final Pattern phrase) {
        if (!(situationRecord instanceof final RoadOrCarriagewayOrLaneManagement management)) {
            return false;
        }

        // Do not loosen this to any management type. An open ice road drops the whole announcement
        // from the feed, and the phrase also appears in records that describe an ice road used as a
        // detour around an incident. Without the check those real incidents would vanish from Waze.
        final _RoadOrCarriagewayOrLaneManagementTypeEnum managementType =
            management.getRoadOrCarriagewayOrLaneManagementType();
        if (managementType == null || managementType.getValue() != RoadOrCarriagewayOrLaneManagementTypeEnum.OTHER) {
            return false;
        }

        return generalPublicComments(situationRecord).anyMatch(comment -> phrase.matcher(comment).find());
    }

    private static Stream<String> generalPublicComments(final SituationRecord situationRecord) {
        return situationRecord.getGeneralPublicComments().stream()
            .map(Comment::getComment)
            .filter(Objects::nonNull)
            .map(MultilingualString::getValues)
            .filter(Objects::nonNull)
            .flatMap(values -> values.getValues().stream())
            .map(MultilingualStringValue::getValue)
            .filter(Objects::nonNull);
    }

    public static boolean hasTransitInformationRecord(final WazeDatex2FeatureDto wazeDatex2FeatureDto) {
        final String situationId = wazeDatex2FeatureDto.situationId;
        return getSituationRecords(situationId, wazeDatex2FeatureDto.situationPublication)
            .stream()
            .anyMatch(sr -> sr instanceof TransitInformation);
    }

    public static boolean isValidWazeEntry(final WazeDatex2FeatureDto feature) {
        return hasGeometry(feature)
            && hasActiveSituationRecords(feature)
            && isValidType(feature);
    }

    private static boolean isValidType(final WazeDatex2FeatureDto feature) {
        if (feature.feature.getProperties().getSituationType() == SituationType.TRAFFIC_ANNOUNCEMENT) {
            return !hasIceRoadOpenRecord(feature) && !hasTransitInformationRecord(feature);
        }

        final boolean lowSeverity = feature.situationPublication.getSituations().stream()
            .anyMatch(s -> s.getOverallSeverity() != null && s.getOverallSeverity().getValue() == fi.livi.digitraffic.tie.datex2.v3_7.SeverityEnum.LOW);
        if (lowSeverity) {
            return false;
        }

        return getSituationRecords(feature.situationId, feature.situationPublication).stream().anyMatch(sr ->
            (sr instanceof RoadOrCarriagewayOrLaneManagement management
                && management.getRoadOrCarriagewayOrLaneManagementType() != null && management.getRoadOrCarriagewayOrLaneManagementType().getValue() == fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.ROAD_CLOSED)
            || (sr instanceof ConstructionWorks constructionWorks
                && constructionWorks.getConstructionWorkType() != null && constructionWorks.getConstructionWorkType().getValue() == fi.livi.digitraffic.tie.datex2.v3_7.ConstructionWorkTypeEnum.CONSTRUCTION_WORK));
    }
}
