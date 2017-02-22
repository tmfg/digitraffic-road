package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.data.service.Datex2DataService;

@DisallowConcurrentExecution
public class UnhandledDatex2MessagesImportJob extends SimpleUpdateJob {

    @Autowired
    private Datex2DataService datex2DataService;

    @Override
    protected void doExecute(JobExecutionContext context) throws Exception {
        datex2DataService.handleUnhandledDatex2Messages();
    }
}
