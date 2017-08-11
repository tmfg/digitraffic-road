package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.data.service.datex2.Datex2TrafficAlertMessageUpdater;

@DisallowConcurrentExecution
public class Datex2MessageUpdateJob extends SimpleUpdateJob {

    @Autowired
    private Datex2TrafficAlertMessageUpdater datex2TrafficAlertMessageUpdater;

    @Override
    protected void doExecute(JobExecutionContext context) throws Exception {
        datex2TrafficAlertMessageUpdater.updateDatex2TrafficAlertMessages();
    }
}
