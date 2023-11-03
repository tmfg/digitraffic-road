package fi.livi.digitraffic.tie.scheduler;

import static fi.livi.digitraffic.common.scheduler.JobLogger.JobType.Quartz;

import org.apache.commons.lang3.time.StopWatch;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.livi.digitraffic.common.scheduler.JobLogger;
import fi.livi.digitraffic.common.scheduler.JobLogger.JobType;

public abstract class SimpleUpdateJob extends AbstractUpdateJob {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final JobType jobType = Quartz;

    @Override
    public final void execute(final JobExecutionContext context) {
        final String jobName = context.getJobDetail().getKey().getName();

        JobLogger.logJobStart(log, jobType, jobName);

        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            doExecute(context);
            JobLogger.logJobEndStatusSuccess(log, jobType, jobName, stopWatch.getTime());
        } catch(final Exception e) {
            JobLogger.logJobEndStatusFail(log, jobType, jobName, stopWatch.getTime(), e);
        }
    }

    protected abstract void doExecute(final JobExecutionContext context) throws Exception;
}
