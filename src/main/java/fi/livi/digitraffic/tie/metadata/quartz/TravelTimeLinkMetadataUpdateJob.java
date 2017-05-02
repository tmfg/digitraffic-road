package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.service.traveltime.TravelTimeLinkMetadataUpdater;

@DisallowConcurrentExecution
public class TravelTimeLinkMetadataUpdateJob extends SimpleUpdateJob {

    @Autowired
    private TravelTimeLinkMetadataUpdater travelTimeLinkMetadataUpdater;

    @Override
    protected void doExecute(JobExecutionContext context) throws Exception {

        travelTimeLinkMetadataUpdater.updateLinkMetadataIfUpdateAvailable();
    }
}
