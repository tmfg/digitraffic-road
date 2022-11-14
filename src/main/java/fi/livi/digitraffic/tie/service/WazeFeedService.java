package fi.livi.digitraffic.tie.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.converter.WazeDatex2JsonConverter;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedAnnouncementDto;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedIncidentDto;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2;

@ConditionalOnWebApplication
@Service
public class WazeFeedService {

    private final Datex2Repository datex2Repository;
    private final WazeDatex2JsonConverter wazeDatex2JsonConverter;

    @Autowired
    public WazeFeedService(final Datex2Repository datex2Repository,
                           final WazeDatex2JsonConverter wazeDatex2JsonConverter) {
        this.datex2Repository = datex2Repository;
        this.wazeDatex2JsonConverter = wazeDatex2JsonConverter;
    }

    @Transactional(readOnly = true)
    public WazeFeedAnnouncementDto findActive() {
        final List<Datex2> activeIncidents = datex2Repository.findAllActiveBySituationTypeWithJson(1, SituationType.TRAFFIC_ANNOUNCEMENT.toString());

        final List<WazeFeedIncidentDto> incidents = activeIncidents.stream()
            .map(this.wazeDatex2JsonConverter::convertToWazeFeedAnnouncementDto)
            .flatMap(Optional::stream)
            .collect(Collectors.toList());

        return new WazeFeedAnnouncementDto(incidents);
    }

}