package fi.livi.digitraffic.tie.data.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.RoadStationStatusRepository;
import fi.livi.digitraffic.tie.data.dto.RoadStationStatusesDataObjectDto;

@Service
public class RoadStationStatusService {
    private final RoadStationStatusRepository roadStationStatusRepository;

    @Autowired
    public RoadStationStatusService(final RoadStationStatusRepository roadStationStatusRepository) {
        this.roadStationStatusRepository = roadStationStatusRepository;
    }

    @Transactional(readOnly = true)
    public RoadStationStatusesDataObjectDto findPublicRoadStationStatuses(final boolean onlyUpdateInfo) {
        final LocalDateTime updated = roadStationStatusRepository.getLatestMeasurementTime();

        if (onlyUpdateInfo) {
            return new RoadStationStatusesDataObjectDto(updated);
        } else {
            return new RoadStationStatusesDataObjectDto(
                    roadStationStatusRepository.findAllPublicRoadStationStatuses(),
                    updated);
        }
    }

    @Transactional(readOnly = true)
    public RoadStationStatusesDataObjectDto findPublicRoadStationStatus(final long roadStationId) {
        final LocalDateTime updated = roadStationStatusRepository.getLatestMeasurementTime();

        return new RoadStationStatusesDataObjectDto(
                    roadStationStatusRepository.findPublicRoadStationStatus(roadStationId),
                    updated);
    }
}
