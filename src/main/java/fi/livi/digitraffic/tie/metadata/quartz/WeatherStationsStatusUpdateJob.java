package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationStatusUpdater;

@DisallowConcurrentExecution
public class WeatherStationsStatusUpdateJob extends SimpleUpdateJob {

    @Autowired
    private RoadStationStatusUpdater roadStationStatusUpdater;

    @Override
    protected void doExecute(JobExecutionContext context) {
        final int wsCount = roadStationStatusUpdater.updateWeatherStationsStatuses();
        staticDataStatusService.updateMetadataUpdated(MetadataType.WEATHER_STATION);

        log.info("Updated {} weather stations statuses", wsCount);
    }
}
