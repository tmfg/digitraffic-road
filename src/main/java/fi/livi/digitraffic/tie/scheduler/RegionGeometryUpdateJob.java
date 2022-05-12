package fi.livi.digitraffic.tie.scheduler;

import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v3.trafficannouncement.geojson.RegionGeometry;
import fi.livi.digitraffic.tie.service.v2.datex2.RegionGeometryGitClient;
import fi.livi.digitraffic.tie.service.v3.datex2.V3RegionGeometryUpdateService;

@DisallowConcurrentExecution
public class RegionGeometryUpdateJob extends SimpleUpdateJob {

    @Autowired
    private RegionGeometryGitClient regionGeometryGitClient;

    @Autowired
    private V3RegionGeometryUpdateService v3RegionGeometryUpdateService;

    @Override
    protected void doExecute(final JobExecutionContext context) {
        final StopWatch start = StopWatch.createStarted();

        final String currentLatestCommitId = v3RegionGeometryUpdateService.getLatestCommitId();
        final List<RegionGeometry> changes =
            regionGeometryGitClient.getChangesAfterCommit(currentLatestCommitId);

        v3RegionGeometryUpdateService.saveChanges(changes);

        final String latestCommitId = v3RegionGeometryUpdateService.getLatestCommitId();

        if (changes.size() > 0) {
            dataStatusService.updateDataUpdated(DataType.TRAFFIC_MESSAGES_REGION_GEOMETRY_DATA);
        }
        dataStatusService.updateDataUpdated(DataType.TRAFFIC_MESSAGES_REGION_GEOMETRY_DATA_CHECK);
        log.info("method=updateAreaLocationRegion from commitId {} to {} insertCount={} tookMs={}",
            currentLatestCommitId, latestCommitId, changes.size(), start.getTime());
    }
}
