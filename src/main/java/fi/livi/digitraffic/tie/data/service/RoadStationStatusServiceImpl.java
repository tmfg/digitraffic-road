package fi.livi.digitraffic.tie.data.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.RoadStationStatusRepository;
import fi.livi.digitraffic.tie.metadata.model.RoadStationStatusesData;

@Service
public class RoadStationStatusServiceImpl implements RoadStationStatusService {
    private final RoadStationStatusRepository roadStationStatusRepository;

    @Autowired
    public RoadStationStatusServiceImpl(RoadStationStatusRepository roadStationStatusRepository) {
        this.roadStationStatusRepository = roadStationStatusRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public RoadStationStatusesData findAllRoadStationStatuses(boolean onlyUpdateInfo) {
        LocalDateTime updated = roadStationStatusRepository.getLatestMeasurementTime();

        if (onlyUpdateInfo) {
            return new RoadStationStatusesData(updated);
        } else {
            return new RoadStationStatusesData(
                    roadStationStatusRepository.findAllRoadStationStatuses(),
                    updated);
        }
    }
}
