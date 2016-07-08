package fi.livi.digitraffic.tie.data.service;

import fi.livi.digitraffic.tie.data.dto.RoadStationStatusesDataObjectDto;

public interface RoadStationStatusService {
    RoadStationStatusesDataObjectDto findPublicRoadStationStatuses(boolean onlyUpdateInfo);
}
