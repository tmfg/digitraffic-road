package fi.livi.digitraffic.tie.scheduler;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;

import fi.livi.digitraffic.tie.service.data.DataUpdatingService;

import org.springframework.beans.factory.annotation.Autowired;

@DisallowConcurrentExecution
public class DataIncomingHandlerJob extends SimpleUpdateJob {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private DataUpdatingService dataUpdatingService;

    @Override
    protected void doExecute(final JobExecutionContext context) throws Exception {
        dataUpdatingService.handleNewData();
    }
}
