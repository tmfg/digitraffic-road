package fi.livi.digitraffic.tie.data.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.data.dao.LamFreeFlowSpeedRepository;
import fi.livi.digitraffic.tie.data.dao.LinkFreeFlowSpeedRepository;
import fi.livi.digitraffic.tie.data.model.FreeFlowSpeedObject;

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

    @Override
    public FreeFlowSpeedObject listAllFreeFlowSpeeds() {
        return new FreeFlowSpeedObject(linkFreeFlowSpeedRepository.listAllLinkFreeFlowSpeeds(), lamFreeFlowSpeedRepository
                .listAllLamFreeFlowSpeeds());
    }
}