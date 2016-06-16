package fi.livi.digitraffic.tie.data.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.LamFreeFlowSpeedRepository;
import fi.livi.digitraffic.tie.data.dao.LinkFreeFlowSpeedRepository;
import fi.livi.digitraffic.tie.data.dto.freeflowspeed.FreeFlowSpeedRootDataObjectDto;

@Service
public class FreeFlowSpeedServiceImpl implements FreeFlowSpeedService {
    private final LinkFreeFlowSpeedRepository linkFreeFlowSpeedRepository;
    private final LamFreeFlowSpeedRepository lamFreeFlowSpeedRepository;

    @Autowired
    public FreeFlowSpeedServiceImpl(final LinkFreeFlowSpeedRepository linkFreeFlowSpeedRepository, final LamFreeFlowSpeedRepository
            lamFreeFlowSpeedRepository) {
        this.linkFreeFlowSpeedRepository = linkFreeFlowSpeedRepository;
        this.lamFreeFlowSpeedRepository = lamFreeFlowSpeedRepository;
    }

    // TODO onlyUpdateInfo: do direct query to get update info
    @Transactional(readOnly = true)
    @Override
    public FreeFlowSpeedRootDataObjectDto listAllFreeFlowSpeeds(boolean onlyUpdateInfo) {
        FreeFlowSpeedRootDataObjectDto data =
                new FreeFlowSpeedRootDataObjectDto(linkFreeFlowSpeedRepository.listAllLinkFreeFlowSpeeds(),
                                                   lamFreeFlowSpeedRepository.listAllLamFreeFlowSpeeds(),
                                                   // TODO where should this info be found?
                                                   LocalDateTime.now());
        if (onlyUpdateInfo) {
            data.clearData();
        }
        return data;
    }
}
