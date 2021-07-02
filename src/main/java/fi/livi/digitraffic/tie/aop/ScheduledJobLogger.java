package fi.livi.digitraffic.tie.aop;

import static fi.livi.digitraffic.tie.scheduler.JobLogger.JobType.Scheduled;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import fi.livi.digitraffic.tie.scheduler.JobLogger;
import fi.livi.digitraffic.tie.scheduler.JobLogger.JobType;

@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ScheduledJobLogger {

    private static final Logger log = LoggerFactory.getLogger(ScheduledJobLogger.class);
    private static final JobType jobType = Scheduled;

    /**
     * By default every method with @Scheduled annotation is monitored for
     * logging execution start (debug) and end or error.
     */
    @Around("@annotation(org.springframework.scheduling.annotation.Scheduled) && !@annotation(fi.livi.digitraffic.tie.aop.NoJobLogging)")
    public Object monitorScheduledJob(final ProceedingJoinPoint pjp) throws Throwable {
        final String method = pjp.getSignature().getName();
        // Strip away Configuration suffix and Spring proxy classes
        final String jobClass = StringUtils.substringBefore(StringUtils.substringBefore(pjp.getTarget().getClass().getSimpleName(),"Configuration"), "$");

        final StopWatch stopWatch = StopWatch.createStarted();
        final String jobName = jobClass + "." + method;

        JobLogger.logJobStart(log, jobType, jobName);

        Exception error = null;
        try {
            return pjp.proceed();
        } catch (final Exception e) {
            error = e;
            throw e;
        } finally {
            stopWatch.stop();
            if (error == null) {
                JobLogger.logJobEndStatusSuccess(log, jobType, jobName, stopWatch.getTime());
            } else {
                JobLogger.logJobEndStatusFail(log, jobType, jobName, stopWatch.getTime(), error);
            }
        }
    }
}
