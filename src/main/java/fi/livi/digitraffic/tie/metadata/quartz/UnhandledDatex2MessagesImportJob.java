package fi.livi.digitraffic.tie.metadata.quartz;

import org.apache.commons.lang3.time.StopWatch;
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
        StopWatch sw = StopWatch.createStarted();
        datex2DataService.handleUnhandledDatex2Messages();
        sw.stop();
        log.info("Handle unhandled Datex2 messages took: {} ms", sw.getTime());
    }
}
