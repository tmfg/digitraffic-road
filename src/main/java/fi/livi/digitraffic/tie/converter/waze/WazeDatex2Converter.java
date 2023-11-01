package fi.livi.digitraffic.tie.converter.waze;

import static fi.livi.digitraffic.tie.datex2.ExtendedRoadOrCarriagewayOrLaneManagementTypeEnum.ICE_ROAD_OPEN;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.oxm.UnmarshallingFailureException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.OverallPeriod;
import fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagement;
import fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementExtensionType;
import fi.livi.digitraffic.tie.datex2.Situation;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.datex2.SituationRecord;
import fi.livi.digitraffic.tie.datex2.ValidityStatusEnum;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeDatex2FeatureDto;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.SituationType;
import fi.livi.digitraffic.tie.service.trafficmessage.Datex2XmlStringToObjectMarshaller;
import fi.livi.digitraffic.tie.service.trafficmessage.TrafficMessageImsJsonConverterV1;

@ConditionalOnWebApplication
@Component
public class WazeDatex2Converter {
    private static final Logger logger = LoggerFactory.getLogger(WazeDatex2Converter.class);
    private final TrafficMessageImsJsonConverterV1 datex2JsonConverterV1;
    private final Datex2XmlStringToObjectMarshaller datex2XmlStringToObjectMarshaller;

    public WazeDatex2Converter(final TrafficMessageImsJsonConverterV1 datex2JsonConverterV1, final Datex2XmlStringToObjectMarshaller datex2XmlStringToObjectMarshaller) {
        this.datex2JsonConverterV1 = datex2JsonConverterV1;
        this.datex2XmlStringToObjectMarshaller = datex2XmlStringToObjectMarshaller;
    }

    public Optional<WazeDatex2FeatureDto> convertToWazeDatex2FeatureDto(final Datex2 datex2) {

        final TrafficAnnouncementFeature feature;
        final String jsonMessage = datex2.getJsonMessage();

        try {
            feature = datex2JsonConverterV1.convertToFeatureJsonObject_V1(
                jsonMessage,
                SituationType.TRAFFIC_ANNOUNCEMENT,
                datex2.getTrafficAnnouncementType(),
                false,
                    datex2.getModified()
            );
        } catch (final JsonProcessingException e) {
            logger.error("method=convertToWazeDatex2FeatureDto json string conversion to feature object failed", e);
            logger.info(String.format("DEBUG method=convertToWazeFeedAnnouncementDto json string conversion error in string: %s", jsonMessage));
            return Optional.empty();
        }

        final D2LogicalModel d2LogicalModel;

        try {
            d2LogicalModel = datex2XmlStringToObjectMarshaller.convertToObject(datex2.getMessage());
        } catch (UnmarshallingFailureException e) {
            logger.error("method=export situation {} did not have a proper datex2 message, error: {}", feature.getProperties().situationId, e.getMessage());
            return Optional.empty();
        }

        return Optional.of(new WazeDatex2FeatureDto(datex2, d2LogicalModel, feature));

    }

    public static boolean hasGeometry(final WazeDatex2FeatureDto wazeDatex2FeatureDto) {
        return wazeDatex2FeatureDto.feature.getGeometry() != null;
    }

    public static boolean hasActiveSituationRecords(final WazeDatex2FeatureDto wazeDatex2FeatureDto) {
        final String situationId = wazeDatex2FeatureDto.feature.getProperties().situationId;

        final boolean result = getSituationRecords(situationId, wazeDatex2FeatureDto.d2LogicalModel)
                .stream()
                .anyMatch(WazeDatex2Converter::isActiveSituationRecord);

        if (!result) {
            logger.info("method=hasActiveSituationRecords situation {} did not have any valid situation records", situationId);
        }

        return result;
    }

    public static boolean isActiveSituationRecord(final SituationRecord situationRecord) {
        final ValidityStatusEnum validityStatus = situationRecord.getValidity().getValidityStatus();

        switch (validityStatus) {
        case ACTIVE:
            return true;
        case DEFINED_BY_VALIDITY_TIME_SPEC:
            final OverallPeriod validityTimeSpec = situationRecord.getValidity().getValidityTimeSpecification();
            final Instant now = Instant.now();
            final Instant overallEndTime = validityTimeSpec.getOverallEndTime();
            return now.isAfter(validityTimeSpec.getOverallStartTime()) &&
                    overallEndTime == null || now.isBefore(overallEndTime);
        }

        return false;
    }

    public static List<SituationRecord> getSituationRecords(final String situationId, final D2LogicalModel d2LogicalModel) {
        final SituationPublication situationPublication = (SituationPublication) d2LogicalModel.getPayloadPublication();
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

    public static boolean hasIceRoadOpenRecord(final WazeDatex2FeatureDto wazeDatex2FeatureDto) {
        final String situationId = wazeDatex2FeatureDto.feature.getProperties().situationId;
        return getSituationRecords(situationId, wazeDatex2FeatureDto.d2LogicalModel)
                .stream()
                .filter(sr -> sr instanceof RoadOrCarriagewayOrLaneManagement)
                .anyMatch(sr -> Optional.of((RoadOrCarriagewayOrLaneManagement) sr)
                        .map(RoadOrCarriagewayOrLaneManagement::getRoadOrCarriagewayOrLaneManagementExtension)
                        .map(RoadOrCarriagewayOrLaneManagementExtensionType::getRoadOrCarriagewayOrLaneManagementType)
                        .map(x -> x.equals(ICE_ROAD_OPEN))
                        .orElse(false));
    }
}
