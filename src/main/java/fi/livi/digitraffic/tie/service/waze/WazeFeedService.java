package fi.livi.digitraffic.tie.service.waze;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.converter.waze.WazeDatex2Converter;
import fi.livi.digitraffic.tie.converter.waze.WazeDatex2JsonConverter;
import fi.livi.digitraffic.tie.dao.trafficmessage.datex2.Datex2Repository;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.SituationType;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedAnnouncementDto;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedIncidentDto;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2;


@ConditionalOnWebApplication
@Service
public class WazeFeedService {
    private static final Logger logger = LoggerFactory.getLogger(WazeFeedService.class);

    private final Datex2Repository datex2Repository;
    private final WazeDatex2Converter wazeDatex2Converter;
    private final WazeDatex2JsonConverter wazeDatex2JsonConverter;

    @Autowired
    public WazeFeedService(final Datex2Repository datex2Repository,
                           final WazeDatex2Converter wazeDatex2Converter,
                           final WazeDatex2JsonConverter wazeDatex2JsonConverter) {
        this.datex2Repository = datex2Repository;
        this.wazeDatex2Converter = wazeDatex2Converter;
        this.wazeDatex2JsonConverter = wazeDatex2JsonConverter;
    }

    @Transactional(readOnly = true)
    public WazeFeedAnnouncementDto findActive() {
        final List<Datex2> activeIncidents = datex2Repository.findAllActiveBySituationTypeWithJson(1,
        SituationType.TRAFFIC_ANNOUNCEMENT.name(), SituationType.ROAD_WORK.name());

        logger.info("method=findActive active incidents count=" + activeIncidents.size());

        final List<WazeFeedIncidentDto> incidents = activeIncidents.stream()
            .map(this.wazeDatex2Converter::convertToWazeDatex2FeatureDto)
            .flatMap(Optional::stream)
            .filter(WazeDatex2Converter::isValidWazeEntry)
            .map(this.wazeDatex2JsonConverter::convertToWazeFeedAnnouncementDto)
            .flatMap(Optional::stream)
            .collect(Collectors.toList());

        logger.info("method=findActive valid incidents count=" + incidents.size());

        return new WazeFeedAnnouncementDto(incidents);
    }
}