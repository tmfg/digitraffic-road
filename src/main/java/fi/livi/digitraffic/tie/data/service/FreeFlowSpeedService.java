package fi.livi.digitraffic.tie.data.service;

import java.time.ZonedDateTime;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.LinkFreeFlowSpeedRepository;
import fi.livi.digitraffic.tie.data.dao.TmsFreeFlowSpeedRepository;
import fi.livi.digitraffic.tie.data.dto.freeflowspeed.FreeFlowSpeedRootDataObjectDto;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.service.DataStatusService;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationService;

@Service
public class FreeFlowSpeedService {
    private final LinkFreeFlowSpeedRepository linkFreeFlowSpeedRepository;
    private final TmsFreeFlowSpeedRepository tmsFreeFlowSpeedRepository;
    private final TmsStationService tmsStationService;
    private final DataStatusService dataStatusService;

    @Autowired
    public FreeFlowSpeedService(final LinkFreeFlowSpeedRepository linkFreeFlowSpeedRepository,
                                final TmsFreeFlowSpeedRepository tmsFreeFlowSpeedRepository,
                                final TmsStationService tmsStationService,
                                final DataStatusService dataStatusService) {
        this.linkFreeFlowSpeedRepository = linkFreeFlowSpeedRepository;
        this.tmsFreeFlowSpeedRepository = tmsFreeFlowSpeedRepository;
        this.tmsStationService = tmsStationService;
        this.dataStatusService = dataStatusService;
    }


    @Transactional(readOnly = true)
    public FreeFlowSpeedRootDataObjectDto listLinksPublicFreeFlowSpeeds(final boolean onlyUpdateInfo) {

        final ZonedDateTime linkUpdated = dataStatusService.findDataUpdatedTimeByDataType(DataType.LINK_FREE_FLOW_SPEEDS_DATA);
        final ZonedDateTime tmsUpdated = dataStatusService.findDataUpdatedTimeByDataType(DataType.TMS_FREE_FLOW_SPEEDS_DATA);
        final ZonedDateTime updated = DateHelper.getNewest(linkUpdated, tmsUpdated);
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
        return new FreeFlowSpeedRootDataObjectDto(
                linkFreeFlowSpeedRepository.listAllLinkFreeFlowSpeeds(linkId),
                Collections.emptyList(),
                dataStatusService.findDataUpdatedTimeByDataType(DataType.LINK_FREE_FLOW_SPEEDS_DATA));

    }

    @Transactional(readOnly = true)
    public FreeFlowSpeedRootDataObjectDto listTmsPublicFreeFlowSpeeds(final long roadStationNaturalId) {
        if (!tmsStationService.tmsStationExistsWithRoadStationNaturalId(roadStationNaturalId)) {
            throw new ObjectNotFoundException("TmsStation", roadStationNaturalId);
        }

        return new FreeFlowSpeedRootDataObjectDto(
                Collections.emptyList(),
                tmsFreeFlowSpeedRepository.listAllPublicTmsFreeFlowSpeeds(roadStationNaturalId),
                dataStatusService.findDataUpdatedTimeByDataType(DataType.TMS_FREE_FLOW_SPEEDS_DATA));

    }
}
