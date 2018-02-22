package fi.livi.digitraffic.tie.metadata.quartz;

import org.apache.commons.lang3.time.StopWatch;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SimpleUpdateJob extends AbstractUpdateJob {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public final void execute(final JobExecutionContext context) {
        final String jobName = context.getJobDetail().getKey().getName();

        log.info("Quartz jobName={} start", jobName);

        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            doExecute(context);
        } catch(final Exception e) {
            log.error("Exception executing jobName=" + jobName, e);
        }

        stopWatch.stop();

        log.info("Quartz jobName={} end (took jobTimeMs={} ms)", jobName, stopWatch.getTime());
    }

    protected abstract void doExecute(final JobExecutionContext context) throws Exception;
}
