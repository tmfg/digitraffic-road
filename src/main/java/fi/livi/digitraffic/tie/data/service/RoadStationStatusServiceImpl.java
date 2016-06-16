package fi.livi.digitraffic.tie.data.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.RoadStationStatusRepository;
import fi.livi.digitraffic.tie.metadata.model.RoadStationStatus;
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
        List<RoadStationStatus> all = roadStationStatusRepository.findAllRoadStationStatuses();
        RoadStationStatus lastUptaded = all.stream().max(Comparator.comparing(
                s -> s.getLastUpdated())).orElse(null);

        return new RoadStationStatusesData(
                onlyUpdateInfo ? null : all,
                lastUptaded != null ? lastUptaded.getLastUpdated() : null);
    }
}
