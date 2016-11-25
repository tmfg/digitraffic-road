package fi.livi.digitraffic.tie.data.service;

import java.time.ZonedDateTime;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.LinkFreeFlowSpeedRepository;
import fi.livi.digitraffic.tie.data.dao.TmsFreeFlowSpeedRepository;
import fi.livi.digitraffic.tie.data.dto.freeflowspeed.FreeFlowSpeedRootDataObjectDto;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationService;

@Service
public class FreeFlowSpeedService {
    private final LinkFreeFlowSpeedRepository linkFreeFlowSpeedRepository;
    private final TmsFreeFlowSpeedRepository tmsFreeFlowSpeedRepository;
    private final TmsStationService tmsStationService;

    @Autowired
    public FreeFlowSpeedService(final LinkFreeFlowSpeedRepository linkFreeFlowSpeedRepository,
                                final TmsFreeFlowSpeedRepository tmsFreeFlowSpeedRepository,
                                final TmsStationService tmsStationService) {
        this.linkFreeFlowSpeedRepository = linkFreeFlowSpeedRepository;
        this.tmsFreeFlowSpeedRepository = tmsFreeFlowSpeedRepository;
        this.tmsStationService = tmsStationService;
    }


    @Transactional(readOnly = true)
    public FreeFlowSpeedRootDataObjectDto listLinksPublicFreeFlowSpeeds(final boolean onlyUpdateInfo) {

        // TODO: where to read update info?
        final ZonedDateTime updated = ZonedDateTime.now();
        if (onlyUpdateInfo) {
            return new FreeFlowSpeedRootDataObjectDto(updated);
        } else {
            return new FreeFlowSpeedRootDataObjectDto(
                    linkFreeFlowSpeedRepository.listAllLinkFreeFlowSpeeds(),
                    tmsFreeFlowSpeedRepository.listAllPublicTmsFreeFlowSpeeds(),
                    updated);
        }
    }

    @Transactional(readOnly = true)
    public FreeFlowSpeedRootDataObjectDto listLinksPublicFreeFlowSpeeds(final long linkId) {
        if (1 != linkFreeFlowSpeedRepository.linkExists(linkId)) {
            throw new ObjectNotFoundException("Link", linkId);
        }
        // TODO: where to read update info?
        final ZonedDateTime updated = ZonedDateTime.now();
        return new FreeFlowSpeedRootDataObjectDto(
                linkFreeFlowSpeedRepository.listAllLinkFreeFlowSpeeds(linkId),
                Collections.emptyList(),
                updated);

    }

    @Transactional(readOnly = true)
    public FreeFlowSpeedRootDataObjectDto listTmsPublicFreeFlowSpeeds(final long roadStationNaturalId) {

        // TODO: where to read update info?
        final ZonedDateTime updated = ZonedDateTime.now();
        if (!tmsStationService.tmsStationExistsWithRoadStationNaturalId(roadStationNaturalId)) {
            throw new ObjectNotFoundException("TmsStation", roadStationNaturalId);
        }
        return new FreeFlowSpeedRootDataObjectDto(
                Collections.emptyList(),
                tmsFreeFlowSpeedRepository.listAllPublicTmsFreeFlowSpeeds(roadStationNaturalId),
                updated);

    }
}
