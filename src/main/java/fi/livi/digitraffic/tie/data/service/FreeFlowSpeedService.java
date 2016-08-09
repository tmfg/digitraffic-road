package fi.livi.digitraffic.tie.data.service;

import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dto.freeflowspeed.FreeFlowSpeedRootDataObjectDto;

public interface FreeFlowSpeedService {
    FreeFlowSpeedRootDataObjectDto listLinksPublicFreeFlowSpeeds(boolean onlyUpdateInfo);

    @Transactional(readOnly = true)
    FreeFlowSpeedRootDataObjectDto listLinksPublicFreeFlowSpeeds(long linkId);

    @Transactional(readOnly = true)
    FreeFlowSpeedRootDataObjectDto listLamsPublicFreeFlowSpeeds(long lamId);
}
