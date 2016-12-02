package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationStatusUpdater;

@DisallowConcurrentExecution
public class RoadStationsStatusUpdateJob extends SimpleUpdateJob {

    @Autowired
    private RoadStationStatusUpdater roadStationStatusUpdater;

    @Override
    protected void doExecute(JobExecutionContext context) {
        if (roadStationStatusUpdater.updateTmsStationsStatuses()) {
            staticDataStatusService.updateMetadataUpdated(MetadataType.LAM_STATION);
        }
        if (roadStationStatusUpdater.updateWeatherStationsStatuses()) {
            staticDataStatusService.updateMetadataUpdated(MetadataType.WEATHER_STATION);
        }
        if (roadStationStatusUpdater.updateCameraStationsStatuses()) {
            staticDataStatusService.updateMetadataUpdated(MetadataType.CAMERA_STATION);
        }
    }
}
