package fi.livi.digitraffic.tie.data.service;

import fi.livi.digitraffic.tie.data.dto.freeflowspeed.FreeFlowSpeedRootDataObjectDto;

public interface FreeFlowSpeedService {
    FreeFlowSpeedRootDataObjectDto listPublicFreeFlowSpeeds(boolean onlyUpdateInfo);
}
