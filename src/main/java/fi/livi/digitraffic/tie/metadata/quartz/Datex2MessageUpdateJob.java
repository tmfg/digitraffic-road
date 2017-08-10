package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.data.service.datex2.Datex2MessageUpdater;

@DisallowConcurrentExecution
public class Datex2MessageUpdateJob extends SimpleUpdateJob {

    @Autowired
    private Datex2MessageUpdater datex2MessageUpdater;

    @Override
    protected void doExecute(JobExecutionContext context) throws Exception {
        datex2MessageUpdater.updateDatex2TrafficAlertMessages();
    }
}
