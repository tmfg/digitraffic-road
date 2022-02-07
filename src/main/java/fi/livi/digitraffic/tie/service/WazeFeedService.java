package fi.livi.digitraffic.tie.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.converter.WazeDatex2JsonConverter;
import fi.livi.digitraffic.tie.dao.WazeDatex2Repository;
import fi.livi.digitraffic.tie.dto.WazeFeedAnnouncementDto;
import fi.livi.digitraffic.tie.dto.WazeFeedIncidentsDto;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2;

@ConditionalOnWebApplication
@Service
public class WazeFeedService {

    private final WazeDatex2Repository wazeRepository;
    private final WazeDatex2JsonConverter wazeDatex2JsonConverter;

    @Autowired
    public WazeFeedService(final WazeDatex2Repository wazeRepository,
                           final WazeDatex2JsonConverter wazeDatex2JsonConverter) {
        this.wazeRepository = wazeRepository;
        this.wazeDatex2JsonConverter = wazeDatex2JsonConverter;
    }

    @Transactional(readOnly = true)
    public WazeFeedAnnouncementDto findActive() {
        final List<Datex2> activeIncidents = wazeRepository.findAllActive();

        final List<WazeFeedIncidentsDto> incidents = activeIncidents.stream()
            .map(this.wazeDatex2JsonConverter::convertToWazeFeedAnnouncementDto)
            .flatMap(Optional::stream)
            .collect(Collectors.toList());

        return new WazeFeedAnnouncementDto(incidents);
    }

}