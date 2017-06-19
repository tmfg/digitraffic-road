package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationStatusUpdater;

@DisallowConcurrentExecution
public class TmsStationsStatusUpdateJob extends SimpleUpdateJob {

    @Autowired
    private RoadStationStatusUpdater roadStationStatusUpdater;

    @Override
    protected void doExecute(JobExecutionContext context) {
        final int tmsCount = roadStationStatusUpdater.updateTmsStationsStatuses();
        dataStatusService.updateMetadataUpdated(MetadataType.LAM_STATION);
        log.info("Updated {} TMS stations statuses", tmsCount);
    }
}
