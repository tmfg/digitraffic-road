package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationStatusUpdater;

@DisallowConcurrentExecution
public class TmsStationsStatusMetadataUpdateJob extends SimpleUpdateJob {

    @Autowired
    private RoadStationStatusUpdater roadStationStatusUpdater;

    @Override
    protected void doExecute(JobExecutionContext context) {
        final int tmsCount = roadStationStatusUpdater.updateTmsStationsStatuses();
        if (tmsCount > 0) {
            dataStatusService.updateDataUpdated(DataType.TMS_STATION_METADATA);
        }
        dataStatusService.updateDataUpdated(DataType.TMS_STATION_METADATA_CHECK);
        log.info("Updated {} TMS stations statuses", tmsCount);
    }
}
