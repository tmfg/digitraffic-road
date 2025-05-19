package fi.livi.digitraffic.tie.scheduler;

import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.trafficmessage.RegionGeometry;
import fi.livi.digitraffic.tie.service.trafficmessage.RegionGeometryGitClient;
import fi.livi.digitraffic.tie.service.trafficmessage.RegionGeometryUpdateService;

@DisallowConcurrentExecution
public class RegionGeometryUpdateJob extends SimpleUpdateJob {

    // AutowiringSpringBeanJobFactory takes care of autowiring
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private RegionGeometryGitClient regionGeometryGitClient;

    // AutowiringSpringBeanJobFactory takes care of autowiring
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private RegionGeometryUpdateService regionGeometryUpdateService;

    @Override
    protected void doExecute(final JobExecutionContext context) {
        final StopWatch start = StopWatch.createStarted();

        final String currentLatestCommitId = regionGeometryUpdateService.getLatestCommitId();
        final List<RegionGeometry> changes =
            regionGeometryGitClient.getChangesAfterCommit(currentLatestCommitId);

        regionGeometryUpdateService.saveChanges(changes);

        final String latestCommitId = regionGeometryUpdateService.getLatestCommitId();

        if (!changes.isEmpty()) {
            dataStatusService.updateDataUpdated(DataType.TRAFFIC_MESSAGES_REGION_GEOMETRY_DATA);
        }
        dataStatusService.updateDataUpdated(DataType.TRAFFIC_MESSAGES_REGION_GEOMETRY_DATA_CHECK);
        log.info("method=updateAreaLocationRegion from commitId {} to {} insertCount={} tookMs={}",
            currentLatestCommitId, latestCommitId, changes.size(), start.getDuration().toMillis());
    }
}
