package fi.livi.digitraffic.tie.converter;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeDatex2FeatureDto;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationType;
import fi.livi.digitraffic.tie.service.trafficmessage.TrafficMessageJsonConverterV1;

@ConditionalOnWebApplication
@Component
public class WazeDatex2Converter {
    private static final Logger logger = LoggerFactory.getLogger(WazeDatex2Converter.class);
    private final TrafficMessageJsonConverterV1 datex2JsonConverterV1;

    public WazeDatex2Converter(final TrafficMessageJsonConverterV1 datex2JsonConverterV1) {
        this.datex2JsonConverterV1 = datex2JsonConverterV1;
    }

    public Optional<WazeDatex2FeatureDto> convertToWazeDatex2FeatureDto(final Datex2 datex2) {

        final TrafficAnnouncementFeature feature;
        final String jsonMessage = datex2.getJsonMessage();

        try {
            feature = datex2JsonConverterV1.convertToFeatureJsonObjectV3(
                jsonMessage,
                SituationType.TRAFFIC_ANNOUNCEMENT,
                datex2.getTrafficAnnouncementType(),
                false
            );
        } catch (final JsonProcessingException e) {
            logger.error("method=convertToWazeDatex2FeatureDto json string conversion to feature object failed", e);
            logger.info(String.format("DEBUG method=convertToWazeFeedAnnouncementDto json string conversion error in string: %s", jsonMessage));
            return Optional.empty();
        }

        return Optional.of(new WazeDatex2FeatureDto(datex2, feature));

    }

    public static boolean hasGeometry(final WazeDatex2FeatureDto wazeDatex2FeatureDto) {
        return wazeDatex2FeatureDto.feature.getGeometry() != null;
    }
}
