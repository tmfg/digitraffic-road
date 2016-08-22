package fi.livi.digitraffic.tie.data.service;

import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dto.RoadStationStatusesDataObjectDto;

public interface RoadStationStatusService {
    RoadStationStatusesDataObjectDto findPublicRoadStationStatuses(boolean onlyUpdateInfo);

    @Transactional(readOnly = true)
    RoadStationStatusesDataObjectDto findPublicRoadStationStatus(long roadStationId);
}
