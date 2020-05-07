package fi.livi.digitraffic.tie.scheduler;

import org.apache.commons.lang3.time.StopWatch;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SimpleUpdateJob extends AbstractUpdateJob {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public final void execute(final JobExecutionContext context) {
        final String jobName = context.getJobDetail().getKey().getName();

        log.info("jobType=Quartz jobName={} start", jobName);

        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Exception lastError = null;
        try {
            doExecute(context);
        } catch(final Exception e) {
            lastError = e;
            log.error("jobType=Quartz Exception executing jobName=" + jobName, e);
        }

        stopWatch.stop();

        if (lastError != null) {
            log.info("jobType=Quartz jobName={} end jobEndStatus={} jobTimeMs={} lastError: {} {}",
                     jobName, lastError == null ? "SUCCESS" : "FAIL", stopWatch.getTime(),
                     lastError != null ? lastError.getClass() : null, lastError != null ? lastError.getMessage() : "");
        } else {
            log.info("jobType=Quartz jobName={} end jobEndStatus={} jobTimeMs={}",
                     jobName, lastError == null ? "SUCCESS" : "FAIL", stopWatch.getTime());
        }
    }

    protected abstract void doExecute(final JobExecutionContext context) throws Exception;
}
