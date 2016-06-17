package fi.livi.digitraffic.tie.data.service;

import fi.livi.digitraffic.tie.metadata.model.RoadStationStatusesData;

public interface RoadStationStatusService {
    RoadStationStatusesData findAllRoadStationStatuses(boolean onlyUpdateInfo);
}
