package fi.livi.digitraffic.tie.service.v3.datex2;

import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import fi.livi.digitraffic.tie.dao.v3.RegionGeometryRepository;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v3.trafficannouncement.geojson.RegionGeometry;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.v2.datex2.RegionGeometryGitClient;

@ConditionalOnNotWebApplication
@Service
public class V3RegionGeometryUpdateService {
    private static final Logger log = LoggerFactory.getLogger(V3RegionGeometryUpdateService.class);

    private final ObjectReader geometryReader;
    private RegionGeometryRepository regionGeometryRepository;
    private RegionGeometryGitClient regionGeometryGitClient;
    private final DataStatusService dataStatusService;

    @Autowired
    public V3RegionGeometryUpdateService(final RegionGeometryRepository regionGeometryRepository,
                                         final RegionGeometryGitClient regionGeometryGitClient,
                                         final DataStatusService dataStatusService,
                                         final ObjectMapper objectMapper) {
        this.regionGeometryRepository = regionGeometryRepository;
        this.regionGeometryGitClient = regionGeometryGitClient;
        this.dataStatusService = dataStatusService;
        geometryReader = objectMapper.readerFor(Geometry.class);
    }

    // Run every 24 h
    @Scheduled(fixedRate = 360000, initialDelayString = "${dt.scheduled.job.initialDelay.ms}")
    @Transactional
    public int updateAreaLocationRegions() {
        final StopWatch start = StopWatch.createStarted();
        final String currentLatestCommitId = regionGeometryRepository.getLatestCommitId();
        final List<RegionGeometry> changes =
            regionGeometryGitClient.getChangesAfterCommit(currentLatestCommitId);
        changes.forEach(c -> regionGeometryRepository.save(c));
        final String latestCommitId = regionGeometryRepository.getLatestCommitId();
        if (changes.size() > 0) {
            dataStatusService.updateDataUpdated(DataType.TRAFFIC_MESSAGES_REGION_GEOMETRY_DATA, latestCommitId);
        }
        dataStatusService.updateDataUpdated(DataType.TRAFFIC_MESSAGES_REGION_GEOMETRY_DATA_CHECK, latestCommitId);
        log.info("method=updateAreaLocationRegion from commitId {} to {} insertCount={} tookMs={}",
                 currentLatestCommitId, latestCommitId, changes.size(), start.getTime());
        return changes.size();
    }
}
