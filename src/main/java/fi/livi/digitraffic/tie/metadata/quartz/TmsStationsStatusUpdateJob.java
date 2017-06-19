package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationStatusUpdater;

@DisallowConcurrentExecution
public class TmsStationsStatusUpdateJob extends SimpleUpdateJob {

    @Autowired
    private RoadStationStatusUpdater roadStationStatusUpdater;

    @Override
    protected void doExecute(JobExecutionContext context) {
        final int tmsCount = roadStationStatusUpdater.updateTmsStationsStatuses();
        dataStatusService.updateDataUpdated(DataType.TMS_STATION_METADATA);
        log.info("Updated {} TMS stations statuses", tmsCount);
    }
}
