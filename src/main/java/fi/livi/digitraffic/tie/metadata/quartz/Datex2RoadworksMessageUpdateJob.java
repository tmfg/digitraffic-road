package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.data.service.datex2.Datex2RoadworksMessageUpdater;

@DisallowConcurrentExecution
public class Datex2RoadworksMessageUpdateJob extends SimpleUpdateJob {

    @Autowired
    private Datex2RoadworksMessageUpdater datex2RoadworksMessageUpdater;

    @Override
    protected void doExecute(final JobExecutionContext context) throws Exception {
        datex2RoadworksMessageUpdater.updateDatex2RoadworksMessages();
    }
}
