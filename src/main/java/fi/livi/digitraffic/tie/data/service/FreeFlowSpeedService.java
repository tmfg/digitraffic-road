package fi.livi.digitraffic.tie.data.service;

import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.TmsFreeFlowSpeedRepository;
import fi.livi.digitraffic.tie.data.dto.freeflowspeed.FreeFlowSpeedRootDataObjectDto;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.service.DataStatusService;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationService;

@Service
public class FreeFlowSpeedService {
    private final TmsFreeFlowSpeedRepository tmsFreeFlowSpeedRepository;
    private final TmsStationService tmsStationService;
    private final DataStatusService dataStatusService;

    @Autowired
    public FreeFlowSpeedService(final TmsFreeFlowSpeedRepository tmsFreeFlowSpeedRepository,
                                final TmsStationService tmsStationService,
                                final DataStatusService dataStatusService) {
        this.tmsFreeFlowSpeedRepository = tmsFreeFlowSpeedRepository;
        this.tmsStationService = tmsStationService;
        this.dataStatusService = dataStatusService;
    }


    @Transactional(readOnly = true)
    public FreeFlowSpeedRootDataObjectDto listLinksPublicFreeFlowSpeeds(final boolean onlyUpdateInfo) {
        final ZonedDateTime tmsUpdated = dataStatusService.findDataUpdatedTimeByDataType(DataType.TMS_FREE_FLOW_SPEEDS_DATA);

        if (onlyUpdateInfo) {
            return new FreeFlowSpeedRootDataObjectDto(tmsUpdated);
        } else {
            return new FreeFlowSpeedRootDataObjectDto(
                    tmsFreeFlowSpeedRepository.listAllPublicTmsFreeFlowSpeeds(),
                    tmsUpdated);
        }
    }

    @Transactional(readOnly = true)
    public FreeFlowSpeedRootDataObjectDto listTmsPublicFreeFlowSpeeds(final long roadStationNaturalId) {
        if (!tmsStationService.tmsStationExistsWithRoadStationNaturalId(roadStationNaturalId)) {
            throw new ObjectNotFoundException("TmsStation", roadStationNaturalId);
        }

        return new FreeFlowSpeedRootDataObjectDto(
                tmsFreeFlowSpeedRepository.listAllPublicTmsFreeFlowSpeeds(roadStationNaturalId),
                dataStatusService.findDataUpdatedTimeByDataType(DataType.TMS_FREE_FLOW_SPEEDS_DATA));

    }
}
